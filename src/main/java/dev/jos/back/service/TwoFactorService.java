package dev.jos.back.service;

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
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        repository.invalidateAll(userEmail);

        TwoFactorCode twoFactorCode = new TwoFactorCode();
        twoFactorCode.setUser(user);
        twoFactorCode.setCode(generateCode());
        twoFactorCode.setExpiresAt(LocalDateTime.now().plusMinutes(expiration));

        repository.save(twoFactorCode);

        emailService.sendTwoFactorEmail(user.getEmail(), twoFactorCode.getCode());
    }

    @Transactional
    public boolean verifyCode(String userEmail, String submittedCode) {
        TwoFactorCode twoFactorCode = repository.findLatestValid(userEmail, LocalDateTime.now())
                .orElseThrow(() -> new TwoFactorCodeNotFoundException("No valid code found"));

        if (twoFactorCode.getFailedAttempts() >= maxAttempts) {
            twoFactorCode.setUsed(true);
            repository.save(twoFactorCode);
            throw new TwoFactorMaxAttemptsException("Max attempts reached");
        }

        if (!twoFactorCode.getCode().equals(submittedCode)) {
            twoFactorCode.setFailedAttempts(twoFactorCode.getFailedAttempts() + 1);
            repository.save(twoFactorCode);
            return false;
        }

        twoFactorCode.setUsed(true);
        repository.save(twoFactorCode);
        return true;
    }

    @Transactional
    public void toggle(String userEmail, boolean enabled) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

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
