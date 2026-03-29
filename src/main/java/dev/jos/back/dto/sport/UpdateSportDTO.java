package dev.jos.back.dto.sport;

import dev.jos.back.util.enums.Phases;

import java.util.List;

public record UpdateSportDTO(
        String name,
        String description,
        String icon,
        List<Phases> phases,
        List<String> places
) {
}
