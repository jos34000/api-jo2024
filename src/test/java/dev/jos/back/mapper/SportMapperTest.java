package dev.jos.back.mapper;

import dev.jos.back.dto.sport.CreateSportDTO;
import dev.jos.back.dto.sport.SportResponseDTO;
import dev.jos.back.entities.Event;
import dev.jos.back.entities.Sport;
import dev.jos.back.support.TestFixtures;
import dev.jos.back.util.enums.Phases;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SportMapperTest {

    private SportMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new SportMapper();
    }

    private Sport sport(List<Event> events) {
        return Sport.builder()
                .id(1L)
                .name("Athlétisme")
                .description("Épreuves sur piste et de champ")
                .icon("athletics.svg")
                .phases(List.of(Phases.QUALIFICATION, Phases.FINALE))
                .events(events)
                .build();
    }

    // ── toEntity ─────────────────────────────────────────────────────────────

    @Test
    void toEntity_mapsAllFields() {
        CreateSportDTO dto = new CreateSportDTO(
                "Natation", "Épreuves aquatiques", "swim.svg",
                List.of(Phases.DEMI_FINALE, Phases.FINALE)
        );

        Sport sport = mapper.toEntity(dto);

        assertThat(sport.getName()).isEqualTo("Natation");
        assertThat(sport.getDescription()).isEqualTo("Épreuves aquatiques");
        assertThat(sport.getIcon()).isEqualTo("swim.svg");
        assertThat(sport.getPhases()).containsExactly(Phases.DEMI_FINALE, Phases.FINALE);
    }

    // ── toDto ────────────────────────────────────────────────────────────────

    @Test
    void toDto_mapsBasicFields() {
        Sport sport = sport(List.of());

        SportResponseDTO dto = mapper.toDto(sport);

        assertThat(dto.id()).isEqualTo(1L);
        assertThat(dto.name()).isEqualTo("Athlétisme");
        assertThat(dto.description()).isEqualTo("Épreuves sur piste et de champ");
        assertThat(dto.icon()).isEqualTo("athletics.svg");
        assertThat(dto.phases()).containsExactly(Phases.QUALIFICATION, Phases.FINALE);
    }

    @Test
    void toDto_computesEventCountFromEvents() {
        List<Event> events = List.of(
                TestFixtures.event("100m", 50),
                TestFixtures.event("200m", 30)
        );

        SportResponseDTO dto = mapper.toDto(sport(events));

        assertThat(dto.eventCount()).isEqualTo(2);
    }

    @Test
    void toDto_computesDistinctSortedLocationsAsPlaces() {
        Event e1 = TestFixtures.event("100m", 50);
        e1.setLocation("Stade de France");
        Event e2 = TestFixtures.event("200m", 30);
        e2.setLocation("Bercy Arena");
        Event e3 = TestFixtures.event("400m", 20);
        e3.setLocation("Stade de France"); // doublon — doit être dédupliqué

        SportResponseDTO dto = mapper.toDto(sport(List.of(e1, e2, e3)));

        assertThat(dto.places()).containsExactly("Bercy Arena", "Stade de France");
    }

    @Test
    void toDto_handlesNullEvents() {
        Sport sport = sport(null);

        SportResponseDTO dto = mapper.toDto(sport);

        assertThat(dto.eventCount()).isEqualTo(0);
        assertThat(dto.places()).isEmpty();
    }

    @Test
    void toDto_withCustomNameAndDescription_overridesEntityValues() {
        Sport sport = sport(List.of());

        SportResponseDTO dto = mapper.toDto(sport, "Athletics [EN]", "Track and field events");

        assertThat(dto.name()).isEqualTo("Athletics [EN]");
        assertThat(dto.description()).isEqualTo("Track and field events");
        assertThat(dto.id()).isEqualTo(1L);
    }
}
