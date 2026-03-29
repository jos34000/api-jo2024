package dev.jos.back.dto.offer;

import java.util.List;

public record UpdateOfferDTO(
        String name,
        String description,
        Double price,
        Integer numberOfTickets,
        Boolean isActive,
        Integer displayOrder,
        List<String> features
) {
}
