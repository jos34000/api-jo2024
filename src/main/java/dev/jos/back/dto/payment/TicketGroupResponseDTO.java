package dev.jos.back.dto.payment;

import dev.jos.back.dto.cart.CartEventSummaryDTO;
import dev.jos.back.dto.cart.CartOfferSummaryDTO;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Représente un groupe de billets issus du même achat (même transaction, même épreuve, même offre).
 * <p>
 * Une offre Duo ou Famille génère plusieurs billets individuels ; ce DTO les réunit en une entrée
 * unique pour l'affichage dans l'historique de l'utilisateur.
 * </p>
 *
 * @param transactionId    identifiant de la transaction parente
 * @param paymentReference référence de paiement lisible
 * @param purchasedAt      date d'achat
 * @param event            résumé de l'épreuve
 * @param offer            résumé de l'offre (Solo, Duo, Famille…)
 * @param totalSeats       nombre total de places physiques dans ce groupe
 * @param totalPrice       prix total du groupe
 * @param groupStatus      statut agrégé : {@code VALID}, {@code USED} ou {@code CANCELLED}
 * @param barcodes         codes-barres des billets individuels (un par place)
 */
@Builder
public record TicketGroupResponseDTO(
        Long transactionId,
        String paymentReference,
        LocalDateTime purchasedAt,
        CartEventSummaryDTO event,
        CartOfferSummaryDTO offer,
        int totalSeats,
        double totalPrice,
        String groupStatus,
        List<String> barcodes
) {}
