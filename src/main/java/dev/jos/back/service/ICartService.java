package dev.jos.back.service;

import dev.jos.back.dto.cart.CartItemRequestDTO;
import dev.jos.back.dto.cart.CartItemUpdateDTO;
import dev.jos.back.dto.cart.CartResponseDTO;

public interface ICartService {
    CartResponseDTO getActiveCart(String email);
    CartResponseDTO addItem(String email, CartItemRequestDTO dto);
    CartResponseDTO removeItem(String email, Long itemId);
    CartResponseDTO updateItemQuantity(String email, Long itemId, CartItemUpdateDTO dto);
    CartResponseDTO clearCart(String email);
}
