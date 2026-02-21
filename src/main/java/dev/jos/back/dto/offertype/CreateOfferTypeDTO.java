package dev.jos.back.dto.offertype;

import lombok.Builder;

@Builder
public record CreateOfferTypeDTO(
        String name,
        String description,
        Double price,
        Integer numberOfTickets,
        Boolean isActive,
        Integer displayOrder
) {
}
