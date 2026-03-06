package dev.jos.back.dto.offertype;

import java.util.List;

public record BulkOfferTypesResponseDTO(Integer created,
                                        List<OfferTypeResponseDTO> offers) {
}
