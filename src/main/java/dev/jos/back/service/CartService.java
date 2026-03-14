package dev.jos.back.service;

import dev.jos.back.dto.cart.CartItemRequestDTO;
import dev.jos.back.dto.cart.CartResponseDTO;
import dev.jos.back.entities.*;
import dev.jos.back.exceptions.cart.CartItemNotFoundException;
import dev.jos.back.exceptions.cart.CartNotFoundException;
import dev.jos.back.exceptions.event.EventNotFoundException;
import dev.jos.back.exceptions.event.EventSoldOutException;
import dev.jos.back.exceptions.offertype.OfferNotFoundException;
import dev.jos.back.exceptions.user.UserNotFoundException;
import dev.jos.back.mapper.CartMapper;
import dev.jos.back.repository.*;
import dev.jos.back.util.enums.CartStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemsRepository cartItemsRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final OfferRepository offerRepository;
    private final CartMapper cartMapper;

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public CartResponseDTO getActiveCart(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Utilisateur introuvable"));

        Cart cart = cartRepository.findByUserAndStatus(user, CartStatus.ACTIVE)
                .orElseThrow(() -> new CartNotFoundException("Aucun panier actif"));

        if (LocalDateTime.now().isAfter(cart.getExpiresAt())) {
            cart.setStatus(CartStatus.ABANDONED);
            cartRepository.save(cart);
            throw new CartNotFoundException("Le panier a expiré");
        }

        return cartMapper.toCartResponseDTO(cart);
    }

    @Transactional
    public CartResponseDTO addItem(String email, CartItemRequestDTO dto) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Aucun utilisateur avec ce mail: " + email));

        Cart cart = getOrCreateActiveCart(user);

        Event event = eventRepository.findById(dto.eventId())
                .orElseThrow(() -> new EventNotFoundException("Évènement non trouvé : " + dto.eventId()));

        if (event.getAvailableSlots() <= 0) {
            throw new EventSoldOutException("Cet évènement est complet : " + event.getName());
        }

        Offer offer = offerRepository.findById(dto.offerId())
                .orElseThrow(() -> new OfferNotFoundException("Cette offre n'existe plus : " + dto.offerId()));

        Optional<CartItems> existing = cartItemsRepository.findByCartAndEventAndOffer(cart, event, offer);

        if (existing.isPresent()) {
            CartItems item = existing.get();
            item.setQuantity(item.getQuantity() + dto.quantity());
            cartItemsRepository.save(item);
        } else {
            CartItems item = new CartItems();
            item.setCart(cart);
            item.setEvent(event);
            item.setOffer(offer);
            item.setUnitPrice(offer.getPrice());
            item.setQuantity(dto.quantity());
            cartItemsRepository.save(item);
        }

        entityManager.refresh(cart);
        return cartMapper.toCartResponseDTO(cart);
    }

    @Transactional
    public CartResponseDTO removeItem(String email, Long itemId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Aucun utilisateur avec ce mail: " + email));

        CartItems item = cartItemsRepository.findById(itemId)
                .orElseThrow(() -> new CartItemNotFoundException("Article introuvable : " + itemId));

        if (!item.getCart().getUser().getId().equals(user.getId())) {
            throw new CartItemNotFoundException("Article introuvable : " + itemId);
        }

        Cart cart = item.getCart();
        cartItemsRepository.delete(item);
        entityManager.flush();
        entityManager.refresh(cart);
        return cartMapper.toCartResponseDTO(cart);
    }

    private Cart getOrCreateActiveCart(User user) {
        Optional<Cart> existing = cartRepository.findByUserAndStatus(user, CartStatus.ACTIVE);

        if (existing.isPresent()) {
            Cart cart = existing.get();
            if (LocalDateTime.now().isAfter(cart.getExpiresAt())) {
                cart.setStatus(CartStatus.ABANDONED);
                cartRepository.save(cart);
                return createNewCart(user);
            }
            return cart;
        }

        return createNewCart(user);
    }

    private Cart createNewCart(User user) {
        Cart cart = new Cart();
        cart.setUser(user);
        cart.setStatus(CartStatus.ACTIVE);
        cart.setExpiresAt(LocalDateTime.now().plusMinutes(30));
        return cartRepository.save(cart);
    }
}
