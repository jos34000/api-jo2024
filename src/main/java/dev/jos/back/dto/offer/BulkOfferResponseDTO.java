package dev.jos.back.dto.offer;

import java.util.List;

public record BulkOfferResponseDTO(Integer created,
                                   List<OfferResponseDTO> offers) {
}
