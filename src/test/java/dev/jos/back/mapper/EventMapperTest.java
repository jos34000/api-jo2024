package dev.jos.back.mapper;

import dev.jos.back.dto.event.CreateEventDTO;
import dev.jos.back.dto.event.EventResponseDTO;
import dev.jos.back.entities.Event;
import dev.jos.back.entities.Sport;
import dev.jos.back.util.enums.Phases;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EventMapperTest {

    private EventMapper mapper;

    private static final LocalDateTime EVENT_DATE = LocalDateTime.of(2024, 7, 26, 20, 0);

    @BeforeEach
    void setUp() {
        mapper = new EventMapper();
    }

    private Sport sport(String name) {
        return Sport.builder()
                .id(1L)
                .name(name)
                .description("desc")
                .icon("icon.svg")
                .phases(List.of(Phases.FINALE))
                .build();
    }

    private Event eventWithSport() {
        return Event.builder()
                .id(10L)
                .name("100m Finale")
                .description("Grande finale du 100m")
                .icon("sprint.svg")
                .category("Athlétisme")
                .phase(Phases.FINALE)
                .location("Stade de France")
                .city("Saint-Denis")
                .eventDate(EVENT_DATE)
                .capacity(100)
                .availableSlots(50)
                .isActive(true)
                .sport(sport("Athlétisme"))
                .build();
    }

    // ── toEntity ─────────────────────────────────────────────────────────────

    @Test
    void toEntity_mapsAllFields() {
        CreateEventDTO dto = new CreateEventDTO(
                "100m Finale", "Grande finale du 100m", "sprint.svg",
                "Athlétisme", Phases.FINALE, "Stade de France", "Saint-Denis",
                "Athlétisme", EVENT_DATE, 100, 50, true
        );

        Event event = mapper.toEntity(dto);

        assertThat(event.getName()).isEqualTo("100m Finale");
        assertThat(event.getDescription()).isEqualTo("Grande finale du 100m");
        assertThat(event.getIcon()).isEqualTo("sprint.svg");
        assertThat(event.getCategory()).isEqualTo("Athlétisme");
        assertThat(event.getPhase()).isEqualTo(Phases.FINALE);
        assertThat(event.getLocation()).isEqualTo("Stade de France");
        assertThat(event.getCity()).isEqualTo("Saint-Denis");
        assertThat(event.getEventDate()).isEqualTo(EVENT_DATE);
        assertThat(event.getCapacity()).isEqualTo(100);
        assertThat(event.getAvailableSlots()).isEqualTo(50);
        assertThat(event.getIsActive()).isTrue();
    }

    @Test
    void toEntity_doesNotResolveSportRelationship() {
        CreateEventDTO dto = new CreateEventDTO(
                "100m Finale", "Grande finale du 100m", "sprint.svg",
                "Athlétisme", Phases.FINALE, "Stade de France", "Saint-Denis",
                "Athlétisme", EVENT_DATE, 100, 50, true
        );

        Event event = mapper.toEntity(dto);

        // Le service résout la relation Sport séparément via SportRepository
        assertThat(event.getSport()).isNull();
    }

    // ── toResponseDTO ─────────────────────────────────────────────────────────

    @Test
    void toResponseDTO_mapsAllFieldsFromEntity() {
        Event event = eventWithSport();

        EventResponseDTO dto = mapper.toResponseDTO(event);

        assertThat(dto.id()).isEqualTo(10L);
        assertThat(dto.name()).isEqualTo("100m Finale");
        assertThat(dto.description()).isEqualTo("Grande finale du 100m");
        assertThat(dto.icon()).isEqualTo("sprint.svg");
        assertThat(dto.category()).isEqualTo("Athlétisme");
        assertThat(dto.phase()).isEqualTo(Phases.FINALE);
        assertThat(dto.location()).isEqualTo("Stade de France");
        assertThat(dto.city()).isEqualTo("Saint-Denis");
        assertThat(dto.eventDate()).isEqualTo(EVENT_DATE);
        assertThat(dto.capacity()).isEqualTo(100);
        assertThat(dto.availableSlots()).isEqualTo(50);
        assertThat(dto.isActive()).isTrue();
        assertThat(dto.sport()).isEqualTo("Athlétisme");
    }

    @Test
    void toResponseDTO_withCustomNameAndDescription_overridesEntityValues() {
        Event event = eventWithSport();

        EventResponseDTO dto = mapper.toResponseDTO(event, "100m Final [EN]", "Grand finale of 100m");

        assertThat(dto.name()).isEqualTo("100m Final [EN]");
        assertThat(dto.description()).isEqualTo("Grand finale of 100m");
        // Les autres champs restent ceux de l'entité
        assertThat(dto.id()).isEqualTo(10L);
        assertThat(dto.sport()).isEqualTo("Athlétisme");
    }
}
