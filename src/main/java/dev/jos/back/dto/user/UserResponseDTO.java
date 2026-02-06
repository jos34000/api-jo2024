package dev.jos.back.dto.user;

import dev.jos.back.model.User;

import java.time.LocalDateTime;

public record UserResponseDTO(
        Long id,
        String email,
        String firstName,
        String lastName,
        boolean mfaEnabled,
        LocalDateTime createdDate
) {

    public static UserResponseDTO from(User user) {
        return new UserResponseDTO(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.isMfaEnabled(),
                user.getCreatedDate()
        );
    }
}