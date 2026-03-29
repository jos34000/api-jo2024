package dev.jos.back.service;

import dev.jos.back.dto.event.EventResponseDTO;
import dev.jos.back.dto.event.UpdateEventDTO;
import dev.jos.back.entities.Event;
import dev.jos.back.entities.Sport;
import dev.jos.back.exceptions.event.EventNotFoundException;
import dev.jos.back.mapper.EventMapper;
import dev.jos.back.repository.EventRepository;
import dev.jos.back.repository.EventTranslationRepository;
import dev.jos.back.repository.SportRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
}
