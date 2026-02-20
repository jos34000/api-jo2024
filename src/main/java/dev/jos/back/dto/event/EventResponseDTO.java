package dev.jos.back.dto.event;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record EventResponseDTO(
        String name,
        String description,
        String location,
        LocalDateTime eventDate,
        Integer capacity,
        Integer availableSlots,
        Boolean isActive
) {
}