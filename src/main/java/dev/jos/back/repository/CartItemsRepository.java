package dev.jos.back.repository;

import dev.jos.back.entities.Cart;
import dev.jos.back.entities.CartItems;
import dev.jos.back.entities.Event;
import dev.jos.back.entities.Offer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartItemsRepository extends JpaRepository<CartItems, Long> {
    Optional<CartItems> findByCartAndEventAndOffer(Cart cart, Event event, Offer offer);
}
