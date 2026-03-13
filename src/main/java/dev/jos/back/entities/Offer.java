package dev.jos.back.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "offers")
public class Offer {
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

    @ElementCollection
    @CollectionTable(
            name = "offer_features",
            joinColumns = @JoinColumn(name = "offer_id")
    )
    private List<String> features;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "offer")
    private Set<CartItems> cartItems;

    @OneToMany(mappedBy = "offer")
    private Set<Ticket> tickets;
}