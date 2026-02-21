package dev.jos.back.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "offer_types")
public class OfferType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private Double price;

    @Column(nullable = false)
    private Integer numberOfTickets;

    @Column(nullable = false)
    private Boolean isActive = true;

    @Column(nullable = false)
    private Integer displayOrder;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "offerType")
    private Set<CartItems> cartItems;

    @OneToMany(mappedBy = "offerType")
    private Set<Ticket> tickets;
}