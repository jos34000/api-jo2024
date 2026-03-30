package dev.jos.back.service;

import dev.jos.back.dto.cart.CartItemRequestDTO;
import dev.jos.back.dto.cart.CartItemUpdateDTO;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock CartRepository cartRepository;
    @Mock CartItemsRepository cartItemsRepository;
    @Mock UserRepository userRepository;
    @Mock EventRepository eventRepository;
    @Mock OfferRepository offerRepository;
    @Mock CartMapper cartMapper;
    @InjectMocks CartService cartService;

    // ── getActiveCart ─────────────────────────────────────────────────────────

    @Test
    void getActiveCart_returnsDTO_whenCartIsActive() {
        User user = new User();
        user.setId(1L);
        user.setEmail("test@test.com");

        Cart cart = new Cart();
        cart.setId(1L);
        cart.setStatus(CartStatus.ACTIVE);
        cart.setExpiresAt(LocalDateTime.now().plusMinutes(30));

        CartResponseDTO expected = CartResponseDTO.builder().id(1L).status(CartStatus.ACTIVE).build();

        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(cartRepository.findByUserAndStatus(user, CartStatus.ACTIVE)).thenReturn(Optional.of(cart));
        when(cartMapper.toCartResponseDTO(cart)).thenReturn(expected);

        CartResponseDTO result = cartService.getActiveCart("test@test.com");

        assertThat(result.id()).isEqualTo(1L);
    }

    @Test
    void getActiveCart_throwsUserNotFoundException_whenUserNotFound() {
        when(userRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.getActiveCart("unknown@test.com"))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void getActiveCart_throwsCartNotFoundException_whenNoActiveCart() {
        User user = new User();
        user.setId(1L);
        user.setEmail("test@test.com");

        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(cartRepository.findByUserAndStatus(user, CartStatus.ACTIVE)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.getActiveCart("test@test.com"))
                .isInstanceOf(CartNotFoundException.class);
    }

    @Test
    void getActiveCart_throwsCartNotFoundException_whenCartExpired() {
        User user = new User();
        user.setId(1L);
        user.setEmail("test@test.com");

        Cart cart = new Cart();
        cart.setId(1L);
        cart.setStatus(CartStatus.ACTIVE);
        cart.setExpiresAt(LocalDateTime.now().minusMinutes(1));

        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(cartRepository.findByUserAndStatus(user, CartStatus.ACTIVE)).thenReturn(Optional.of(cart));
        when(cartRepository.save(cart)).thenReturn(cart);

        assertThatThrownBy(() -> cartService.getActiveCart("test@test.com"))
                .isInstanceOf(CartNotFoundException.class);
    }

    // ── addItem ───────────────────────────────────────────────────────────────

    @Test
    void addItem_savesNewItem_whenNoExistingItem() {
        User user = new User();
        user.setId(1L);
        user.setEmail("test@test.com");

        Cart cart = new Cart();
        cart.setId(1L);
        cart.setStatus(CartStatus.ACTIVE);
        cart.setExpiresAt(LocalDateTime.now().plusMinutes(30));

        Event event = new Event();
        event.setId(10L);
        event.setAvailableSlots(100);

        Offer offer = new Offer();
        offer.setId(20L);
        offer.setPrice(50.0);

        CartItemRequestDTO dto = new CartItemRequestDTO(10L, 20L, 2);
        CartResponseDTO expected = CartResponseDTO.builder().id(1L).build();

        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(cartRepository.findByUserAndStatus(user, CartStatus.ACTIVE)).thenReturn(Optional.of(cart));
        when(eventRepository.findById(10L)).thenReturn(Optional.of(event));
        when(offerRepository.findById(20L)).thenReturn(Optional.of(offer));
        when(cartItemsRepository.findByCartAndEventAndOffer(cart, event, offer)).thenReturn(Optional.empty());
        when(cartRepository.findByIdWithItems(1L)).thenReturn(Optional.of(cart));
        when(cartMapper.toCartResponseDTO(cart)).thenReturn(expected);

        CartResponseDTO result = cartService.addItem("test@test.com", dto);

        assertThat(result.id()).isEqualTo(1L);
        verify(cartItemsRepository).save(any(CartItems.class));
    }

    @Test
    void addItem_incrementsQuantity_whenItemAlreadyInCart() {
        User user = new User();
        user.setId(1L);
        user.setEmail("test@test.com");

        Cart cart = new Cart();
        cart.setId(1L);
        cart.setStatus(CartStatus.ACTIVE);
        cart.setExpiresAt(LocalDateTime.now().plusMinutes(30));

        Event event = new Event();
        event.setId(10L);
        event.setAvailableSlots(100);

        Offer offer = new Offer();
        offer.setId(20L);
        offer.setPrice(50.0);

        CartItems existingItem = new CartItems();
        existingItem.setId(100L);
        existingItem.setQuantity(3);

        CartItemRequestDTO dto = new CartItemRequestDTO(10L, 20L, 2);
        CartResponseDTO expected = CartResponseDTO.builder().id(1L).build();

        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(cartRepository.findByUserAndStatus(user, CartStatus.ACTIVE)).thenReturn(Optional.of(cart));
        when(eventRepository.findById(10L)).thenReturn(Optional.of(event));
        when(offerRepository.findById(20L)).thenReturn(Optional.of(offer));
        when(cartItemsRepository.findByCartAndEventAndOffer(cart, event, offer)).thenReturn(Optional.of(existingItem));
        when(cartRepository.findByIdWithItems(1L)).thenReturn(Optional.of(cart));
        when(cartMapper.toCartResponseDTO(cart)).thenReturn(expected);

        cartService.addItem("test@test.com", dto);

        assertThat(existingItem.getQuantity()).isEqualTo(5);
        verify(cartItemsRepository).save(existingItem);
    }

    @Test
    void addItem_throwsUserNotFoundException() {
        when(userRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());

        CartItemRequestDTO dto = new CartItemRequestDTO(10L, 20L, 1);

        assertThatThrownBy(() -> cartService.addItem("unknown@test.com", dto))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void addItem_throwsEventNotFoundException() {
        User user = new User();
        user.setId(1L);
        user.setEmail("test@test.com");

        Cart cart = new Cart();
        cart.setId(1L);
        cart.setStatus(CartStatus.ACTIVE);
        cart.setExpiresAt(LocalDateTime.now().plusMinutes(30));

        CartItemRequestDTO dto = new CartItemRequestDTO(99L, 20L, 1);

        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(cartRepository.findByUserAndStatus(user, CartStatus.ACTIVE)).thenReturn(Optional.of(cart));
        when(eventRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.addItem("test@test.com", dto))
                .isInstanceOf(EventNotFoundException.class);
    }

    @Test
    void addItem_throwsEventSoldOutException_whenNoSlotsAvailable() {
        User user = new User();
        user.setId(1L);
        user.setEmail("test@test.com");

        Cart cart = new Cart();
        cart.setId(1L);
        cart.setStatus(CartStatus.ACTIVE);
        cart.setExpiresAt(LocalDateTime.now().plusMinutes(30));

        Event event = new Event();
        event.setId(10L);
        event.setAvailableSlots(0);

        CartItemRequestDTO dto = new CartItemRequestDTO(10L, 20L, 1);

        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(cartRepository.findByUserAndStatus(user, CartStatus.ACTIVE)).thenReturn(Optional.of(cart));
        when(eventRepository.findById(10L)).thenReturn(Optional.of(event));

        assertThatThrownBy(() -> cartService.addItem("test@test.com", dto))
                .isInstanceOf(EventSoldOutException.class);
    }

    @Test
    void addItem_throwsOfferNotFoundException() {
        User user = new User();
        user.setId(1L);
        user.setEmail("test@test.com");

        Cart cart = new Cart();
        cart.setId(1L);
        cart.setStatus(CartStatus.ACTIVE);
        cart.setExpiresAt(LocalDateTime.now().plusMinutes(30));

        Event event = new Event();
        event.setId(10L);
        event.setAvailableSlots(100);

        CartItemRequestDTO dto = new CartItemRequestDTO(10L, 99L, 1);

        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(cartRepository.findByUserAndStatus(user, CartStatus.ACTIVE)).thenReturn(Optional.of(cart));
        when(eventRepository.findById(10L)).thenReturn(Optional.of(event));
        when(offerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.addItem("test@test.com", dto))
                .isInstanceOf(OfferNotFoundException.class);
    }

    @Test
    void addItem_createsNewCart_whenNoActiveCartExists() {
        User user = new User();
        user.setId(1L);
        user.setEmail("test@test.com");

        Cart newCart = new Cart();
        newCart.setId(1L);
        newCart.setStatus(CartStatus.ACTIVE);
        newCart.setExpiresAt(LocalDateTime.now().plusMinutes(30));

        Event event = new Event();
        event.setId(10L);
        event.setAvailableSlots(100);

        Offer offer = new Offer();
        offer.setId(20L);
        offer.setPrice(50.0);

        CartItemRequestDTO dto = new CartItemRequestDTO(10L, 20L, 1);
        CartResponseDTO expected = CartResponseDTO.builder().id(1L).build();

        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(cartRepository.findByUserAndStatus(user, CartStatus.ACTIVE)).thenReturn(Optional.empty());
        when(cartRepository.save(any(Cart.class))).thenReturn(newCart);
        when(eventRepository.findById(10L)).thenReturn(Optional.of(event));
        when(offerRepository.findById(20L)).thenReturn(Optional.of(offer));
        when(cartItemsRepository.findByCartAndEventAndOffer(newCart, event, offer)).thenReturn(Optional.empty());
        when(cartRepository.findByIdWithItems(1L)).thenReturn(Optional.of(newCart));
        when(cartMapper.toCartResponseDTO(newCart)).thenReturn(expected);

        CartResponseDTO result = cartService.addItem("test@test.com", dto);

        assertThat(result.id()).isEqualTo(1L);
        verify(cartRepository).save(any(Cart.class));
    }

    // ── removeItem ────────────────────────────────────────────────────────────

    @Test
    void removeItem_deletesItemAndReturnsDTO() {
        User user = new User();
        user.setId(1L);
        user.setEmail("test@test.com");

        Cart cart = new Cart();
        cart.setId(1L);
        cart.setUser(user);

        CartItems item = new CartItems();
        item.setId(100L);
        item.setCart(cart);

        CartResponseDTO expected = CartResponseDTO.builder().id(1L).build();

        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(cartItemsRepository.findById(100L)).thenReturn(Optional.of(item));
        when(cartRepository.findByIdWithItems(1L)).thenReturn(Optional.of(cart));
        when(cartMapper.toCartResponseDTO(cart)).thenReturn(expected);

        CartResponseDTO result = cartService.removeItem("test@test.com", 100L);

        assertThat(result.id()).isEqualTo(1L);
        verify(cartItemsRepository).delete(item);
    }

    @Test
    void removeItem_throwsUserNotFoundException() {
        when(userRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.removeItem("unknown@test.com", 100L))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void removeItem_throwsCartItemNotFoundException_whenItemNotFound() {
        User user = new User();
        user.setId(1L);
        user.setEmail("test@test.com");

        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(cartItemsRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.removeItem("test@test.com", 999L))
                .isInstanceOf(CartItemNotFoundException.class);
    }

    @Test
    void removeItem_throwsCartItemNotFoundException_whenItemBelongsToOtherUser() {
        User user = new User();
        user.setId(1L);
        user.setEmail("test@test.com");

        User otherUser = new User();
        otherUser.setId(2L);

        Cart cart = new Cart();
        cart.setId(1L);
        cart.setUser(otherUser);

        CartItems item = new CartItems();
        item.setId(100L);
        item.setCart(cart);

        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(cartItemsRepository.findById(100L)).thenReturn(Optional.of(item));

        assertThatThrownBy(() -> cartService.removeItem("test@test.com", 100L))
                .isInstanceOf(CartItemNotFoundException.class);
    }

    // ── updateItemQuantity ────────────────────────────────────────────────────

    @Test
    void updateItemQuantity_updatesQuantity_whenPositive() {
        User user = new User();
        user.setId(1L);
        user.setEmail("test@test.com");

        Cart cart = new Cart();
        cart.setId(1L);
        cart.setUser(user);

        CartItems item = new CartItems();
        item.setId(100L);
        item.setQuantity(2);
        item.setCart(cart);

        CartItemUpdateDTO dto = new CartItemUpdateDTO(5);
        CartResponseDTO expected = CartResponseDTO.builder().id(1L).build();

        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(cartItemsRepository.findById(100L)).thenReturn(Optional.of(item));
        when(cartRepository.findByIdWithItems(1L)).thenReturn(Optional.of(cart));
        when(cartMapper.toCartResponseDTO(cart)).thenReturn(expected);

        CartResponseDTO result = cartService.updateItemQuantity("test@test.com", 100L, dto);

        assertThat(result.id()).isEqualTo(1L);
        assertThat(item.getQuantity()).isEqualTo(5);
        verify(cartItemsRepository).save(item);
    }

    @Test
    void updateItemQuantity_deletesItem_whenQuantityIsZero() {
        User user = new User();
        user.setId(1L);
        user.setEmail("test@test.com");

        Cart cart = new Cart();
        cart.setId(1L);
        cart.setUser(user);

        CartItems item = new CartItems();
        item.setId(100L);
        item.setQuantity(3);
        item.setCart(cart);

        CartItemUpdateDTO dto = new CartItemUpdateDTO(0);
        CartResponseDTO expected = CartResponseDTO.builder().id(1L).build();

        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(cartItemsRepository.findById(100L)).thenReturn(Optional.of(item));
        when(cartRepository.findByIdWithItems(1L)).thenReturn(Optional.of(cart));
        when(cartMapper.toCartResponseDTO(cart)).thenReturn(expected);

        cartService.updateItemQuantity("test@test.com", 100L, dto);

        verify(cartItemsRepository).delete(item);
    }

    @Test
    void updateItemQuantity_deletesItem_whenQuantityIsNegative() {
        User user = new User();
        user.setId(1L);
        user.setEmail("test@test.com");

        Cart cart = new Cart();
        cart.setId(1L);
        cart.setUser(user);

        CartItems item = new CartItems();
        item.setId(100L);
        item.setQuantity(3);
        item.setCart(cart);

        CartItemUpdateDTO dto = new CartItemUpdateDTO(-1);
        CartResponseDTO expected = CartResponseDTO.builder().id(1L).build();

        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(cartItemsRepository.findById(100L)).thenReturn(Optional.of(item));
        when(cartRepository.findByIdWithItems(1L)).thenReturn(Optional.of(cart));
        when(cartMapper.toCartResponseDTO(cart)).thenReturn(expected);

        cartService.updateItemQuantity("test@test.com", 100L, dto);

        verify(cartItemsRepository).delete(item);
    }

    @Test
    void updateItemQuantity_throwsUserNotFoundException() {
        when(userRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.updateItemQuantity("unknown@test.com", 100L, new CartItemUpdateDTO(2)))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void updateItemQuantity_throwsCartItemNotFoundException_whenItemNotFound() {
        User user = new User();
        user.setId(1L);
        user.setEmail("test@test.com");

        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(cartItemsRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.updateItemQuantity("test@test.com", 999L, new CartItemUpdateDTO(2)))
                .isInstanceOf(CartItemNotFoundException.class);
    }

    @Test
    void updateItemQuantity_throwsCartItemNotFoundException_whenItemBelongsToOtherUser() {
        User user = new User();
        user.setId(1L);
        user.setEmail("test@test.com");

        User otherUser = new User();
        otherUser.setId(2L);

        Cart cart = new Cart();
        cart.setId(1L);
        cart.setUser(otherUser);

        CartItems item = new CartItems();
        item.setId(100L);
        item.setCart(cart);

        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(cartItemsRepository.findById(100L)).thenReturn(Optional.of(item));

        assertThatThrownBy(() -> cartService.updateItemQuantity("test@test.com", 100L, new CartItemUpdateDTO(2)))
                .isInstanceOf(CartItemNotFoundException.class);
    }

    // ── clearCart ─────────────────────────────────────────────────────────────

    @Test
    void clearCart_deletesAllItemsAndReturnsDTO() {
        User user = new User();
        user.setId(1L);
        user.setEmail("test@test.com");

        Cart cart = new Cart();
        cart.setId(1L);
        cart.setStatus(CartStatus.ACTIVE);
        cart.setExpiresAt(LocalDateTime.now().plusMinutes(30));

        CartResponseDTO expected = CartResponseDTO.builder().id(1L).build();

        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(cartRepository.findByUserAndStatus(user, CartStatus.ACTIVE)).thenReturn(Optional.of(cart));
        when(cartRepository.findByIdWithItems(1L)).thenReturn(Optional.of(cart));
        when(cartMapper.toCartResponseDTO(cart)).thenReturn(expected);

        CartResponseDTO result = cartService.clearCart("test@test.com");

        assertThat(result.id()).isEqualTo(1L);
        verify(cartItemsRepository).deleteAll(cart.getCartItems());
    }

    @Test
    void clearCart_throwsUserNotFoundException() {
        when(userRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.clearCart("unknown@test.com"))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void clearCart_throwsCartNotFoundException_whenNoActiveCart() {
        User user = new User();
        user.setId(1L);
        user.setEmail("test@test.com");

        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(cartRepository.findByUserAndStatus(user, CartStatus.ACTIVE)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.clearCart("test@test.com"))
                .isInstanceOf(CartNotFoundException.class);
    }
}
