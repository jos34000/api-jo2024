package dev.jos.back.dto.payment;

import dev.jos.back.util.enums.PaymentMethod;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO représentant la requête de paiement transmise lors du checkout.
 *
 * @param cardNumber    le numéro de carte bancaire (16 chiffres)
 * @param expiryMonth   le mois d'expiration (1 à 12)
 * @param expiryYear    l'année d'expiration (2024 minimum)
 * @param cvv           le code de sécurité de la carte
 * @param paymentMethod la méthode de paiement choisie
 */
public record CheckoutRequestDTO(
        @NotBlank(message = "Le numéro de carte est requis")
        String cardNumber,

        @Min(value = 1, message = "Le mois d'expiration doit être entre 1 et 12")
        @Max(value = 12, message = "Le mois d'expiration doit être entre 1 et 12")
        int expiryMonth,

        @Min(value = 2024, message = "L'année d'expiration est invalide")
        int expiryYear,

        @NotBlank(message = "Le CVV est requis")
        String cvv,

        @NotNull(message = "La méthode de paiement est requise")
        PaymentMethod paymentMethod
) {}
