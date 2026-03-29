package dev.jos.back.service;

import dev.jos.back.dto.sport.SportResponseDTO;
import dev.jos.back.dto.sport.UpdateSportDTO;
import dev.jos.back.entities.Sport;
import dev.jos.back.exceptions.sport.SportNotFoundException;
import dev.jos.back.mapper.SportMapper;
import dev.jos.back.repository.SportRepository;
import dev.jos.back.repository.SportTranslationRepository;
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
class SportServiceTest {

    @Mock SportRepository sportRepository;
    @Mock SportMapper sportMapper;
    @Mock SportTranslationRepository sportTranslationRepository;
    @InjectMocks SportService sportService;

    @Test
    void updateSport_updatesNameAndReturnsDTO() {
        Sport sport = new Sport();
        sport.setId(1L);
        sport.setName("Ancien");

        UpdateSportDTO dto = new UpdateSportDTO("Nouveau", null, null, null, null);

        when(sportRepository.findById(1L)).thenReturn(Optional.of(sport));
        when(sportRepository.saveAndFlush(sport)).thenReturn(sport);
        SportResponseDTO expected = SportResponseDTO.builder().id(1L).name("Nouveau").phases(List.of()).places(List.of()).build();
        when(sportMapper.toDto(sport)).thenReturn(expected);

        SportResponseDTO result = sportService.updateSport(1L, dto);

        assertThat(result.name()).isEqualTo("Nouveau");
        assertThat(sport.getName()).isEqualTo("Nouveau");
    }

    @Test
    void updateSport_throwsSportNotFoundException_whenNotFound() {
        UpdateSportDTO dto = new UpdateSportDTO(null, null, null, null, null);
        when(sportRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sportService.updateSport(99L, dto))
                .isInstanceOf(SportNotFoundException.class);
    }

    @Test
    void deleteSport_callsDeleteById() {
        when(sportRepository.existsById(1L)).thenReturn(true);

        sportService.deleteSport(1L);

        verify(sportRepository).deleteById(1L);
    }

    @Test
    void deleteSport_throwsSportNotFoundException_whenNotFound() {
        when(sportRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> sportService.deleteSport(99L))
                .isInstanceOf(SportNotFoundException.class);
    }
}
