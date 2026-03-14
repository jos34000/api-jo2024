package dev.jos.back.dto.cart;

import jakarta.validation.constraints.Min;

public record CartItemUpdateDTO(
        @Min(0) int quantity
) {}
