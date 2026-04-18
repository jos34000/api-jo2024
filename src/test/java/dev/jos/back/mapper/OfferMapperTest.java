package dev.jos.back.mapper;

import dev.jos.back.dto.offer.CreateOfferDTO;
import dev.jos.back.dto.offer.OfferResponseDTO;
import dev.jos.back.entities.Offer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OfferMapperTest {

    private OfferMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new OfferMapper();
    }

    private Offer offer() {
        return Offer.builder()
                .id(1L)
                .name("Solo")
                .description("Un billet solo")
                .price(50.0)
                .numberOfTickets(1)
                .isActive(true)
                .displayOrder(1)
                .features(List.of("Accès tribune", "Programme officiel"))
                .build();
    }

    // ── toEntity ─────────────────────────────────────────────────────────────

    @Test
    void toEntity_mapsAllFields() {
        CreateOfferDTO dto = CreateOfferDTO.builder()
                .name("Duo")
                .description("Deux billets")
                .price(90.0)
                .numberOfTickets(2)
                .isActive(true)
                .displayOrder(2)
                .features(List.of("Accès VIP"))
                .build();

        Offer offer = mapper.toEntity(dto);

        assertThat(offer.getName()).isEqualTo("Duo");
        assertThat(offer.getDescription()).isEqualTo("Deux billets");
        assertThat(offer.getPrice()).isEqualTo(90.0);
        assertThat(offer.getNumberOfTickets()).isEqualTo(2);
        assertThat(offer.getIsActive()).isTrue();
        assertThat(offer.getDisplayOrder()).isEqualTo(2);
        assertThat(offer.getFeatures()).containsExactly("Accès VIP");
    }

    // ── toResponseDTO ─────────────────────────────────────────────────────────

    @Test
    void toResponseDTO_mapsAllFieldsFromEntity() {
        Offer offer = offer();

        OfferResponseDTO dto = mapper.toResponseDTO(offer);

        assertThat(dto.id()).isEqualTo(1L);
        assertThat(dto.name()).isEqualTo("Solo");
        assertThat(dto.description()).isEqualTo("Un billet solo");
        assertThat(dto.price()).isEqualTo(50.0);
        assertThat(dto.numberOfTickets()).isEqualTo(1);
        assertThat(dto.isActive()).isTrue();
        assertThat(dto.displayOrder()).isEqualTo(1);
        assertThat(dto.features()).containsExactly("Accès tribune", "Programme officiel");
    }

    @Test
    void toResponseDTO_withCustomValues_overridesEntityValues() {
        Offer offer = offer();
        List<String> translatedFeatures = List.of("Tribune access", "Official programme");

        OfferResponseDTO dto = mapper.toResponseDTO(offer, "Solo [EN]", "One ticket", translatedFeatures);

        assertThat(dto.name()).isEqualTo("Solo [EN]");
        assertThat(dto.description()).isEqualTo("One ticket");
        assertThat(dto.features()).containsExactly("Tribune access", "Official programme");
        // Les champs non surchargés restent ceux de l'entité
        assertThat(dto.id()).isEqualTo(1L);
        assertThat(dto.price()).isEqualTo(50.0);
    }
}
