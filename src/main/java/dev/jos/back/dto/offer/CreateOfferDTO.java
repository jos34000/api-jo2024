package dev.jos.back.dto.offer;

import lombok.Builder;

import java.util.List;

@Builder
public record CreateOfferDTO(
        String name,
        String description,
        Double price,
        Integer numberOfTickets,
        Boolean isActive,
        Integer displayOrder,
        List<String> features
) {
}
