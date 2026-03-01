package dev.jos.back.util;

import dev.jos.back.service.PasswordResetTokenService;
import dev.jos.back.service.TwoFactorService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CleanUp {

    private final TwoFactorService twoFactorService;
    private final PasswordResetTokenService passwordResetTokenService;

    @Scheduled(cron = "0 0 3 * * *")
    public void cleanTwoFactorCodes() {
        twoFactorService.purgeExpiredCodes();
    }

    @Scheduled(cron = "0 0 3 * * *")
    public void cleanResetPasswordTokens() {
        passwordResetTokenService.purgeExpiredCodes();
    }
}
