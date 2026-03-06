package dev.jos.back.dto.event;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record EventResponseDTO(
        Long id,
        String name,
        String description,
        String category,
        String sport,
        String location,
        LocalDateTime eventDate,
        Integer capacity,
        Integer availableSlots,
        Boolean isActive
) {
}