package dev.jos.back.service;

import dev.jos.back.dto.user.CreateUserDTO;
import dev.jos.back.dto.user.UserResponseDTO;
import dev.jos.back.exceptions.auth.UserAlreadyExistsException;
import dev.jos.back.model.User;
import dev.jos.back.repository.UserRepository;
import dev.jos.back.util.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserResponseDTO createUser(CreateUserDTO dto) {
        if (userRepository.existsByEmail(dto.email())) {
            throw new UserAlreadyExistsException("Cet email est déjà utilisé");
        }

        User user = dto.toEntity();
        user.setPasswordHash(passwordEncoder.encode(dto.password()));
        user.setRole(Role.ROLE_USER);

        User savedUser = userRepository.save(user);
        return UserResponseDTO.from(savedUser);
    }

    public UserResponseDTO getUserResponseDto(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        return UserResponseDTO.from(user);
    }
}