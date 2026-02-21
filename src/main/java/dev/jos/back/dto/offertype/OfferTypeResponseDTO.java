package dev.jos.back.dto.offertype;

import lombok.Builder;

@Builder
public record OfferTypeResponseDTO(
        String name,
        String description,
        Double price,
        Integer numberOfTickets,
        Boolean isActive
) {
}
