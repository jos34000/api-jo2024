package dev.jos.back.controller;

import dev.jos.back.dto.cart.CartResponseDTO;
import dev.jos.back.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Contrôleur REST pour la gestion du panier d'achat.
 * Fournit des endpoints pour consulter le panier de l'utilisateur authentifié.
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
     * et une réponse 404 est renvoyée.</p>
     *
     * @param authentication l'objet d'authentification Spring Security injecté automatiquement,
     *                       contenant l'email de l'utilisateur extrait du JWT
     * @return {@code ResponseEntity<CartResponseDTO>} contenant le panier actif avec ses articles,
     * les prix unitaires, les sous-totaux, le prix total et le nombre total de billets
     * @throws dev.jos.back.exceptions.cart.CartNotFoundException si l'utilisateur n'a pas de panier
     *                                                            actif ou si son panier a expiré
     */
    @GetMapping
    public ResponseEntity<CartResponseDTO> getActiveCart(Authentication authentication) {
        String email = authentication.getName();
        CartResponseDTO cart = cartService.getActiveCart(email);
        return ResponseEntity.ok(cart);
    }
}
