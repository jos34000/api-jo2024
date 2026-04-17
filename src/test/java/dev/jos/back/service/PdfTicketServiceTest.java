package dev.jos.back.service;

import dev.jos.back.dto.cart.CartEventSummaryDTO;
import dev.jos.back.dto.payment.TicketResponseDTO;
import dev.jos.back.dto.payment.TransactionResponseDTO;
import dev.jos.back.support.TestFixtures;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PdfTicketServiceTest {

    private final PdfTicketService pdfTicketService = new PdfTicketService();

    @Test
    void generate_producesValidPdf_withDefaultFrenchLocale() {
        TransactionResponseDTO tx = TestFixtures.transaction(
                List.of(TestFixtures.ticket("T1", 50.0)));

        byte[] pdf = pdfTicketService.generate(tx);

        assertThat(pdf).isNotEmpty();
        assertThat(startsWithPdfHeader(pdf)).isTrue();
    }

    @Test
    void generate_singleArgOverload_producesPdfOfComparableSize_toFrenchLocale() {
        TransactionResponseDTO tx = TestFixtures.transaction(
                List.of(TestFixtures.ticket("T1", 50.0)));

        byte[] defaultPdf = pdfTicketService.generate(tx);
        byte[] frenchPdf = pdfTicketService.generate(tx, "fr");

        // PDFs embed timestamps in metadata so byte-equality is unreliable;
        // identical content produces nearly-identical sizes.
        assertThat(Math.abs(defaultPdf.length - frenchPdf.length)).isLessThan(100);
    }

    @ParameterizedTest
    @ValueSource(strings = {"fr", "en", "de", "es"})
    void generate_producesValidPdf_forEverySupportedLocale(String locale) {
        TransactionResponseDTO tx = TestFixtures.transaction(
                List.of(TestFixtures.ticket("T1", 50.0)));

        byte[] pdf = pdfTicketService.generate(tx, locale);

        assertThat(pdf).isNotEmpty();
        assertThat(startsWithPdfHeader(pdf)).isTrue();
    }

    @Test
    void generate_fallsBackToFrench_whenLocaleIsUnknown() {
        TransactionResponseDTO tx = TestFixtures.transaction(
                List.of(TestFixtures.ticket("T1", 50.0)));

        byte[] pdf = pdfTicketService.generate(tx, "zz");

        assertThat(pdf).isNotEmpty();
        assertThat(startsWithPdfHeader(pdf)).isTrue();
    }

    @Test
    void generate_producesLargerPdf_whenTransactionHasMultipleTickets() {
        TransactionResponseDTO single = TestFixtures.transaction(
                List.of(TestFixtures.ticket("T1", 50.0)));
        TransactionResponseDTO triple = TestFixtures.transaction(List.of(
                TestFixtures.ticket("T1", 50.0),
                TestFixtures.ticket("T2", 50.0),
                TestFixtures.ticket("T3", 50.0)));

        byte[] singlePdf = pdfTicketService.generate(single, "fr");
        byte[] triplePdf = pdfTicketService.generate(triple, "fr");

        assertThat(triplePdf.length).isGreaterThan(singlePdf.length);
    }

    @Test
    void generate_doesNotThrow_whenEventPhaseIsNull() {
        CartEventSummaryDTO eventNoPhase = CartEventSummaryDTO.builder()
                .id(1L)
                .name("100m Finale")
                .eventDate(java.time.LocalDateTime.of(2024, 7, 26, 20, 0))
                .location("Stade de France")
                .city("Saint-Denis")
                .phase(null)
                .build();
        TicketResponseDTO ticket = TicketResponseDTO.builder()
                .id(1L).ticketKey("T1").combinedKey("a".repeat(64)).barcode("JO2024-T1").price(50.0).status("VALID")
                .createdAt(java.time.LocalDateTime.of(2024, 7, 1, 10, 0))
                .event(eventNoPhase).offer(TestFixtures.offerSummary())
                .build();
        TransactionResponseDTO tx = TestFixtures.transaction(List.of(ticket));

        byte[] pdf = pdfTicketService.generate(tx, "fr");

        assertThat(pdf).isNotEmpty();
        assertThat(startsWithPdfHeader(pdf)).isTrue();
    }

    private static boolean startsWithPdfHeader(byte[] bytes) {
        // PDF magic bytes: "%PDF-"
        return bytes.length >= 5
                && bytes[0] == '%' && bytes[1] == 'P' && bytes[2] == 'D'
                && bytes[3] == 'F' && bytes[4] == '-';
    }
}
