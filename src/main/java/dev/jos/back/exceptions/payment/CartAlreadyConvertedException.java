package dev.jos.back.exceptions.payment;

/**
 * Exception levée lorsqu'un paiement est tenté sur un panier déjà converti en commande.
 */
public class CartAlreadyConvertedException extends RuntimeException {
    public CartAlreadyConvertedException(String message) {
        super(message);
    }
}
