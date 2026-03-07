package dev.jos.back.dto.sport;

import dev.jos.back.util.enums.Phases;
import lombok.Builder;

import java.util.List;

@Builder
public record SportResponseDTO(String name, String description, List<Phases> phases, Integer eventCount) {
}
