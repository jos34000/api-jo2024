package dev.jos.back.exceptions.offertype;

public class OfferTypeAlreadyExistsException extends RuntimeException {
    public OfferTypeAlreadyExistsException(String message) {
        super(message);
    }
}
