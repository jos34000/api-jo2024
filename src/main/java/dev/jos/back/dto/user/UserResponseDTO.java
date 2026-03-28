package dev.jos.back.dto.user;

import dev.jos.back.util.enums.Role;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record UserResponseDTO(
        String email,
        String firstName,
        String lastName,
        boolean mfaEnabled,
        String locale,
        LocalDateTime createdDate,
        Role role
) {
}