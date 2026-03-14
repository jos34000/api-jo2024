package dev.jos.back.dto.cart;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CartItemRequestDTO(
        @NotNull Long eventId,
        @NotNull Long offerId,
        @Min(1) int quantity
) {}
