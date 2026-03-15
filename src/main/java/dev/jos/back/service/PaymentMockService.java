package dev.jos.back.service;

import org.springframework.stereotype.Service;

/**
 * Service simulant un système de traitement de paiement par carte bancaire.
 * <p>
 * Ce service est un mock de paiement destiné aux environnements de développement et de test.
 * Le résultat du paiement est déterminé par le numéro de carte fourni, sur le modèle
 * des numéros de test Stripe.
 * </p>
 *
 * <p>Numéros de test disponibles :</p>
 * <ul>
 *   <li>{@code 4242424242424242} — Paiement accepté</li>
 *   <li>{@code 5555555555554444} — Paiement accepté (Mastercard)</li>
 *   <li>{@code 4000000000000002} — Refus : carte déclinée</li>
 *   <li>{@code 4000000000009995} — Refus : fonds insuffisants</li>
 *   <li>{@code 4000000000000069} — Refus : carte expirée</li>
 *   <li>Tout autre numéro — Paiement accepté</li>
 * </ul>
 */
@Service
public class PaymentMockService {

    /**
     * Traite un paiement simulé en fonction du numéro de carte.
     *
     * @param cardNumber le numéro de carte bancaire (espaces ignorés)
     * @return un {@link PaymentResult} indiquant le succès ou l'échec avec son motif
     */
    public PaymentResult processPayment(String cardNumber) {
        String normalized = cardNumber.replaceAll("\\s", "");

        return switch (normalized) {
            case "4000000000000002" -> PaymentResult.failure("Carte déclinée");
            case "4000000000009995" -> PaymentResult.failure("Fonds insuffisants");
            case "4000000000000069" -> PaymentResult.failure("Carte expirée");
            default -> PaymentResult.success();
        };
    }

    /**
     * Résultat d'un traitement de paiement simulé.
     *
     * @param succeeded     {@code true} si le paiement est accepté
     * @param declineReason motif du refus, {@code null} si le paiement est accepté
     */
    public record PaymentResult(boolean succeeded, String declineReason) {

        /**
         * Crée un résultat de paiement accepté.
         */
        public static PaymentResult success() {
            return new PaymentResult(true, null);
        }

        /**
         * Crée un résultat de paiement refusé avec le motif associé.
         *
         * @param reason le motif du refus
         */
        public static PaymentResult failure(String reason) {
            return new PaymentResult(false, reason);
        }
    }
}
