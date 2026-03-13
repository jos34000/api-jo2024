package dev.jos.back.dto.sport;

import dev.jos.back.util.enums.Phases;
import lombok.Builder;

import java.util.List;

@Builder
public record SportResponseDTO(Long id, String name, String description, String icon, List<Phases> phases,
                               Integer eventCount,
                               List<String> places) {
}
