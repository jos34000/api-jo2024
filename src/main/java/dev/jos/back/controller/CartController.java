package dev.jos.back.controller;

import dev.jos.back.dto.cart.CartItemRequestDTO;
import dev.jos.back.dto.cart.CartResponseDTO;
import dev.jos.back.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Contrôleur REST pour la gestion du panier d'achat.
 * Fournit des endpoints pour consulter et modifier le panier de l'utilisateur authentifié.
 *
 * @see CartService
 */
@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    /**
     * Récupère le panier actif de l'utilisateur authentifié avec l'ensemble de ses articles.
     * Le panier retourné est celui dont le statut est {@code ACTIVE}. Si le panier
     * est expiré au moment de la requête, il est automatiquement marqué {@code ABANDONED}
     * et une réponse 404 est renvoyée.
     *
     * @param authentication l'objet d'authentification Spring Security injecté automatiquement
     * @return {@code ResponseEntity<CartResponseDTO>} contenant le panier actif
     */
    @GetMapping
    public ResponseEntity<CartResponseDTO> getActiveCart(Authentication authentication) {
        String email = authentication.getName();
        CartResponseDTO cart = cartService.getActiveCart(email);
        return ResponseEntity.ok(cart);
    }

    /**
     * Ajoute un article au panier de l'utilisateur authentifié.
     * Si l'utilisateur n'a pas de panier actif, un nouveau est créé (expiration 30 min).
     * Si le même couple event/offer est déjà dans le panier, la quantité est incrémentée.
     *
     * @param authentication l'objet d'authentification Spring Security
     * @param dto            l'article à ajouter (eventId, offerId, quantity)
     * @return {@code ResponseEntity<CartResponseDTO>} contenant le panier mis à jour
     */
    @PostMapping("/items")
    public ResponseEntity<CartResponseDTO> addItem(
            Authentication authentication,
            @Valid @RequestBody CartItemRequestDTO dto
    ) {
        String email = authentication.getName();
        CartResponseDTO cart = cartService.addItem(email, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(cart);
    }
}
