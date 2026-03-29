package dev.jos.back.dto.event;

import dev.jos.back.util.enums.Phases;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record UpdateEventDTO(
        @Size(min = 3, max = 200) String name,
        @Size(min = 10, max = 2000) String description,
        String icon,
        @Size(min = 2, max = 2000) String category,
        Phases phase,
        @Size(min = 3, max = 200) String location,
        @Size(min = 3, max = 200) String city,
        String sport,
        LocalDateTime eventDate,
        @Min(1) @Max(500000) Integer capacity,
        @Min(0) Integer availableSlots,
        Boolean isActive
) {
}
