package dev.jos.back.exceptions.twofactor;

public class BadTwoFactorCodeException extends RuntimeException {
    public BadTwoFactorCodeException(String message) {
        super(message);
    }
}
