package dev.jos.back.service;

import dev.jos.back.dto.cart.CartResponseDTO;
import dev.jos.back.entities.Cart;
import dev.jos.back.entities.User;
import dev.jos.back.exceptions.cart.CartNotFoundException;
import dev.jos.back.exceptions.user.UserNotFoundException;
import dev.jos.back.mapper.CartMapper;
import dev.jos.back.repository.CartRepository;
import dev.jos.back.repository.UserRepository;
import dev.jos.back.util.enums.CartStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final CartMapper cartMapper;

    @Transactional
    public CartResponseDTO getActiveCart(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + email));

        Cart cart = cartRepository.findByUserAndStatus(user, CartStatus.ACTIVE)
                .orElseThrow(() -> new CartNotFoundException("No active cart found"));

        if (LocalDateTime.now().isAfter(cart.getExpiresAt())) {
            cart.setStatus(CartStatus.ABANDONED);
            cartRepository.save(cart);
            throw new CartNotFoundException("Cart has expired");
        }

        return cartMapper.toCartResponseDTO(cart);
    }
}
