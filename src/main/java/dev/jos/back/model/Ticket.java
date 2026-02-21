package dev.jos.back.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "tickets")
public class Ticket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String userKey;

    @Column(nullable = false)
    private String transactionKey;

    @Column(nullable = false, unique = true)
    private String ticketKey;

    @Column(nullable = false, unique = true)
    private String combinedKey;

    @Column(nullable = false, unique = true)
    private String barcode;

    @Column(nullable = false)
    private Double price;

    @Column(nullable = false)
    private Boolean isValid = true;

    @Column(nullable = false)
    private Boolean isScanned = false;

    private LocalDateTime scannedAt;

    private String scannedBy;

    private LocalDateTime expiryAt;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name = "transaction_id", nullable = false)
    private Transaction transaction;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @ManyToOne
    @JoinColumn(name = "offer_type_id", nullable = false)
    private OfferType offerType;
}