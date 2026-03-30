package dev.jos.back.service;

import dev.jos.back.dto.user.CreateUserDTO;
import dev.jos.back.dto.user.UserResponseDTO;
import dev.jos.back.entities.User;
import dev.jos.back.exceptions.user.InvalidPasswordException;
import dev.jos.back.exceptions.user.UserAlreadyExistsException;
import dev.jos.back.util.enums.Role;
import dev.jos.back.util.enums.TokenValidationResult;
import dev.jos.back.mapper.UserMapper;
import dev.jos.back.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock UserRepository userRepository;
    @Mock ResetTokenStore resetTokenStore;
    @Mock PasswordEncoder passwordEncoder;
    @Mock EmailService emailService;
    @Mock UserMapper userMapper;
    @InjectMocks UserService userService;

    // ── createUser ────────────────────────────────────────────────────────────

    @Test
    void createUser_savesUserAndReturnsDTO() {
        CreateUserDTO dto = new CreateUserDTO("john@example.com", "pass1234", "John", "Doe", false, null, null);
        User user = new User();
        UserResponseDTO expected = UserResponseDTO.builder().id(1L).email("john@example.com").build();

        when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(userMapper.toEntity(dto)).thenReturn(user);
        when(passwordEncoder.encode("pass1234")).thenReturn("hashed");
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toResponseDTO(user)).thenReturn(expected);

        UserResponseDTO result = userService.createUser(dto);

        assertThat(result.email()).isEqualTo("john@example.com");
        assertThat(user.getPasswordHash()).isEqualTo("hashed");
    }

    @Test
    void createUser_setsAdminRole() {
        CreateUserDTO dto = new CreateUserDTO("john@example.com", "pass1234", "John", "Doe", false, null, null);
        User user = new User();

        when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(userMapper.toEntity(dto)).thenReturn(user);
        when(passwordEncoder.encode("pass1234")).thenReturn("hashed");
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toResponseDTO(user)).thenReturn(UserResponseDTO.builder().id(1L).email("john@example.com").build());

        userService.createUser(dto);

        assertThat(user.getRole()).isEqualTo(Role.ROLE_ADMIN);
    }

    @Test
    void createUser_throwsUserAlreadyExistsException_whenEmailTaken() {
        CreateUserDTO dto = new CreateUserDTO("john@example.com", "pass1234", "John", "Doe", false, null, null);
        when(userRepository.existsByEmail("john@example.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(dto))
                .isInstanceOf(UserAlreadyExistsException.class);
    }

    // ── updateUser ────────────────────────────────────────────────────────────

    @Test
    void updateUser_updatesFieldsAndReturnsDTO() {
        User user = new User();
        user.setEmail("john@example.com");
        UserResponseDTO expected = UserResponseDTO.builder().id(1L).email("john@example.com").build();

        // same email → conflict guard skipped
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        when(userRepository.saveAndFlush(user)).thenReturn(user);
        when(userMapper.toResponseDTO(user)).thenReturn(expected);

        userService.updateUser("john@example.com", "john@example.com", "John", "Doe", true);

        assertThat(user.getFirstName()).isEqualTo("John");
        assertThat(user.getLastName()).isEqualTo("Doe");
        assertThat(user.isMfaEnabled()).isTrue();
    }

    @Test
    void updateUser_throwsUserAlreadyExistsException_whenNewEmailAlreadyTaken() {
        when(userRepository.existsByEmail("new@example.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.updateUser("old@example.com", "new@example.com", "John", "Doe", false))
                .isInstanceOf(UserAlreadyExistsException.class);
    }

    @Test
    void updateUser_throwsUsernameNotFoundException_whenUserNotFound() {
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateUser("john@example.com", "john@example.com", "John", "Doe", false))
                .isInstanceOf(UsernameNotFoundException.class);
    }

    // ── getUserResponseDto ────────────────────────────────────────────────────

    @Test
    void getUserResponseDto_returnsDTO_whenFound() {
        User user = new User();
        UserResponseDTO expected = UserResponseDTO.builder().id(1L).email("john@example.com").build();

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        when(userMapper.toResponseDTO(user)).thenReturn(expected);

        UserResponseDTO result = userService.getUserResponseDto("john@example.com");

        assertThat(result.email()).isEqualTo("john@example.com");
    }

    @Test
    void getUserResponseDto_throwsUsernameNotFoundException_whenNotFound() {
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserResponseDto("john@example.com"))
                .isInstanceOf(UsernameNotFoundException.class);
    }

    // ── updatePassword ────────────────────────────────────────────────────────

    @Test
    void updatePassword_encodesAndSavesNewPassword() {
        User user = new User();
        user.setPasswordHash("hashed-old");

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("old123", "hashed-old")).thenReturn(true);
        when(passwordEncoder.encode("new123")).thenReturn("hashed-new");
        when(userRepository.saveAndFlush(user)).thenReturn(user);

        userService.updatePassword("john@example.com", "old123", "new123");

        assertThat(user.getPasswordHash()).isEqualTo("hashed-new");
    }

    @Test
    void updatePassword_throwsInvalidPasswordException_whenOldPasswordWrong() {
        User user = new User();
        user.setPasswordHash("hashed-old");

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "hashed-old")).thenReturn(false);

        assertThatThrownBy(() -> userService.updatePassword("john@example.com", "wrong", "new123"))
                .isInstanceOf(InvalidPasswordException.class);
    }

    @Test
    void updatePassword_throwsUsernameNotFoundException_whenUserNotFound() {
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updatePassword("john@example.com", "old123", "new123"))
                .isInstanceOf(UsernameNotFoundException.class);
    }

    // ── resetPassword ─────────────────────────────────────────────────────────

    @Test
    void resetPassword_storesTokenAndSendsEmail_whenUserExists() {
        User user = new User();
        user.setEmail("john@example.com");
        user.setFirstName("John");

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));

        userService.resetPassword("john@example.com");

        verify(resetTokenStore).store(eq(user), anyString(), any(LocalDateTime.class));
        verify(emailService).sendPasswordResetEmail(eq("john@example.com"), eq("John"), anyString(), any());
    }

    @Test
    void resetPassword_doesNothing_whenUserNotFound() {
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        userService.resetPassword("unknown@example.com");

        verify(resetTokenStore, never()).store(any(), any(), any());
        verify(emailService, never()).sendPasswordResetEmail(any(), any(), any(), any());
    }

    // ── updateLocale ──────────────────────────────────────────────────────────

    @Test
    void updateLocale_persistsLocale() {
        User user = new User();
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        when(userRepository.saveAndFlush(user)).thenReturn(user);

        userService.updateLocale("john@example.com", "en");

        assertThat(user.getLocale()).isEqualTo("en");
    }

    @Test
    void updateLocale_throwsUsernameNotFoundException_whenUserNotFound() {
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateLocale("john@example.com", "en"))
                .isInstanceOf(UsernameNotFoundException.class);
    }

    // ── validateResetToken ────────────────────────────────────────────────────

    @Test
    void validateResetToken_delegatesToResetTokenStore() {
        when(resetTokenStore.validate("some-token")).thenReturn(TokenValidationResult.VALID);

        TokenValidationResult result = userService.validateResetToken("some-token");

        assertThat(result).isEqualTo(TokenValidationResult.VALID);
    }
}
