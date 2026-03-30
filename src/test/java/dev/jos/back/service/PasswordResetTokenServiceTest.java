package dev.jos.back.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PasswordResetTokenServiceTest {

    @Mock ResetTokenStore resetTokenStore;
    @InjectMocks PasswordResetTokenService service;

    @Test
    void purgeExpiredCodes_delegatesToResetTokenStore() {
        service.purgeExpiredCodes();

        verify(resetTokenStore).purgeExpired();
    }
}
