package dev.jos.back.dto.cart;

import dev.jos.back.util.enums.CartStatus;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record CartResponseDTO(
        Long id,
        CartStatus status,
        LocalDateTime expiresAt,
        Double totalPrice,
        Integer totalTickets,
        List<CartItemResponseDTO> items
) {}
