package dev.jos.back.exceptions.twofactor;

public class TwoFactorMaxAttemptsException extends RuntimeException {
    public TwoFactorMaxAttemptsException(String message) {
        super(message);
    }
}
