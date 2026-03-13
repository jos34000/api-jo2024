package dev.jos.back.mapper;

import dev.jos.back.dto.cart.*;
import dev.jos.back.entities.Cart;
import dev.jos.back.entities.CartItems;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CartMapper {

    public CartResponseDTO toCartResponseDTO(Cart cart) {
        List<CartItemResponseDTO> items = cart.getCartItems().stream()
                .map(this::toCartItemResponseDTO)
                .toList();

        double totalPrice = items.stream()
                .mapToDouble(CartItemResponseDTO::subtotal)
                .sum();

        int totalTickets = items.stream()
                .mapToInt(i -> i.quantity() * i.offer().numberOfTickets())
                .sum();

        return CartResponseDTO.builder()
                .id(cart.getId())
                .status(cart.getStatus())
                .expiresAt(cart.getExpiresAt())
                .totalPrice(totalPrice)
                .totalTickets(totalTickets)
                .items(items)
                .build();
    }

    private CartItemResponseDTO toCartItemResponseDTO(CartItems item) {
        double subtotal = item.getUnitPrice() * item.getQuantity();

        return CartItemResponseDTO.builder()
                .id(item.getId())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .subtotal(subtotal)
                .event(CartEventSummaryDTO.builder()
                        .id(item.getEvent().getId())
                        .name(item.getEvent().getName())
                        .eventDate(item.getEvent().getEventDate())
                        .location(item.getEvent().getLocation())
                        .city(item.getEvent().getCity())
                        .phase(item.getEvent().getPhase())
                        .build())
                .offer(CartOfferSummaryDTO.builder()
                        .id(item.getOffer().getId())
                        .name(item.getOffer().getName())
                        .numberOfTickets(item.getOffer().getNumberOfTickets())
                        .price(item.getOffer().getPrice())
                        .build())
                .build();
    }
}
