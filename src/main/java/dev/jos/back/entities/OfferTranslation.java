package dev.jos.back.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "offer_translations",
        uniqueConstraints = @UniqueConstraint(columnNames = {"offer_id", "locale"}))
public class OfferTranslation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "offer_id", nullable = false)
    private Offer offer;

    @Column(nullable = false, length = 5)
    private String locale;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    // JSON array sérialisé, ex: ["1 siège","Accès standard"]
    @Column(nullable = false, columnDefinition = "TEXT")
    private String features;
}
