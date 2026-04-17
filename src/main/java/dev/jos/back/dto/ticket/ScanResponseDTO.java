package dev.jos.back.dto.ticket;

import dev.jos.back.dto.cart.CartEventSummaryDTO;
import dev.jos.back.dto.cart.CartOfferSummaryDTO;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * Résultat d'un scan de billet retourné au staff.
 *
 * @param outcome         résultat du scan : {@code SUCCESS} ou {@code ALREADY_USED}
 * @param barcode         code-barre lisible du billet (JO2024-XXXXXXXX)
 * @param status          état du billet : {@code VALID}, {@code USED}, {@code EXPIRED} ou {@code CANCELLED}
 * @param holderFirstName prénom du porteur du billet
 * @param holderLastName  nom du porteur du billet
 * @param holderEmail     email du porteur du billet
 * @param scannedAt       date de scan (null si outcome=SUCCESS et premier scan)
 * @param scannedBy       email de l'agent ayant scanné (null si premier scan)
 * @param price           prix unitaire du billet
 * @param expiryAt        date d'expiration du billet
 * @param createdAt       date de création du billet
 * @param event           résumé de l'événement associé
 * @param offer           résumé de l'offre associée
 */
@Builder
public record ScanResponseDTO(
        String outcome,
        String barcode,
        String status,
        String holderFirstName,
        String holderLastName,
        String holderEmail,
        LocalDateTime scannedAt,
        String scannedBy,
        Double price,
        LocalDateTime expiryAt,
        LocalDateTime createdAt,
        CartEventSummaryDTO event,
        CartOfferSummaryDTO offer
) {}
