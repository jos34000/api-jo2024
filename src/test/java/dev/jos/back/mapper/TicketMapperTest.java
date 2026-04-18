package dev.jos.back.mapper;

import dev.jos.back.dto.payment.TicketGroupResponseDTO;
import dev.jos.back.dto.payment.TicketResponseDTO;
import dev.jos.back.dto.payment.TransactionResponseDTO;
import dev.jos.back.dto.ticket.ScanResponseDTO;
import dev.jos.back.entities.Ticket;
import dev.jos.back.entities.Transaction;
import dev.jos.back.support.TestFixtures;
import dev.jos.back.util.enums.TransactionStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class TicketMapperTest {

    private TicketMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new TicketMapper();
    }

    private Transaction transaction() {
        Transaction tx = new Transaction();
        tx.setId(10L);
        tx.setTransactionKey("tx-key-uuid");
        tx.setStatus(TransactionStatus.COMPLETED);
        tx.setAmount(BigDecimal.valueOf(50.0));
        tx.setPaymentReference("REF-A1B2");
        tx.setPayedDate(LocalDateTime.of(2024, 7, 1, 10, 0));
        return tx;
    }

    private Ticket validTicket(String barcode) {
        Ticket t = TestFixtures.ticketEntity(barcode);
        t.setTransaction(transaction());
        return t;
    }

    // ── computeStatus (testé via toTicketResponseDTO) ─────────────────────────

    @Test
    void toTicketResponseDTO_statusIsValid_whenIsValidTrueAndNotScanned() {
        Ticket ticket = validTicket("JO-001");
        ticket.setIsValid(true);
        ticket.setIsScanned(false);

        TicketResponseDTO dto = mapper.toTicketResponseDTO(ticket);

        assertThat(dto.status()).isEqualTo("VALID");
    }

    @Test
    void toTicketResponseDTO_statusIsCancelled_whenIsValidFalse() {
        Ticket ticket = validTicket("JO-002");
        ticket.setIsValid(false);

        TicketResponseDTO dto = mapper.toTicketResponseDTO(ticket);

        assertThat(dto.status()).isEqualTo("CANCELLED");
    }

    @Test
    void toTicketResponseDTO_statusIsUsed_whenIsScannedTrue() {
        Ticket ticket = validTicket("JO-003");
        ticket.setIsValid(true);
        ticket.setIsScanned(true);

        TicketResponseDTO dto = mapper.toTicketResponseDTO(ticket);

        assertThat(dto.status()).isEqualTo("USED");
    }

    // ── toTicketResponseDTO ───────────────────────────────────────────────────

    @Test
    void toTicketResponseDTO_mapsAllFields() {
        Ticket ticket = validTicket("JO-BARCODE");

        TicketResponseDTO dto = mapper.toTicketResponseDTO(ticket);

        assertThat(dto.id()).isEqualTo(1L);
        assertThat(dto.barcode()).isEqualTo("JO-BARCODE");
        assertThat(dto.ticketKey()).isEqualTo("ticket-key-uuid");
        assertThat(dto.combinedKey()).isEqualTo("a".repeat(64));
        assertThat(dto.price()).isEqualTo(50.0);
        assertThat(dto.event().name()).isEqualTo("100m Finale");
        assertThat(dto.offer().name()).isEqualTo("Solo");
    }

    // ── toScanResponseDTO ─────────────────────────────────────────────────────

    @Test
    void toScanResponseDTO_mapsOutcomeAndHolderInfo() {
        Ticket ticket = validTicket("JO-SCAN");
        ticket.setScannedAt(LocalDateTime.of(2024, 7, 26, 20, 15));
        ticket.setScannedBy("agent@jo2024.fr");

        ScanResponseDTO dto = mapper.toScanResponseDTO(ticket, "SUCCESS");

        assertThat(dto.outcome()).isEqualTo("SUCCESS");
        assertThat(dto.barcode()).isEqualTo("JO-SCAN");
        assertThat(dto.holderFirstName()).isEqualTo("Alice");
        assertThat(dto.holderLastName()).isEqualTo("Dupont");
        assertThat(dto.holderEmail()).isEqualTo("alice@jo2024.fr");
        assertThat(dto.scannedBy()).isEqualTo("agent@jo2024.fr");
        assertThat(dto.price()).isEqualTo(50.0);
    }

    // ── toTicketGroupResponseDTO ───────────────────────────────────────────────

    @Test
    void toTicketGroupResponseDTO_mapsTransactionAndEventFromFirstTicket() {
        Ticket ticket = validTicket("JO-GROUP");

        TicketGroupResponseDTO dto = mapper.toTicketGroupResponseDTO(List.of(ticket));

        assertThat(dto.transactionId()).isEqualTo(10L);
        assertThat(dto.paymentReference()).isEqualTo("REF-A1B2");
        assertThat(dto.event().name()).isEqualTo("100m Finale");
        assertThat(dto.offer().name()).isEqualTo("Solo");
    }

    @Test
    void toTicketGroupResponseDTO_computesTotalSeatsAndPrice() {
        Ticket t1 = validTicket("JO-T1");
        Ticket t2 = validTicket("JO-T2");
        t2.setId(2L);
        t2.setTransaction(transaction());
        t2.setUser(TestFixtures.user("alice@jo2024.fr"));
        t2.setEvent(TestFixtures.event("100m Finale", 100));
        t2.setOffer(TestFixtures.offer("Solo", 50.0, 1));

        TicketGroupResponseDTO dto = mapper.toTicketGroupResponseDTO(List.of(t1, t2));

        assertThat(dto.totalSeats()).isEqualTo(2);
        assertThat(dto.totalPrice()).isCloseTo(100.0, within(0.01));
        assertThat(dto.barcodes()).containsExactlyInAnyOrder("JO-T1", "JO-T2");
    }

    @Test
    void toTicketGroupResponseDTO_groupStatusIsValid_whenAllTicketsValid() {
        Ticket ticket = validTicket("JO-G1");
        ticket.setIsValid(true);
        ticket.setIsScanned(false);

        TicketGroupResponseDTO dto = mapper.toTicketGroupResponseDTO(List.of(ticket));

        assertThat(dto.groupStatus()).isEqualTo("VALID");
    }

    @Test
    void toTicketGroupResponseDTO_groupStatusIsCancelled_whenAnyTicketInvalid() {
        Ticket t1 = validTicket("JO-G2");
        t1.setIsValid(true);
        Ticket t2 = validTicket("JO-G3");
        t2.setIsValid(false);
        t2.setTransaction(transaction());
        t2.setUser(TestFixtures.user("alice@jo2024.fr"));
        t2.setEvent(TestFixtures.event("100m Finale", 100));
        t2.setOffer(TestFixtures.offer("Solo", 50.0, 1));

        TicketGroupResponseDTO dto = mapper.toTicketGroupResponseDTO(List.of(t1, t2));

        assertThat(dto.groupStatus()).isEqualTo("CANCELLED");
    }

    @Test
    void toTicketGroupResponseDTO_groupStatusIsUsed_whenAllTicketsScanned() {
        Ticket ticket = validTicket("JO-G4");
        ticket.setIsValid(true);
        ticket.setIsScanned(true);

        TicketGroupResponseDTO dto = mapper.toTicketGroupResponseDTO(List.of(ticket));

        assertThat(dto.groupStatus()).isEqualTo("USED");
    }

    // ── toTransactionResponseDTO ──────────────────────────────────────────────

    @Test
    void toTransactionResponseDTO_mapsAllFields() {
        Transaction tx = transaction();
        Ticket ticket = validTicket("JO-TX");

        TransactionResponseDTO dto = mapper.toTransactionResponseDTO(tx, List.of(ticket));

        assertThat(dto.id()).isEqualTo(10L);
        assertThat(dto.transactionKey()).isEqualTo("tx-key-uuid");
        assertThat(dto.status()).isEqualTo("COMPLETED");
        assertThat(dto.amount()).isEqualByComparingTo(BigDecimal.valueOf(50.0));
        assertThat(dto.paymentReference()).isEqualTo("REF-A1B2");
        assertThat(dto.tickets()).hasSize(1);
        assertThat(dto.tickets().getFirst().barcode()).isEqualTo("JO-TX");
    }
}
