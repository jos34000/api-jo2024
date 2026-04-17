package dev.jos.back.mapper;

import dev.jos.back.dto.cart.CartEventSummaryDTO;
import dev.jos.back.dto.cart.CartOfferSummaryDTO;
import dev.jos.back.dto.payment.TicketGroupResponseDTO;
import dev.jos.back.dto.payment.TicketResponseDTO;
import dev.jos.back.dto.payment.TransactionResponseDTO;
import dev.jos.back.dto.ticket.ScanResponseDTO;
import dev.jos.back.entities.Ticket;
import dev.jos.back.entities.Transaction;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TicketMapper {

    public TicketResponseDTO toTicketResponseDTO(Ticket t) {
        return TicketResponseDTO.builder()
                .id(t.getId())
                .ticketKey(t.getTicketKey())
                .combinedKey(t.getCombinedKey())
                .barcode(t.getBarcode())
                .price(t.getPrice())
                .status(computeStatus(t))
                .createdAt(t.getCreatedAt())
                .event(toEventSummary(t))
                .offer(toOfferSummary(t))
                .build();
    }

    public ScanResponseDTO toScanResponseDTO(Ticket t, String outcome) {
        return ScanResponseDTO.builder()
                .outcome(outcome)
                .barcode(t.getBarcode())
                .status(computeStatus(t))
                .holderFirstName(t.getUser().getFirstName())
                .holderLastName(t.getUser().getLastName())
                .holderEmail(t.getUser().getEmail())
                .scannedAt(t.getScannedAt())
                .scannedBy(t.getScannedBy())
                .price(t.getPrice())
                .expiryAt(t.getExpiryAt())
                .createdAt(t.getCreatedAt())
                .event(toEventSummary(t))
                .offer(toOfferSummary(t))
                .build();
    }

    public TicketGroupResponseDTO toTicketGroupResponseDTO(List<Ticket> tickets) {
        Ticket first = tickets.getFirst();
        return TicketGroupResponseDTO.builder()
                .transactionId(first.getTransaction().getId())
                .paymentReference(first.getTransaction().getPaymentReference())
                .purchasedAt(first.getCreatedAt())
                .event(toEventSummary(first))
                .offer(toOfferSummary(first))
                .totalSeats(tickets.size())
                .totalPrice(tickets.stream().mapToDouble(Ticket::getPrice).sum())
                .groupStatus(computeGroupStatus(tickets))
                .barcodes(tickets.stream().map(Ticket::getBarcode).toList())
                .build();
    }

    public TransactionResponseDTO toTransactionResponseDTO(Transaction transaction, List<Ticket> tickets) {
        return TransactionResponseDTO.builder()
                .id(transaction.getId())
                .transactionKey(transaction.getTransactionKey())
                .status(transaction.getStatus().name())
                .amount(transaction.getAmount())
                .paymentReference(transaction.getPaymentReference())
                .payedDate(transaction.getPayedDate())
                .tickets(tickets.stream().map(this::toTicketResponseDTO).toList())
                .build();
    }

    private String computeStatus(Ticket t) {
        if (!Boolean.TRUE.equals(t.getIsValid())) return "CANCELLED";
        if (Boolean.TRUE.equals(t.getIsScanned())) return "USED";
        return "VALID";
    }

    private String computeGroupStatus(List<Ticket> tickets) {
        if (tickets.stream().anyMatch(t -> !Boolean.TRUE.equals(t.getIsValid()))) return "CANCELLED";
        if (tickets.stream().allMatch(t -> Boolean.TRUE.equals(t.getIsScanned()))) return "USED";
        return "VALID";
    }

    private CartEventSummaryDTO toEventSummary(Ticket t) {
        return CartEventSummaryDTO.builder()
                .id(t.getEvent().getId())
                .name(t.getEvent().getName())
                .eventDate(t.getEvent().getEventDate())
                .location(t.getEvent().getLocation())
                .city(t.getEvent().getCity())
                .phase(t.getEvent().getPhase())
                .build();
    }

    private CartOfferSummaryDTO toOfferSummary(Ticket t) {
        return CartOfferSummaryDTO.builder()
                .id(t.getOffer().getId())
                .name(t.getOffer().getName())
                .numberOfTickets(t.getOffer().getNumberOfTickets())
                .price(t.getOffer().getPrice())
                .build();
    }
}
