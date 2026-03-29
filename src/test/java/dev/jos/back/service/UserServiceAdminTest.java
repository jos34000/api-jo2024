package dev.jos.back.service;

import dev.jos.back.dto.user.UserResponseDTO;
import dev.jos.back.entities.User;
import dev.jos.back.exceptions.user.UserNotFoundException;
import dev.jos.back.mapper.UserMapper;
import dev.jos.back.repository.UserRepository;
import dev.jos.back.util.enums.Role;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceAdminTest {

    @Mock UserRepository userRepository;
    @Mock ResetTokenStore resetTokenStore;
    @Mock PasswordEncoder passwordEncoder;
    @Mock EmailService emailService;
    @Mock UserMapper userMapper;
    @InjectMocks UserService userService;

    @Test
    void getAllUsers_returnsMappedList() {
        User u1 = new User(); u1.setId(1L); u1.setEmail("a@a.com");
        User u2 = new User(); u2.setId(2L); u2.setEmail("b@b.com");
        UserResponseDTO dto1 = UserResponseDTO.builder().id(1L).email("a@a.com").build();
        UserResponseDTO dto2 = UserResponseDTO.builder().id(2L).email("b@b.com").build();

        when(userRepository.findAll()).thenReturn(List.of(u1, u2));
        when(userMapper.toResponseDTO(u1)).thenReturn(dto1);
        when(userMapper.toResponseDTO(u2)).thenReturn(dto2);

        List<UserResponseDTO> result = userService.getAllUsers();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).email()).isEqualTo("a@a.com");
    }

    @Test
    void deleteUser_callsDeleteById() {
        when(userRepository.existsById(1L)).thenReturn(true);

        userService.deleteUser(1L);

        verify(userRepository).deleteById(1L);
    }

    @Test
    void deleteUser_throwsUserNotFoundException_whenNotFound() {
        when(userRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> userService.deleteUser(99L))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void updateUserRole_changesRoleAndReturnsDTO() {
        User user = new User();
        user.setId(1L);
        user.setRole(Role.ROLE_USER);
        UserResponseDTO expected = UserResponseDTO.builder().id(1L).role(Role.ROLE_ADMIN).build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.saveAndFlush(user)).thenReturn(user);
        when(userMapper.toResponseDTO(user)).thenReturn(expected);

        UserResponseDTO result = userService.updateUserRole(1L, Role.ROLE_ADMIN);

        assertThat(result.role()).isEqualTo(Role.ROLE_ADMIN);
        assertThat(user.getRole()).isEqualTo(Role.ROLE_ADMIN);
    }

    @Test
    void updateUserRole_throwsUserNotFoundException_whenNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateUserRole(99L, Role.ROLE_ADMIN))
                .isInstanceOf(UserNotFoundException.class);
    }
}
