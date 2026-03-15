package dev.jos.back.exceptions.payment;

/**
 * Exception levée lorsqu'un paiement est tenté sur un panier vide.
 */
public class CartEmptyException extends RuntimeException {
    public CartEmptyException(String message) {
        super(message);
    }
}
