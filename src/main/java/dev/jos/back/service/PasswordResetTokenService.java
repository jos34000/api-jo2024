package dev.jos.back.service;

import dev.jos.back.repository.PasswordResetTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PasswordResetTokenService {
    private final PasswordResetTokenRepository repository;

    public void purgeExpiredCodes() {
        repository.deleteExpired(LocalDateTime.now());
    }
}
