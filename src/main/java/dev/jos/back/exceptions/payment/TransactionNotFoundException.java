package dev.jos.back.exceptions.payment;

/**
 * Exception levée lorsqu'une transaction est introuvable ou n'appartient pas à l'utilisateur.
 */
public class TransactionNotFoundException extends RuntimeException {
    public TransactionNotFoundException(String message) {
        super(message);
    }
}
