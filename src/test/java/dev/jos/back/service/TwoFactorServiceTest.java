package dev.jos.back.service;

import dev.jos.back.entities.TwoFactorCode;
import dev.jos.back.entities.User;
import dev.jos.back.exceptions.twofactor.BadTwoFactorCodeException;
import dev.jos.back.exceptions.twofactor.TwoFactorCodeNotFoundException;
import dev.jos.back.exceptions.twofactor.TwoFactorMaxAttemptsException;
import dev.jos.back.exceptions.user.UserNotFoundException;
import dev.jos.back.repository.TwoFactorCodeRepository;
import dev.jos.back.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TwoFactorServiceTest {

    @Mock TwoFactorCodeRepository repository;
    @Mock UserRepository userRepository;
    @Mock EmailService emailService;
    @InjectMocks TwoFactorService twoFactorService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(twoFactorService, "expiration", 5);
        ReflectionTestUtils.setField(twoFactorService, "maxAttempts", 3);
    }

    // ── sendCode ──────────────────────────────────────────────────────────────

    @Test
    void sendCode_invalidatesOldCodes_savesNewCode_andSendsEmail() {
        User user = new User();
        user.setEmail("alice@example.com");
        user.setFirstName("Alice");
        user.setLocale("fr");

        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(user));

        twoFactorService.sendCode("alice@example.com");

        verify(repository).invalidateAll("alice@example.com");

        ArgumentCaptor<TwoFactorCode> captor = ArgumentCaptor.forClass(TwoFactorCode.class);
        verify(repository).save(captor.capture());
        TwoFactorCode saved = captor.getValue();
        assertThat(saved.getCode()).matches("\\d{6}");
        assertThat(saved.getExpiresAt()).isAfter(LocalDateTime.now());
        assertThat(saved.getUser()).isSameAs(user);

        verify(emailService).sendTwoFactorEmail(
                eq("alice@example.com"), eq("Alice"), any(String.class), eq(5), eq("fr"));
    }

    @Test
    void sendCode_throwsUserNotFoundException_whenUserNotFound() {
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> twoFactorService.sendCode("unknown@example.com"))
                .isInstanceOf(UserNotFoundException.class);
    }

    // ── verifyCode ────────────────────────────────────────────────────────────

    @Test
    void verifyCode_marksCodeAsUsed_whenCodeMatches() {
        TwoFactorCode code = new TwoFactorCode();
        code.setCode("123456");
        code.setFailedAttempts(0);

        when(repository.findLatestValid(eq("alice@example.com"), any(LocalDateTime.class)))
                .thenReturn(Optional.of(code));

        twoFactorService.verifyCode("alice@example.com", "123456");

        assertThat(code.isUsed()).isTrue();
        verify(repository).saveAndFlush(code);
    }

    @Test
    void verifyCode_throwsBadTwoFactorCodeException_andIncrementsAttempts_whenCodeWrong() {
        TwoFactorCode code = new TwoFactorCode();
        code.setCode("999999");
        code.setFailedAttempts(0);

        when(repository.findLatestValid(eq("alice@example.com"), any(LocalDateTime.class)))
                .thenReturn(Optional.of(code));

        assertThatThrownBy(() -> twoFactorService.verifyCode("alice@example.com", "000000"))
                .isInstanceOf(BadTwoFactorCodeException.class);

        assertThat(code.getFailedAttempts()).isEqualTo(1);
        assertThat(code.isUsed()).isFalse();
        verify(repository).saveAndFlush(code);
    }

    @Test
    void verifyCode_throwsTwoFactorMaxAttemptsException_andInvalidatesCode_atMaxAttempts() {
        TwoFactorCode code = new TwoFactorCode();
        code.setCode("999999");
        code.setFailedAttempts(2); // maxAttempts=3, donc c'est la dernière tentative

        when(repository.findLatestValid(eq("alice@example.com"), any(LocalDateTime.class)))
                .thenReturn(Optional.of(code));

        assertThatThrownBy(() -> twoFactorService.verifyCode("alice@example.com", "000000"))
                .isInstanceOf(TwoFactorMaxAttemptsException.class);

        assertThat(code.getFailedAttempts()).isEqualTo(3);
        assertThat(code.isUsed()).isTrue();
    }

    @Test
    void verifyCode_throwsTwoFactorCodeNotFoundException_whenNoValidCodeExists() {
        when(repository.findLatestValid(eq("alice@example.com"), any(LocalDateTime.class)))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> twoFactorService.verifyCode("alice@example.com", "123456"))
                .isInstanceOf(TwoFactorCodeNotFoundException.class);
    }

    // ── toggle ────────────────────────────────────────────────────────────────

    @Test
    void toggle_enablesMfa_forUser() {
        User user = new User();
        user.setMfaEnabled(false);

        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(user));

        twoFactorService.toggle("alice@example.com", true);

        assertThat(user.isMfaEnabled()).isTrue();
        verify(userRepository).save(user);
    }

    @Test
    void toggle_disablesMfa_forUser() {
        User user = new User();
        user.setMfaEnabled(true);

        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(user));

        twoFactorService.toggle("alice@example.com", false);

        assertThat(user.isMfaEnabled()).isFalse();
        verify(userRepository).save(user);
    }

    @Test
    void toggle_throwsUserNotFoundException_whenUserNotFound() {
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> twoFactorService.toggle("unknown@example.com", true))
                .isInstanceOf(UserNotFoundException.class);
    }

    // ── purgeExpiredCodes ─────────────────────────────────────────────────────

    @Test
    void purgeExpiredCodes_delegatesToRepository() {
        twoFactorService.purgeExpiredCodes();

        verify(repository).deleteExpiredOrUsed(any(LocalDateTime.class));
    }
}
