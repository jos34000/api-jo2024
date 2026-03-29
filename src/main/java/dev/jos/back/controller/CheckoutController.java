package dev.jos.back.controller;

import dev.jos.back.dto.payment.CheckoutRequestDTO;
import dev.jos.back.dto.payment.TicketGroupResponseDTO;
import dev.jos.back.dto.payment.TransactionResponseDTO;
import dev.jos.back.exceptions.cart.CartNotFoundException;
import dev.jos.back.exceptions.payment.CartAlreadyConvertedException;
import dev.jos.back.exceptions.payment.CartEmptyException;
import dev.jos.back.exceptions.payment.PaymentDeclinedException;
import dev.jos.back.exceptions.payment.TransactionNotFoundException;
import dev.jos.back.service.ICheckoutService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Contrôleur REST gérant le tunnel de paiement et la consultation des billets.
 * Expose les endpoints de traitement du paiement, de récupération du détail
 * d'une transaction et de la liste des billets de l'utilisateur connecté.
 * L'ensemble des endpoints requiert une authentification valide.
 *
 * @see ICheckoutService
 */
@RestController
@RequestMapping("/api/checkout")
@RequiredArgsConstructor
public class CheckoutController {

    private final ICheckoutService transactionService;

    /**
     * Traite le paiement du panier actif de l'utilisateur authentifié.
     * Le résultat dépend du numéro de carte fourni (système mock) :<br>
     * {@code 4242424242424242} → accepté, {@code 4000000000000002} → refusé, etc.
     *
     * @param authentication l'objet d'authentification Spring Security injecté automatiquement
     * @param dto            les données de paiement (numéro de carte, expiry, CVV, méthode)
     * @return {@code ResponseEntity<TransactionResponseDTO>} contenant la transaction créée
     * et les billets générés (201 CREATED)
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
     * Retourne l'historique des billets de l'utilisateur authentifié, regroupés par achat.
     * Les billets issus d'une même offre Duo ou Famille sont réunis en une seule entrée.
     * Le statut du groupe est {@code USED} si toutes les places ont été scannées, {@code VALID} sinon.
     *
     * @param authentication l'objet d'authentification Spring Security injecté automatiquement
     * @return {@code ResponseEntity<List<TicketGroupResponseDTO>>} la liste des groupes (200 OK)
     */
    @GetMapping("/tickets")
    public ResponseEntity<List<TicketGroupResponseDTO>> getUserTickets(Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(transactionService.getUserTicketGroups(email));
    }

    /**
     * Récupère les billets déjà achetés
     * L'utilisateur ne peut accéder qu'à ses propres billets.
     *
     * @param authentication l'objet d'authentification Spring Security injecté automatiquement
     * @param transactionId  l'identifiant de la transaction à récupérer
     * @return {@code ResponseEntity<byte[]>} contenant les billets (200 OK)
     */
    @GetMapping("/{transactionId}/pdf")
    public ResponseEntity<byte[]> downloadTicketsPdf(
            Authentication authentication,
            @PathVariable Long transactionId
    ) {
        String email = authentication.getName();
        byte[] pdf = transactionService.getTicketsPdf(email, transactionId);
        String filename = "billets-" + transactionId + ".pdf";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    /**
     * Récupère le détail d'une transaction par son identifiant pour la page de confirmation.
     * L'utilisateur ne peut accéder qu'à ses propres transactions.
     *
     * @param authentication l'objet d'authentification Spring Security injecté automatiquement
     * @param transactionId  l'identifiant de la transaction à récupérer
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
