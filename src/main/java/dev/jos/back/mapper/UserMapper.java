package dev.jos.back.mapper;

import dev.jos.back.dto.user.CreateUserDTO;
import dev.jos.back.dto.user.UserResponseDTO;
import dev.jos.back.entities.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    public User toEntity(CreateUserDTO dto) {
        return User.builder()
                .email(dto.email())
                .firstName(dto.firstName())
                .lastName(dto.lastName())
                .mfaEnabled(dto.enableTwoFactor())
                .role(dto.role())
                .locale(dto.locale())
                .build();
    }

    public UserResponseDTO toResponseDTO(User user) {
        return UserResponseDTO.builder()
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .mfaEnabled(user.isMfaEnabled())
                .createdDate(user.getCreatedDate())
                .locale(user.getLocale())
                .role(user.getRole())
                .build();
    }
}
