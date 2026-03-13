package dev.jos.back.dto.cart;

import lombok.Builder;

@Builder
public record CartItemResponseDTO(
        Long id,
        Integer quantity,
        Double unitPrice,
        Double subtotal,
        CartEventSummaryDTO event,
        CartOfferSummaryDTO offer
) {}
