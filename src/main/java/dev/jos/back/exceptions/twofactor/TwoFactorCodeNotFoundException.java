package dev.jos.back.exceptions.twofactor;

public class TwoFactorCodeNotFoundException extends RuntimeException {
    public TwoFactorCodeNotFoundException(String message) {
        super(message);
    }
}
