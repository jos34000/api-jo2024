package dev.jos.back.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PasswordResetTokenService {
    private final ResetTokenStore resetTokenStore;

    public void purgeExpiredCodes() {
        resetTokenStore.purgeExpired();
    }
}
