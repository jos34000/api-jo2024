package dev.jos.back.dto.offer;

import lombok.Builder;

import java.util.List;

@Builder
public record OfferResponseDTO(
        Long id,
        String name,
        String description,
        Double price,
        Integer numberOfTickets,
        Boolean isActive,
        Integer displayOrder,
        List<String> features
) {
}
