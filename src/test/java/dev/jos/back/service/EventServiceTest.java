package dev.jos.back.service;

import dev.jos.back.dto.event.CreateEventDTO;
import dev.jos.back.dto.event.EventResponseDTO;
import dev.jos.back.dto.event.UpdateEventDTO;
import dev.jos.back.entities.Event;
import dev.jos.back.entities.EventTranslation;
import dev.jos.back.entities.Sport;
import dev.jos.back.exceptions.event.EventAlreadyExistsException;
import dev.jos.back.exceptions.event.EventNotFoundException;
import dev.jos.back.exceptions.sport.SportNotFoundException;
import dev.jos.back.mapper.EventMapper;
import dev.jos.back.repository.EventRepository;
import dev.jos.back.repository.EventTranslationRepository;
import dev.jos.back.repository.SportRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @Mock EventRepository eventRepository;
    @Mock EventMapper eventMapper;
    @Mock SportRepository sportRepository;
    @Mock EventTranslationRepository eventTranslationRepository;
    @InjectMocks EventService eventService;

    @Test
    void updateEvent_updatesNameAndReturnsDTO() {
        Sport sport = new Sport();
        sport.setName("Natation");
        Event event = new Event();
        event.setId(1L);
        event.setName("Ancien nom");
        event.setSport(sport);

        UpdateEventDTO dto = new UpdateEventDTO("Nouveau nom", null, null, null, null, null, null, null, null, null, null, null);

        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(eventRepository.saveAndFlush(event)).thenReturn(event);
        EventResponseDTO expected = EventResponseDTO.builder().id(1L).name("Nouveau nom").sport("Natation").build();
        when(eventMapper.toResponseDTO(event)).thenReturn(expected);

        EventResponseDTO result = eventService.updateEvent(1L, dto);

        assertThat(result.name()).isEqualTo("Nouveau nom");
        assertThat(event.getName()).isEqualTo("Nouveau nom");
    }

    @Test
    void updateEvent_throwsEventNotFoundException_whenNotFound() {
        UpdateEventDTO dto = new UpdateEventDTO(null, null, null, null, null, null, null, null, null, null, null, null);
        when(eventRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> eventService.updateEvent(99L, dto))
                .isInstanceOf(EventNotFoundException.class);
    }

    @Test
    void deleteEvent_callsDeleteById() {
        when(eventRepository.existsById(1L)).thenReturn(true);

        eventService.deleteEvent(1L);

        verify(eventRepository).deleteById(1L);
    }

    @Test
    void deleteEvent_throwsEventNotFoundException_whenNotFound() {
        when(eventRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> eventService.deleteEvent(99L))
                .isInstanceOf(EventNotFoundException.class);
    }

    // ── createEvent ───────────────────────────────────────────────────────────

    @Test
    void createEvent_savesAndReturnsDTO() {
        LocalDateTime date = LocalDateTime.of(2024, 7, 26, 10, 0);
        CreateEventDTO dto = new CreateEventDTO(
                "100m Hommes", "Une description longue", "icon", "Athlétisme",
                null, "Stade de France", "Paris", "Athlétisme", date, 1000, null, null
        );
        Event saved = new Event();
        saved.setId(1L);
        EventResponseDTO expected = EventResponseDTO.builder().id(1L).name("100m Hommes").build();

        when(eventRepository.findByNameAndEventDate("100m Hommes", date)).thenReturn(Optional.empty());
        when(eventRepository.save(any(Event.class))).thenReturn(saved);
        when(eventMapper.toResponseDTO(saved)).thenReturn(expected);

        EventResponseDTO result = eventService.createEvent(dto);

        assertThat(result.name()).isEqualTo("100m Hommes");
        verify(eventRepository).save(any(Event.class));
    }

    @Test
    void createEvent_throwsEventAlreadyExistsException_whenDuplicate() {
        LocalDateTime date = LocalDateTime.of(2024, 7, 26, 10, 0);
        CreateEventDTO dto = new CreateEventDTO(
                "100m Hommes", "Une description longue", "icon", "Athlétisme",
                null, "Stade de France", "Paris", "Athlétisme", date, 1000, null, null
        );
        when(eventRepository.findByNameAndEventDate("100m Hommes", date)).thenReturn(Optional.of(new Event()));

        assertThatThrownBy(() -> eventService.createEvent(dto))
                .isInstanceOf(EventAlreadyExistsException.class);
    }

    // ── createEventsBulk ──────────────────────────────────────────────────────

    @Test
    void createEventsBulk_skipsDuplicatesAndSavesNew() {
        LocalDateTime date = LocalDateTime.of(2024, 7, 26, 10, 0);
        Sport sport = new Sport();
        sport.setName("Natation");

        CreateEventDTO existing = new CreateEventDTO(
                "Existant", "desc", "icon", "cat", null, "lieu", "Paris", "Natation", date, 100, null, null
        );
        CreateEventDTO newDto = new CreateEventDTO(
                "Nouveau", "desc", "icon", "cat", null, "lieu", "Paris", "Natation", date, 100, null, null
        );

        Event mappedNew = new Event();
        mappedNew.setName("Nouveau");
        EventResponseDTO expectedDTO = EventResponseDTO.builder().id(2L).name("Nouveau").build();

        when(sportRepository.findAll()).thenReturn(List.of(sport));
        when(eventRepository.findByNameAndEventDate("Existant", date)).thenReturn(Optional.of(new Event()));
        when(eventRepository.findByNameAndEventDate("Nouveau", date)).thenReturn(Optional.empty());
        when(eventMapper.toEntity(newDto)).thenReturn(mappedNew);
        when(eventRepository.saveAll(List.of(mappedNew))).thenReturn(List.of(mappedNew));
        when(eventMapper.toResponseDTO(mappedNew)).thenReturn(expectedDTO);

        List<EventResponseDTO> result = eventService.createEventsBulk(List.of(existing, newDto));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("Nouveau");
    }

    @Test
    void createEventsBulk_throwsSportNotFoundException_whenSportUnknown() {
        LocalDateTime date = LocalDateTime.of(2024, 7, 26, 10, 0);
        CreateEventDTO dto = new CreateEventDTO(
                "Event", "desc", "icon", "cat", null, "lieu", "Paris", "InconnuSport", date, 100, null, null
        );

        when(sportRepository.findAll()).thenReturn(List.of());
        when(eventRepository.findByNameAndEventDate("Event", date)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> eventService.createEventsBulk(List.of(dto)))
                .isInstanceOf(SportNotFoundException.class);
    }

    // ── getAllEvents ──────────────────────────────────────────────────────────

    @Test
    void getAllEvents_doesNotQueryTranslations_forFrLocale() {
        when(eventRepository.findAll()).thenReturn(List.of());

        eventService.getAllEvents("fr");

        verify(eventTranslationRepository, never()).findByEventIdsAndLocale(any(), any());
    }

    @Test
    void getAllEvents_queriesTranslationsAndMergesNames_forNonFrLocale() {
        Event event = new Event();
        event.setId(1L);
        event.setName("Original");
        event.setDescription("Desc");

        EventTranslation translation = new EventTranslation();
        translation.setEvent(event);
        translation.setName("Translated");
        translation.setDescription("Translated desc");

        EventResponseDTO expected = EventResponseDTO.builder().id(1L).name("Translated").build();

        when(eventRepository.findAll()).thenReturn(List.of(event));
        when(eventTranslationRepository.findByEventIdsAndLocale(List.of(1L), "en"))
                .thenReturn(List.of(translation));
        when(eventMapper.toResponseDTO(event, "Translated", "Translated desc")).thenReturn(expected);

        List<EventResponseDTO> result = eventService.getAllEvents("en");

        assertThat(result.get(0).name()).isEqualTo("Translated");
        verify(eventTranslationRepository).findByEventIdsAndLocale(List.of(1L), "en");
    }

    // ── getActiveEvents ───────────────────────────────────────────────────────

    @Test
    void getActiveEvents_doesNotQueryTranslations_forFrLocale() {
        when(eventRepository.findByIsActiveTrue()).thenReturn(List.of());

        eventService.getActiveEvents("fr");

        verify(eventTranslationRepository, never()).findByEventIdsAndLocale(any(), any());
    }

    @Test
    void getActiveEvents_queriesTranslations_forNonFrLocale() {
        Event event = new Event();
        event.setId(1L);
        event.setName("Original");
        event.setDescription("Desc");

        EventTranslation translation = new EventTranslation();
        translation.setEvent(event);
        translation.setName("Translated");
        translation.setDescription("Translated desc");

        EventResponseDTO expected = EventResponseDTO.builder().id(1L).name("Translated").build();

        when(eventRepository.findByIsActiveTrue()).thenReturn(List.of(event));
        when(eventTranslationRepository.findByEventIdsAndLocale(List.of(1L), "en"))
                .thenReturn(List.of(translation));
        when(eventMapper.toResponseDTO(event, "Translated", "Translated desc")).thenReturn(expected);

        List<EventResponseDTO> result = eventService.getActiveEvents("en");

        assertThat(result).hasSize(1);
        verify(eventTranslationRepository).findByEventIdsAndLocale(List.of(1L), "en");
    }

    // ── getEventById ──────────────────────────────────────────────────────────

    @Test
    void getEventById_returnsDTOWithOwnName_forFrLocale() {
        Event event = new Event();
        event.setId(1L);
        event.setName("100m Hommes");
        event.setDescription("Description");
        EventResponseDTO expected = EventResponseDTO.builder().id(1L).name("100m Hommes").build();

        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(eventMapper.toResponseDTO(event, "100m Hommes", "Description")).thenReturn(expected);

        EventResponseDTO result = eventService.getEventById(1L, "fr");

        assertThat(result.name()).isEqualTo("100m Hommes");
        verify(eventTranslationRepository, never()).findByEvent_IdAndLocale(any(), any());
    }

    @Test
    void getEventById_returnsTranslatedName_forNonFrLocale() {
        Event event = new Event();
        event.setId(1L);
        event.setName("100m Hommes");
        event.setDescription("Description");

        EventTranslation translation = new EventTranslation();
        translation.setName("100m Men");
        translation.setDescription("Description EN");

        EventResponseDTO expected = EventResponseDTO.builder().id(1L).name("100m Men").build();

        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(eventTranslationRepository.findByEvent_IdAndLocale(1L, "en"))
                .thenReturn(Optional.of(translation));
        when(eventMapper.toResponseDTO(event, "100m Men", "Description EN")).thenReturn(expected);

        EventResponseDTO result = eventService.getEventById(1L, "en");

        assertThat(result.name()).isEqualTo("100m Men");
    }

    @Test
    void getEventById_throwsEventNotFoundException_whenNotFound() {
        when(eventRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> eventService.getEventById(99L, "fr"))
                .isInstanceOf(EventNotFoundException.class);
    }

    // ── getAvailableEvents ────────────────────────────────────────────────────

    @Test
    void getAvailableEvents_doesNotQueryTranslations_forFrLocale() {
        when(eventRepository.findAvailableEvents()).thenReturn(List.of());

        eventService.getAvailableEvents("fr");

        verify(eventTranslationRepository, never()).findByEventIdsAndLocale(any(), any());
    }

    @Test
    void getAvailableEvents_queriesTranslations_forNonFrLocale() {
        Event event = new Event();
        event.setId(1L);
        event.setName("Original");
        event.setDescription("Desc");

        EventTranslation translation = new EventTranslation();
        translation.setEvent(event);
        translation.setName("Translated");
        translation.setDescription("Translated desc");

        EventResponseDTO expected = EventResponseDTO.builder().id(1L).name("Translated").build();

        when(eventRepository.findAvailableEvents()).thenReturn(List.of(event));
        when(eventTranslationRepository.findByEventIdsAndLocale(List.of(1L), "en"))
                .thenReturn(List.of(translation));
        when(eventMapper.toResponseDTO(event, "Translated", "Translated desc")).thenReturn(expected);

        List<EventResponseDTO> result = eventService.getAvailableEvents("en");

        assertThat(result).hasSize(1);
        verify(eventTranslationRepository).findByEventIdsAndLocale(List.of(1L), "en");
    }

    // ── getEventsBySport ──────────────────────────────────────────────────────

    @Test
    void getEventsBySport_doesNotQueryTranslations_forFrLocale() {
        when(eventRepository.findBySportName("Natation")).thenReturn(List.of());

        eventService.getEventsBySport("Natation", "fr");

        verify(eventTranslationRepository, never()).findByEventIdsAndLocale(any(), any());
    }

    @Test
    void getEventsBySport_queriesTranslations_forNonFrLocale() {
        Event event = new Event();
        event.setId(1L);
        event.setName("Original");
        event.setDescription("Desc");

        EventTranslation translation = new EventTranslation();
        translation.setEvent(event);
        translation.setName("Translated");
        translation.setDescription("Translated desc");

        EventResponseDTO expected = EventResponseDTO.builder().id(1L).name("Translated").build();

        when(eventRepository.findBySportName("Natation")).thenReturn(List.of(event));
        when(eventTranslationRepository.findByEventIdsAndLocale(List.of(1L), "en"))
                .thenReturn(List.of(translation));
        when(eventMapper.toResponseDTO(event, "Translated", "Translated desc")).thenReturn(expected);

        List<EventResponseDTO> result = eventService.getEventsBySport("Natation", "en");

        assertThat(result).hasSize(1);
        verify(eventTranslationRepository).findByEventIdsAndLocale(List.of(1L), "en");
    }

    // ── updateEvent (supplement) ──────────────────────────────────────────────

    @Test
    void updateEvent_updatesSport_whenSportFieldProvided() {
        Sport newSport = new Sport();
        newSport.setName("Athlétisme");
        Event event = new Event();
        event.setId(1L);
        UpdateEventDTO dto = new UpdateEventDTO(null, null, null, null, null, null, null, "Athlétisme", null, null, null, null);
        EventResponseDTO expected = EventResponseDTO.builder().id(1L).sport("Athlétisme").build();

        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(sportRepository.findByName("Athlétisme")).thenReturn(Optional.of(newSport));
        when(eventRepository.saveAndFlush(event)).thenReturn(event);
        when(eventMapper.toResponseDTO(event)).thenReturn(expected);

        eventService.updateEvent(1L, dto);

        assertThat(event.getSport()).isEqualTo(newSport);
    }

    @Test
    void updateEvent_throwsSportNotFoundException_whenSportNameNotFound() {
        Event event = new Event();
        event.setId(1L);
        UpdateEventDTO dto = new UpdateEventDTO(null, null, null, null, null, null, null, "InconnuSport", null, null, null, null);

        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(sportRepository.findByName("InconnuSport")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> eventService.updateEvent(1L, dto))
                .isInstanceOf(SportNotFoundException.class);
    }
}
