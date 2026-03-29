package dev.jos.back.service;

import dev.jos.back.entities.PasswordResetToken;
import dev.jos.back.entities.User;
import dev.jos.back.repository.PasswordResetTokenRepository;
import dev.jos.back.util.enums.TokenValidationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HmacResetTokenStoreTest {

    @Mock
    private PasswordResetTokenRepository repository;

    private HmacResetTokenStore store;

    @BeforeEach
    void setUp() {
        store = new HmacResetTokenStore(repository, "test-secret-key-for-unit-tests-only");
    }

    @Test
    void store_savesTokenWithHmacHash() {
        User user = new User();
        store.store(user, "abc-plain-token", LocalDateTime.now().plusHours(1));

        verify(repository).save(argThat(t ->
                t.getHashedToken() != null &&
                !t.getHashedToken().equals("abc-plain-token") &&
                t.getHashedToken().length() == 64
        ));
    }

    @Test
    void validate_returnsValid_whenTokenExistsAndNotExpiredAndNotUsed() {
        PasswordResetToken token = new PasswordResetToken();
        token.setExpiry(LocalDateTime.now().plusHours(1));
        token.setUsed(false);
        when(repository.findByHashedToken(any())).thenReturn(Optional.of(token));

        assertThat(store.validate("valid-token")).isEqualTo(TokenValidationResult.VALID);
    }

    @Test
    void validate_returnsExpired_whenTokenExpired() {
        PasswordResetToken token = new PasswordResetToken();
        token.setExpiry(LocalDateTime.now().minusMinutes(1));
        token.setUsed(false);
        when(repository.findByHashedToken(any())).thenReturn(Optional.of(token));

        assertThat(store.validate("expired-token")).isEqualTo(TokenValidationResult.EXPIRED);
    }

    @Test
    void validate_returnsNotFound_whenTokenAbsent() {
        when(repository.findByHashedToken(any())).thenReturn(Optional.empty());

        assertThat(store.validate("unknown-token")).isEqualTo(TokenValidationResult.NOT_FOUND);
    }

    @Test
    void validate_returnsNotFound_whenTokenAlreadyUsed() {
        PasswordResetToken token = new PasswordResetToken();
        token.setExpiry(LocalDateTime.now().plusHours(1));
        token.setUsed(true);
        when(repository.findByHashedToken(any())).thenReturn(Optional.of(token));

        assertThat(store.validate("used-token")).isEqualTo(TokenValidationResult.NOT_FOUND);
    }

    @Test
    void consume_marksTokenAsUsed() {
        PasswordResetToken token = new PasswordResetToken();
        token.setExpiry(LocalDateTime.now().plusHours(1));
        token.setUsed(false);
        when(repository.findByHashedToken(any())).thenReturn(Optional.of(token));

        store.consume("consume-token");

        assertThat(token.isUsed()).isTrue();
        verify(repository).save(token);
    }

    @Test
    void purgeExpired_deletesExpiredTokens() {
        store.purgeExpired();
        verify(repository).deleteExpired(any(LocalDateTime.class));
    }
}
