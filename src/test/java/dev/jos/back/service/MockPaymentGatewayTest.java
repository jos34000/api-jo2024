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

    @Test
    void processPayment_propagatesDecline_whenCardIsFormattedWithSpaces() {
        PaymentResult result = gateway.processPayment("4000 0000 0000 0002");

        assertThat(result.succeeded()).isFalse();
        assertThat(result.declineReason()).isEqualTo("Carte déclinée");
    }

    @Test
    void processPayment_doesNotMatchByPrefix_onlyExactDigits() {
        // "4000000000000020" differs from any decline card by digit position;
        // confirms the gateway is not prefix-matching its decline list.
        PaymentResult result = gateway.processPayment("4000000000000020");

        assertThat(result.succeeded()).isTrue();
        assertThat(result.declineReason()).isNull();
    }
}
