package dev.jos.back.service;

import dev.jos.back.dto.offer.CreateOfferDTO;
import dev.jos.back.dto.offer.OfferResponseDTO;
import dev.jos.back.dto.offer.UpdateOfferDTO;
import dev.jos.back.entities.Offer;
import dev.jos.back.entities.OfferTranslation;
import dev.jos.back.exceptions.offertype.OfferNotFoundException;
import dev.jos.back.exceptions.offertype.OfferTypeAlreadyExistsException;
import dev.jos.back.mapper.OfferMapper;
import dev.jos.back.repository.OfferRepository;
import dev.jos.back.repository.OfferTranslationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OfferServiceTest {

    @Mock OfferRepository repository;
    @Mock OfferMapper mapper;
    @Mock OfferTranslationRepository offerTranslationRepository;
    @InjectMocks OfferService offerService;

    @Test
    void updateOffer_updatesPriceAndReturnsDTO() {
        Offer offer = new Offer();
        offer.setId(1L);
        offer.setName("Solo");
        offer.setPrice(49.0);

        UpdateOfferDTO dto = new UpdateOfferDTO(null, null, 99.0, null, null, null, null);

        when(repository.findById(1L)).thenReturn(Optional.of(offer));
        when(repository.saveAndFlush(offer)).thenReturn(offer);
        OfferResponseDTO expected = OfferResponseDTO.builder().id(1L).name("Solo").price(99.0).features(List.of()).build();
        when(mapper.toResponseDTO(offer)).thenReturn(expected);

        OfferResponseDTO result = offerService.updateOffer(1L, dto);

        assertThat(result.price()).isEqualTo(99.0);
        assertThat(offer.getPrice()).isEqualTo(99.0);
    }

    @Test
    void updateOffer_throwsOfferNotFoundException_whenNotFound() {
        UpdateOfferDTO dto = new UpdateOfferDTO(null, null, null, null, null, null, null);
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> offerService.updateOffer(99L, dto))
                .isInstanceOf(OfferNotFoundException.class);
    }

    @Test
    void deleteOffer_callsDeleteById() {
        when(repository.existsById(1L)).thenReturn(true);

        offerService.deleteOffer(1L);

        verify(repository).deleteById(1L);
    }

    @Test
    void deleteOffer_throwsOfferNotFoundException_whenNotFound() {
        when(repository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> offerService.deleteOffer(99L))
                .isInstanceOf(OfferNotFoundException.class);
    }

    // ── getAllOfferTypes ──────────────────────────────────────────────────────

    @Test
    void getAllOfferTypes_doesNotQueryTranslations_forFrLocale() {
        when(repository.findAll()).thenReturn(List.of());

        offerService.getAllOfferTypes("fr");

        verify(offerTranslationRepository, never()).findByOfferIdsAndLocale(any(), any());
    }

    @Test
    void getAllOfferTypes_queriesTranslationsAndMergesContent_forNonFrLocale() {
        Offer offer = new Offer();
        offer.setId(1L);
        offer.setName("Solo");
        offer.setFeatures(List.of("Feature FR"));

        OfferTranslation translation = new OfferTranslation();
        translation.setOffer(offer);
        translation.setName("Solo EN");
        translation.setDescription("Description EN");
        translation.setFeatures("[\"Feature EN\"]");

        OfferResponseDTO expected = OfferResponseDTO.builder()
                .id(1L).name("Solo EN").price(0.0).features(List.of("Feature EN")).build();

        when(repository.findAll()).thenReturn(List.of(offer));
        when(offerTranslationRepository.findByOfferIdsAndLocale(List.of(1L), "en"))
                .thenReturn(List.of(translation));
        when(mapper.toResponseDTO(offer, "Solo EN", "Description EN", List.of("Feature EN")))
                .thenReturn(expected);

        List<OfferResponseDTO> result = offerService.getAllOfferTypes("en");

        assertThat(result.get(0).name()).isEqualTo("Solo EN");
        verify(offerTranslationRepository).findByOfferIdsAndLocale(List.of(1L), "en");
    }

    // ── createOfferType ───────────────────────────────────────────────────────

    @Test
    void createOfferType_savesAndReturnsDTO() {
        CreateOfferDTO dto = new CreateOfferDTO("Duo", "Description", 79.0, 2, true, 2, List.of("Feature A"));
        Offer offer = new Offer();
        OfferResponseDTO expected = OfferResponseDTO.builder()
                .id(1L).name("Duo").price(79.0).features(List.of("Feature A")).build();

        when(repository.findByName("Duo")).thenReturn(Optional.empty());
        when(mapper.toEntity(dto)).thenReturn(offer);
        when(repository.save(offer)).thenReturn(offer);
        when(mapper.toResponseDTO(offer)).thenReturn(expected);

        OfferResponseDTO result = offerService.createOfferType(dto);

        assertThat(result.name()).isEqualTo("Duo");
    }

    @Test
    void createOfferType_throwsOfferTypeAlreadyExistsException_whenNameTaken() {
        CreateOfferDTO dto = new CreateOfferDTO("Solo", "Description", 49.0, 1, true, 1, List.of());
        when(repository.findByName("Solo")).thenReturn(Optional.of(new Offer()));

        assertThatThrownBy(() -> offerService.createOfferType(dto))
                .isInstanceOf(OfferTypeAlreadyExistsException.class);
    }

    // ── createOfferTypeBulk ───────────────────────────────────────────────────

    @Test
    void createOfferTypeBulk_savesAllAndReturnsDTOs() {
        CreateOfferDTO dto1 = new CreateOfferDTO("Solo", "Description", 49.0, 1, true, 1, List.of());
        CreateOfferDTO dto2 = new CreateOfferDTO("Duo", "Description", 79.0, 2, true, 2, List.of());
        Offer offer1 = new Offer();
        offer1.setId(1L);
        offer1.setName("Solo");
        Offer offer2 = new Offer();
        offer2.setId(2L);
        offer2.setName("Duo");
        OfferResponseDTO r1 = OfferResponseDTO.builder().id(1L).name("Solo").price(49.0).features(List.of()).build();
        OfferResponseDTO r2 = OfferResponseDTO.builder().id(2L).name("Duo").price(79.0).features(List.of()).build();

        when(mapper.toEntity(dto1)).thenReturn(offer1);
        when(mapper.toEntity(dto2)).thenReturn(offer2);
        when(repository.saveAll(List.of(offer1, offer2))).thenReturn(List.of(offer1, offer2));
        when(mapper.toResponseDTO(offer1)).thenReturn(r1);
        when(mapper.toResponseDTO(offer2)).thenReturn(r2);

        List<OfferResponseDTO> result = offerService.createOfferTypeBulk(List.of(dto1, dto2));

        assertThat(result).hasSize(2);
        assertThat(result.get(0).name()).isEqualTo("Solo");
        assertThat(result.get(1).name()).isEqualTo("Duo");
    }
}
