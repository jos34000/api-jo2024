package dev.jos.back.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class PaymentMockServiceTest {

    private final PaymentMockService service = new PaymentMockService();

    @Test
    void processPayment_succeeds_forArbitraryCard() {
        PaymentMockService.PaymentResult result = service.processPayment("4242424242424242");

        assertThat(result.succeeded()).isTrue();
        assertThat(result.declineReason()).isNull();
    }

    @ParameterizedTest
    @CsvSource({
            "4000000000000002, Carte déclinée",
            "4000000000009995, Fonds insuffisants",
            "4000000000000069, Carte expirée"
    })
    void processPayment_declines_withFrenchReason_forKnownTestCards(String card, String expectedReason) {
        PaymentMockService.PaymentResult result = service.processPayment(card);

        assertThat(result.succeeded()).isFalse();
        assertThat(result.declineReason()).isEqualTo(expectedReason);
    }

    @Test
    void processPayment_normalizesWhitespace_inCardNumber() {
        PaymentMockService.PaymentResult spaced = service.processPayment("4000 0000 0000 0002");
        PaymentMockService.PaymentResult tabbed = service.processPayment("4000\t0000\t0000\t0002");

        assertThat(spaced.succeeded()).isFalse();
        assertThat(spaced.declineReason()).isEqualTo("Carte déclinée");
        assertThat(tabbed.succeeded()).isFalse();
        assertThat(tabbed.declineReason()).isEqualTo("Carte déclinée");
    }

    @Test
    void processPayment_doesNotDecline_forCardsWithSimilarButDifferentDigits() {
        // "4000000000000020" differs from the decline list "4000000000000002" by one digit.
        // Confirms the matching is exact, not prefix-based.
        PaymentMockService.PaymentResult result = service.processPayment("4000000000000020");

        assertThat(result.succeeded()).isTrue();
    }

    @Test
    void paymentResult_factoryMethods_buildCorrectStates() {
        PaymentMockService.PaymentResult success = PaymentMockService.PaymentResult.success();
        PaymentMockService.PaymentResult failure = PaymentMockService.PaymentResult.failure("nope");

        assertThat(success.succeeded()).isTrue();
        assertThat(success.declineReason()).isNull();
        assertThat(failure.succeeded()).isFalse();
        assertThat(failure.declineReason()).isEqualTo("nope");
    }
}
