package dev.jos.back.dto.cart;

import lombok.Builder;

@Builder
public record CartOfferSummaryDTO(
        Long id,
        String name,
        Integer numberOfTickets,
        Double price
) {}
