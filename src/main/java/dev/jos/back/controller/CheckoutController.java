package dev.jos.back.controller;

import dev.jos.back.dto.payment.CheckoutRequestDTO;
import dev.jos.back.dto.payment.TransactionResponseDTO;
import dev.jos.back.exceptions.cart.CartNotFoundException;
import dev.jos.back.exceptions.payment.CartAlreadyConvertedException;
import dev.jos.back.exceptions.payment.CartEmptyException;
import dev.jos.back.exceptions.payment.PaymentDeclinedException;
import dev.jos.back.exceptions.payment.TransactionNotFoundException;
import dev.jos.back.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Contrôleur REST gérant le tunnel de paiement.
 * <p>
 * Expose les endpoints de traitement du paiement du panier actif et de récupération
 * du détail d'une transaction pour la page de confirmation.
 * L'ensemble des endpoints requiert une authentification valide.
 * </p>
 *
 * @see TransactionService
 * @author jos
 */
@RestController
@RequestMapping("/api/checkout")
@RequiredArgsConstructor
public class CheckoutController {

    private final TransactionService transactionService;

    /**
     * Traite le paiement du panier actif de l'utilisateur authentifié.
     * <p>
     * Le résultat dépend du numéro de carte fourni (système mock) :<br>
     * {@code 4242424242424242} → accepté, {@code 4000000000000002} → refusé, etc.
     * </p>
     *
     * @param authentication l'objet d'authentification Spring Security injecté automatiquement
     * @param dto            les données de paiement (numéro de carte, expiry, CVV, méthode)
     * @return {@code ResponseEntity<TransactionResponseDTO>} contenant la transaction créée
     *         et les billets générés (201 CREATED)
     * @throws CartNotFoundException         si aucun panier actif n'existe ou si le panier a expiré
     * @throws CartEmptyException            si le panier est vide
     * @throws CartAlreadyConvertedException si le panier a déjà été converti en commande
     * @throws PaymentDeclinedException      si le paiement est refusé par le système mock
     */
    @PostMapping
    public ResponseEntity<TransactionResponseDTO> checkout(
            Authentication authentication,
            @Valid @RequestBody CheckoutRequestDTO dto
    ) {
        String email = authentication.getName();
        TransactionResponseDTO transaction = transactionService.checkout(email, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(transaction);
    }

    /**
     * Récupère le détail d'une transaction par son identifiant pour la page de confirmation.
     * <p>
     * L'utilisateur ne peut accéder qu'à ses propres transactions.
     * </p>
     *
     * @param authentication  l'objet d'authentification Spring Security injecté automatiquement
     * @param transactionId   l'identifiant de la transaction à récupérer
     * @return {@code ResponseEntity<TransactionResponseDTO>} contenant la transaction et ses billets (200 OK)
     * @throws TransactionNotFoundException si la transaction est introuvable ou n'appartient pas à l'utilisateur
     */
    @GetMapping("/{transactionId}")
    public ResponseEntity<TransactionResponseDTO> getTransaction(
            Authentication authentication,
            @PathVariable Long transactionId
    ) {
        String email = authentication.getName();
        TransactionResponseDTO transaction = transactionService.getTransaction(email, transactionId);
        return ResponseEntity.ok(transaction);
    }
}
