package dev.jos.back.dto.payment;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO représentant le résultat d'une transaction de paiement.
 *
 * @param id               l'identifiant technique de la transaction
 * @param transactionKey   la clé UUID unique de la transaction
 * @param status           le statut de la transaction (COMPLETED ou FAILED)
 * @param amount           le montant total payé
 * @param paymentReference la référence de paiement lisible (ex: REF-A1B2C3D4)
 * @param payedDate        la date et heure du paiement (null si FAILED)
 * @param tickets          la liste des billets générés (vide si FAILED)
 */
@Builder
public record TransactionResponseDTO(
        Long id,
        String transactionKey,
        String status,
        BigDecimal amount,
        String paymentReference,
        LocalDateTime payedDate,
        List<TicketResponseDTO> tickets
) {}
