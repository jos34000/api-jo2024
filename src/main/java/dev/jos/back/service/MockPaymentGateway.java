package dev.jos.back.service;

import dev.jos.back.util.PaymentResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MockPaymentGateway implements PaymentGateway {

    private final PaymentMockService paymentMockService;

    @Override
    public PaymentResult processPayment(String cardNumber) {
        PaymentMockService.PaymentResult inner = paymentMockService.processPayment(cardNumber);
        return inner.succeeded()
                ? PaymentResult.success()
                : PaymentResult.failure(inner.declineReason());
    }
}
