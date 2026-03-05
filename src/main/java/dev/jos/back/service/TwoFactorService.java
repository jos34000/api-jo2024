package dev.jos.back.service;

import dev.jos.back.exceptions.twofactor.BadTwoFactorCodeException;
import dev.jos.back.exceptions.twofactor.TwoFactorCodeNotFoundException;
import dev.jos.back.exceptions.twofactor.TwoFactorMaxAttemptsException;
import dev.jos.back.exceptions.user.UserNotFoundException;
import dev.jos.back.model.TwoFactorCode;
import dev.jos.back.model.User;
import dev.jos.back.repository.TwoFactorCodeRepository;
import dev.jos.back.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TwoFactorService {

    private final TwoFactorCodeRepository repository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    @Value("${two-factor.expiration}")
    private int expiration;

    @Value("${two-factor.max-attempts}")
    private int maxAttempts;

    @Transactional
    public void sendCode(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("L'utilisateur n'existe pas"));

        repository.invalidateAll(userEmail);

        TwoFactorCode twoFactorCode = new TwoFactorCode();
        twoFactorCode.setUser(user);
        twoFactorCode.setCode(generateCode());
        twoFactorCode.setExpiresAt(LocalDateTime.now().plusMinutes(expiration));

        repository.save(twoFactorCode);

        emailService.sendTwoFactorEmail(user.getEmail(), user.getFirstName(), twoFactorCode.getCode(), expiration);
    }

    public void verifyCode(String userEmail, String submittedCode) {
        TwoFactorCode twoFactorCode = repository.findLatestValid(userEmail, LocalDateTime.now())
                .orElseThrow(() -> new TwoFactorCodeNotFoundException("Pas de code valide."));

        if (!twoFactorCode.getCode().equals(submittedCode)) {
            int attempts = twoFactorCode.getFailedAttempts() + 1;
            twoFactorCode.setFailedAttempts(attempts);

            if (attempts >= maxAttempts) {
                twoFactorCode.setUsed(true);
            }

            repository.saveAndFlush(twoFactorCode);

            if (attempts >= maxAttempts) {
                throw new TwoFactorMaxAttemptsException("Tentatives maximales atteintes pour ce code.");
            }
            throw new BadTwoFactorCodeException("Code invalide.");
        }

        twoFactorCode.setUsed(true);
        repository.saveAndFlush(twoFactorCode);
    }

    @Transactional
    public void toggle(String userEmail, boolean enabled) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("L'utilisateur n'existe pas"));

        user.setMfaEnabled(enabled);
        userRepository.save(user);
    }

    @Transactional
    public void purgeExpiredCodes() {
        repository.deleteExpiredOrUsed(LocalDateTime.now());
    }

    private String generateCode() {
        return String.format("%06d", new SecureRandom().nextInt(1_000_000));
    }
}
