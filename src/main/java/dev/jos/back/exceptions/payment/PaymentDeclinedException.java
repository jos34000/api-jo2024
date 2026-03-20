package dev.jos.back.exceptions.payment;

/**
 * Exception levée lorsque le paiement est refusé par le système de traitement.
 */
public class PaymentDeclinedException extends RuntimeException {
    public PaymentDeclinedException(String message) {
        super(message);
    }
}
