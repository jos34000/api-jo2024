package dev.jos.back.dto.user;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record UserResponseDTO(
        String email,
        String firstName,
        String lastName,
        boolean mfaEnabled,
        LocalDateTime createdDate
) {
}