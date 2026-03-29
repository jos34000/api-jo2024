package dev.jos.back.service;

import org.springframework.stereotype.Service;

@Service
public class PaymentMockService {

    public PaymentResult processPayment(String cardNumber) {
        String normalized = cardNumber.replaceAll("\\s", "");

        return switch (normalized) {
            case "4000000000000002" -> PaymentResult.failure("Carte déclinée");
            case "4000000000009995" -> PaymentResult.failure("Fonds insuffisants");
            case "4000000000000069" -> PaymentResult.failure("Carte expirée");
            default -> PaymentResult.success();
        };
    }

    public record PaymentResult(boolean succeeded, String declineReason) {
        public static PaymentResult success() {
            return new PaymentResult(true, null);
        }

        public static PaymentResult failure(String reason) {
            return new PaymentResult(false, reason);
        }
    }
}
