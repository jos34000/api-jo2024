package dev.jos.back.dto.event;

import java.util.List;

public record BulkEventResponseDTO(
        Integer created,
        List<EventResponseDTO> events
) {
}