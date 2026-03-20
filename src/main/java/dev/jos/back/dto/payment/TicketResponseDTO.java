package dev.jos.back.dto.payment;

import dev.jos.back.dto.cart.CartEventSummaryDTO;
import dev.jos.back.dto.cart.CartOfferSummaryDTO;
import lombok.Builder;

/**
 * DTO représentant un billet généré après un paiement réussi.
 *
 * @param id         l'identifiant technique du billet
 * @param ticketKey  la clé unique du billet (UUID)
 * @param barcode    le code-barre du billet au format JO2024-XXXXXXXX
 * @param price      le prix unitaire du billet
 * @param status     l'état du billet : {@code VALID}, {@code USED}, {@code EXPIRED} ou {@code CANCELLED}
 * @param createdAt  la date de création du billet
 * @param event      le résumé de l'événement associé
 * @param offer      le résumé de l'offre associée
 */
@Builder
public record TicketResponseDTO(
        Long id,
        String ticketKey,
        String barcode,
        Double price,
        String status,
        java.time.LocalDateTime createdAt,
        CartEventSummaryDTO event,
        CartOfferSummaryDTO offer
) {}
