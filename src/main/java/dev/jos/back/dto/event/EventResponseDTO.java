package dev.jos.back.dto.event;

import java.time.LocalDateTime;

public record EventResponseDTO(
        Long id,
        String name,
        String description,
        String location,
        LocalDateTime eventDate,
        Integer capacity,
        Integer availableSlots,
        Boolean isActive,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}