package dev.jos.back.service;

import dev.jos.back.util.PaymentResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class MockPaymentGatewayTest {

    private final MockPaymentGateway gateway = new MockPaymentGateway(new PaymentMockService());

    @Test
    void processPayment_succeeds_forDefaultCard() {
        PaymentResult result = gateway.processPayment("4242424242424242");
        assertThat(result.succeeded()).isTrue();
        assertThat(result.declineReason()).isNull();
    }

    @ParameterizedTest
    @CsvSource({
        "4000000000000002, Carte déclinée",
        "4000000000009995, Fonds insuffisants",
        "4000000000000069, Carte expirée"
    })
    void processPayment_fails_forTestDeclineCards(String cardNumber, String expectedReason) {
        PaymentResult result = gateway.processPayment(cardNumber);
        assertThat(result.succeeded()).isFalse();
        assertThat(result.declineReason()).isEqualTo(expectedReason);
    }
}
