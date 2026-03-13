package dev.jos.back.repository;

import dev.jos.back.entities.Cart;
import dev.jos.back.entities.User;
import dev.jos.back.util.enums.CartStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findByUserAndStatus(User user, CartStatus status);
}
