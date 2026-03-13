package dev.jos.back.dto.offertype;

import lombok.Builder;

@Builder
public record OfferResponseDTO(
        String name,
        String description,
        Double price,
        Integer numberOfTickets,
        Boolean isActive
) {
}
