package dev.jos.back.dto.cart;

import dev.jos.back.util.enums.Phases;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record CartEventSummaryDTO(
        Long id,
        String name,
        LocalDateTime eventDate,
        String location,
        String city,
        Phases phase
) {}
