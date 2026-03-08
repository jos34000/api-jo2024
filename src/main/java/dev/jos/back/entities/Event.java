package dev.jos.back.entities;

import dev.jos.back.util.enums.Phases;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "events")
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, length = 1000)
    private String description;

    @Column(nullable = false)
    private String category;

    @Enumerated(EnumType.STRING)
    private Phases phase;

    @Column(nullable = false)
    private String location;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private LocalDateTime eventDate;

    @Column(nullable = false)
    private Integer capacity;

    @Column(nullable = false)
    private Integer availableSlots;

    @Column(nullable = false)
    private Boolean isActive = true;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name = "sport_id")
    private Sport sport;

    @OneToMany(mappedBy = "event")
    private Set<CartItems> cartItems;

    @OneToMany(mappedBy = "event")
    private Set<Ticket> tickets;
}
