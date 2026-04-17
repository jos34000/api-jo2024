package dev.jos.back.support;

import dev.jos.back.dto.cart.CartEventSummaryDTO;
import dev.jos.back.dto.cart.CartOfferSummaryDTO;
import dev.jos.back.dto.payment.TicketResponseDTO;
import dev.jos.back.dto.payment.TransactionResponseDTO;
import dev.jos.back.entities.Event;
import dev.jos.back.entities.Offer;
import dev.jos.back.entities.Ticket;
import dev.jos.back.entities.User;
import dev.jos.back.util.enums.Phases;
import dev.jos.back.util.enums.Role;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public final class TestFixtures {

    private TestFixtures() {}

    public static User user(String email) {
        User u = new User();
        u.setEmail(email);
        u.setFirstName("Alice");
        u.setLastName("Dupont");
        u.setLocale("fr");
        u.setRole(Role.ROLE_USER);
        return u;
    }

    public static Event event(String name, int slots) {
        return Event.builder()
                .name(name)
                .icon("icon.svg")
                .description("desc")
                .category("Athlétisme")
                .phase(Phases.FINALE)
                .location("Stade de France")
                .city("Saint-Denis")
                .eventDate(LocalDateTime.of(2024, 7, 26, 20, 0))
                .capacity(100)
                .availableSlots(slots)
                .isActive(true)
                .build();
    }

    public static Offer offer(String name, double price, int seats) {
        return Offer.builder()
                .name(name)
                .description("Offre " + name)
                .price(price)
                .numberOfTickets(seats)
                .isActive(true)
                .displayOrder(1)
                .build();
    }

    public static Ticket ticketEntity(String barcode) {
        Ticket t = new Ticket();
        t.setId(1L);
        t.setBarcode(barcode);
        t.setUserKey("user-key-uuid");
        t.setTicketKey("ticket-key-uuid");
        t.setCombinedKey("a".repeat(64));
        t.setIsValid(true);
        t.setIsScanned(false);
        t.setPrice(50.0);
        t.setExpiryAt(LocalDateTime.of(2030, 7, 26, 23, 59));
        t.setUser(user("alice@jo2024.fr"));
        t.setEvent(event("100m Finale", 100));
        t.setOffer(offer("Solo", 50.0, 1));
        return t;
    }

    public static TicketResponseDTO ticket(String key, double price) {
        return TicketResponseDTO.builder()
                .id(1L)
                .ticketKey(key)
                .combinedKey("a".repeat(64))
                .barcode("JO2024-" + key)
                .price(price)
                .status("VALID")
                .createdAt(LocalDateTime.of(2024, 7, 1, 10, 0))
                .event(eventSummary())
                .offer(offerSummary())
                .build();
    }

    public static CartEventSummaryDTO eventSummary() {
        return CartEventSummaryDTO.builder()
                .id(1L)
                .name("100m Finale")
                .eventDate(LocalDateTime.of(2024, 7, 26, 20, 0))
                .location("Stade de France")
                .city("Saint-Denis")
                .phase(Phases.FINALE)
                .build();
    }

    public static CartOfferSummaryDTO offerSummary() {
        return CartOfferSummaryDTO.builder()
                .id(1L)
                .name("Solo")
                .numberOfTickets(1)
                .price(50.0)
                .build();
    }

    public static TransactionResponseDTO transaction(List<TicketResponseDTO> tickets) {
        return TransactionResponseDTO.builder()
                .id(42L)
                .transactionKey("tx-key-uuid")
                .status("COMPLETED")
                .amount(BigDecimal.valueOf(50.0))
                .paymentReference("REF-A1B2C3D4")
                .payedDate(LocalDateTime.of(2024, 7, 1, 10, 0))
                .tickets(tickets)
                .build();
    }
}
