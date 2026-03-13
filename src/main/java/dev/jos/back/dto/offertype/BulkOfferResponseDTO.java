package dev.jos.back.dto.offertype;

import java.util.List;

public record BulkOfferResponseDTO(Integer created,
                                   List<OfferResponseDTO> offers) {
}
