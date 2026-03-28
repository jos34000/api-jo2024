package dev.jos.back.service;

import dev.jos.back.entities.User;
import dev.jos.back.util.enums.TokenValidationResult;

import java.time.LocalDateTime;

public interface ResetTokenStore {
    void store(User user, String plainToken, LocalDateTime expiry);
    TokenValidationResult validate(String plainToken);
    void consume(String plainToken);
    void purgeExpired();
}
