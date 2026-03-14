package dev.jos.back.repository;

import dev.jos.back.entities.Cart;
import dev.jos.back.entities.User;
import dev.jos.back.util.enums.CartStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findByUserAndStatus(User user, CartStatus status);

    @Query("SELECT c FROM Cart c LEFT JOIN FETCH c.cartItems WHERE c.id = :id")
    Optional<Cart> findByIdWithItems(@Param("id") Long id);
}
