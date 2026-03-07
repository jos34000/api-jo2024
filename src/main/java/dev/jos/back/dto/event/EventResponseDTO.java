package dev.jos.back.dto.event;

import dev.jos.back.util.enums.Phases;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record EventResponseDTO(
        Long id,
        String name,
        String description,
        String category,
        Phases phase,
        String location,
        LocalDateTime eventDate,
        Integer capacity,
        Integer availableSlots,
        Boolean isActive,
        String sport
) {
}