package dev.jos.back.service;

import dev.jos.back.util.PaymentResult;

public interface PaymentGateway {
    PaymentResult processPayment(String cardNumber);
}
