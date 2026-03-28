package dev.jos.back.util;

public record PaymentResult(boolean succeeded, String declineReason) {

    public static PaymentResult success() {
        return new PaymentResult(true, null);
    }

    public static PaymentResult failure(String reason) {
        return new PaymentResult(false, reason);
    }
}
