package dev.jos.back.entities;

import dev.jos.back.util.enums.Phases;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "sports")
public class Sport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;
    @Column(nullable = false)
    private String description;
    @Column(nullable = false)
    private String icon;

    @ElementCollection
    @Enumerated(EnumType.STRING)
    @CollectionTable(
            name = "sport_phases",
            joinColumns = @JoinColumn(name = "sport_id")
    )
    @Column(name = "phase")
    private List<Phases> phases;

    @ElementCollection
    @CollectionTable(
            name = "sport_places",
            joinColumns = @JoinColumn(name = "sport_id")
    )
    @Column(name = "place")
    private List<String> places;

    @OneToMany(mappedBy = "sport")
    private List<Event> events;
}
