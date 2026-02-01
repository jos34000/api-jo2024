package dev.jos.back.dto;

import java.time.LocalDateTime;

public record ErrorResponseDTO(
        int status,
        String message,
        LocalDateTime timestamp
) {
    public static ErrorResponseDTO of(int status, String message) {
        return new ErrorResponseDTO(status, message, LocalDateTime.now());
    }
}
