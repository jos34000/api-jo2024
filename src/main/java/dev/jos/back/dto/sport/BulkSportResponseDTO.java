package dev.jos.back.dto.sport;

import java.util.List;

public record BulkSportResponseDTO(List<SportResponseDTO> sportsDto, Integer created) {
}
