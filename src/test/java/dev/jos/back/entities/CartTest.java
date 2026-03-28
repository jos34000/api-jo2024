package dev.jos.back.entities;

import dev.jos.back.util.enums.CartStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class CartTest {

    @Test
    void expireIfNeeded_returnsTrue_whenExpired() {
        Cart cart = new Cart();
        cart.setStatus(CartStatus.ACTIVE);
        cart.setExpiresAt(LocalDateTime.now().minusMinutes(1));

        boolean expired = cart.expireIfNeeded();

        assertThat(expired).isTrue();
        assertThat(cart.getStatus()).isEqualTo(CartStatus.ABANDONED);
    }

    @Test
    void expireIfNeeded_returnsFalse_whenNotExpired() {
        Cart cart = new Cart();
        cart.setStatus(CartStatus.ACTIVE);
        cart.setExpiresAt(LocalDateTime.now().plusMinutes(30));

        boolean expired = cart.expireIfNeeded();

        assertThat(expired).isFalse();
        assertThat(cart.getStatus()).isEqualTo(CartStatus.ACTIVE);
    }

    @Test
    void expireIfNeeded_returnsFalse_whenExpiresAtIsNull() {
        Cart cart = new Cart();
        cart.setStatus(CartStatus.ACTIVE);
        cart.setExpiresAt(null);

        boolean expired = cart.expireIfNeeded();

        assertThat(expired).isFalse();
        assertThat(cart.getStatus()).isEqualTo(CartStatus.ACTIVE);
    }
}
