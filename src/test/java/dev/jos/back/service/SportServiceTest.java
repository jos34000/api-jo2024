package dev.jos.back.service;

import dev.jos.back.dto.sport.CreateSportDTO;
import dev.jos.back.dto.sport.SportResponseDTO;
import dev.jos.back.dto.sport.UpdateSportDTO;
import dev.jos.back.entities.Sport;
import dev.jos.back.entities.SportTranslation;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
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

    // ── getSportByName ────────────────────────────────────────────────────────

    @Test
    void getSportByName_returnsDTOWithOwnName_forFrLocale() {
        Sport sport = new Sport();
        sport.setId(1L);
        sport.setName("Natation");
        sport.setDescription("Description");
        SportResponseDTO expected = SportResponseDTO.builder().id(1L).name("Natation").phases(List.of()).places(List.of()).build();

        when(sportRepository.findByName("Natation")).thenReturn(Optional.of(sport));
        when(sportMapper.toDto(sport, "Natation", "Description")).thenReturn(expected);

        SportResponseDTO result = sportService.getSportByName("Natation", "fr");

        assertThat(result.name()).isEqualTo("Natation");
        verify(sportTranslationRepository, never()).findBySport_IdAndLocale(any(), any());
    }

    @Test
    void getSportByName_returnsTranslatedDTO_forNonFrLocale() {
        Sport sport = new Sport();
        sport.setId(1L);
        sport.setName("Natation");
        sport.setDescription("Description");

        SportTranslation translation = new SportTranslation();
        translation.setName("Swimming");
        translation.setDescription("Swimming desc");

        SportResponseDTO expected = SportResponseDTO.builder().id(1L).name("Swimming").phases(List.of()).places(List.of()).build();

        when(sportRepository.findByName("Natation")).thenReturn(Optional.of(sport));
        when(sportTranslationRepository.findBySport_IdAndLocale(1L, "en"))
                .thenReturn(Optional.of(translation));
        when(sportMapper.toDto(sport, "Swimming", "Swimming desc")).thenReturn(expected);

        SportResponseDTO result = sportService.getSportByName("Natation", "en");

        assertThat(result.name()).isEqualTo("Swimming");
    }

    @Test
    void getSportByName_throwsSportNotFoundException_whenNotFound() {
        when(sportRepository.findByName("Inconnu")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sportService.getSportByName("Inconnu", "fr"))
                .isInstanceOf(SportNotFoundException.class);
    }

    // ── getSportById ──────────────────────────────────────────────────────────

    @Test
    void getSportById_returnsDTOWithOwnName_forFrLocale() {
        Sport sport = new Sport();
        sport.setId(1L);
        sport.setName("Natation");
        sport.setDescription("Description");
        SportResponseDTO expected = SportResponseDTO.builder().id(1L).name("Natation").phases(List.of()).places(List.of()).build();

        when(sportRepository.findById(1L)).thenReturn(Optional.of(sport));
        when(sportMapper.toDto(sport, "Natation", "Description")).thenReturn(expected);

        SportResponseDTO result = sportService.getSportById(1L, "fr");

        assertThat(result.name()).isEqualTo("Natation");
        verify(sportTranslationRepository, never()).findBySport_IdAndLocale(any(), any());
    }

    @Test
    void getSportById_returnsTranslatedDTO_forNonFrLocale() {
        Sport sport = new Sport();
        sport.setId(1L);
        sport.setName("Natation");
        sport.setDescription("Description");

        SportTranslation translation = new SportTranslation();
        translation.setName("Swimming");
        translation.setDescription("Swimming desc");

        SportResponseDTO expected = SportResponseDTO.builder().id(1L).name("Swimming").phases(List.of()).places(List.of()).build();

        when(sportRepository.findById(1L)).thenReturn(Optional.of(sport));
        when(sportTranslationRepository.findBySport_IdAndLocale(1L, "en"))
                .thenReturn(Optional.of(translation));
        when(sportMapper.toDto(sport, "Swimming", "Swimming desc")).thenReturn(expected);

        SportResponseDTO result = sportService.getSportById(1L, "en");

        assertThat(result.name()).isEqualTo("Swimming");
    }

    @Test
    void getSportById_throwsSportNotFoundException_whenNotFound() {
        when(sportRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sportService.getSportById(99L, "fr"))
                .isInstanceOf(SportNotFoundException.class);
    }

    // ── getAllSports ──────────────────────────────────────────────────────────

    @Test
    void getAllSports_doesNotQueryTranslations_forFrLocale() {
        when(sportRepository.findAll()).thenReturn(List.of());

        List<SportResponseDTO> result = sportService.getAllSports("fr");

        assertThat(result).isEmpty();
        verify(sportTranslationRepository, never()).findBySportIdsAndLocale(any(), any());
    }

    @Test
    void getAllSports_queriesTranslationsAndMergesNames_forNonFrLocale() {
        Sport sport = new Sport();
        sport.setId(1L);
        sport.setName("Natation");
        sport.setDescription("Description");

        SportTranslation translation = new SportTranslation();
        translation.setSport(sport);
        translation.setName("Swimming");
        translation.setDescription("Swimming desc");

        SportResponseDTO expected = SportResponseDTO.builder().id(1L).name("Swimming").phases(List.of()).places(List.of()).build();

        when(sportRepository.findAll()).thenReturn(List.of(sport));
        when(sportTranslationRepository.findBySportIdsAndLocale(List.of(1L), "en"))
                .thenReturn(List.of(translation));
        when(sportMapper.toDto(sport, "Swimming", "Swimming desc")).thenReturn(expected);

        List<SportResponseDTO> result = sportService.getAllSports("en");

        assertThat(result.get(0).name()).isEqualTo("Swimming");
        verify(sportTranslationRepository).findBySportIdsAndLocale(List.of(1L), "en");
    }

    // ── createSportsBulk ──────────────────────────────────────────────────────

    @Test
    void createSportsBulk_savesNewSports() {
        CreateSportDTO dto = new CreateSportDTO("Natation", "Description", "icon", List.of());
        Sport sport = new Sport();
        sport.setName("Natation");
        SportResponseDTO expected = SportResponseDTO.builder().id(1L).name("Natation").phases(List.of()).places(List.of()).build();

        when(sportRepository.findByName("Natation")).thenReturn(Optional.empty());
        when(sportMapper.toEntity(dto)).thenReturn(sport);
        when(sportRepository.saveAll(List.of(sport))).thenReturn(List.of(sport));
        when(sportMapper.toDto(sport)).thenReturn(expected);

        List<SportResponseDTO> result = sportService.createSportsBulk(List.of(dto));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("Natation");
    }

    @Test
    void createSportsBulk_skipsExistingSports() {
        CreateSportDTO dto = new CreateSportDTO("Natation", "Description", "icon", List.of());

        when(sportRepository.findByName("Natation")).thenReturn(Optional.of(new Sport()));
        when(sportRepository.saveAll(List.of())).thenReturn(List.of());

        List<SportResponseDTO> result = sportService.createSportsBulk(List.of(dto));

        assertThat(result).isEmpty();
        verify(sportMapper, never()).toEntity(any());
    }
}
