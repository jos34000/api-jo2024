package dev.jos.back.service;

import dev.jos.back.dto.offer.OfferResponseDTO;
import dev.jos.back.dto.offer.UpdateOfferDTO;
import dev.jos.back.entities.Offer;
import dev.jos.back.exceptions.offertype.OfferNotFoundException;
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
}
