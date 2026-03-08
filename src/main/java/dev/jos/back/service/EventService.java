package dev.jos.back.service;

import dev.jos.back.dto.event.CreateEventDTO;
import dev.jos.back.dto.event.EventResponseDTO;
import dev.jos.back.entities.Event;
import dev.jos.back.entities.Sport;
import dev.jos.back.exceptions.event.EventAlreadyExistsException;
import dev.jos.back.exceptions.event.EventNotFoundException;
import dev.jos.back.exceptions.sport.SportNotFoundException;
import dev.jos.back.mapper.EventMapper;
import dev.jos.back.repository.EventRepository;
import dev.jos.back.repository.SportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final EventMapper eventMapper;
    private final SportRepository sportRepository;

    @Transactional
    public List<EventResponseDTO> createEventsBulk(List<CreateEventDTO> eventsDto) {

        Map<String, Sport> sportsByName = sportRepository.findAll()
                .stream()
                .collect(Collectors.toMap(Sport::getName, s -> s));

        List<Event> eventsToSave = new ArrayList<>();

        eventsDto.forEach(dto -> {
            Optional<Event> existing = eventRepository.findByNameAndEventDate(
                    dto.name(),
                    dto.eventDate()
            );
            if (existing.isPresent()) {
                log.warn("Événement ignoré - déjà existant : {} le {}", dto.name(), dto.eventDate());
                return;
            }

            Sport sport = Optional.ofNullable(sportsByName.get(dto.sport()))
                    .orElseThrow(() -> new SportNotFoundException("Sport non trouvé : " + dto.sport()));

            Event event = eventMapper.toEntity(dto);
            event.setSport(sport);
            eventsToSave.add(event);
        });

        List<Event> savedEvents = eventRepository.saveAll(eventsToSave);
        return savedEvents.stream()
                .map(eventMapper::toResponseDTO)
                .toList();
    }

    @Transactional
    public EventResponseDTO createEvent(CreateEventDTO dto) {
        Optional<Event> existing = eventRepository.findByNameAndEventDate(
                dto.name(),
                dto.eventDate()
        );

        if (existing.isPresent()) {
            throw new EventAlreadyExistsException("Cet événement existe déjà");
        }

        Event event = new Event();
        event.setName(dto.name());
        event.setDescription(dto.description());
        event.setLocation(dto.location());
        event.setEventDate(dto.eventDate());
        event.setCapacity(dto.capacity());
        event.setAvailableSlots(
                dto.availableSlots() != null ? dto.availableSlots() : dto.capacity()
        );
        event.setIsActive(dto.isActive() == null || dto.isActive());

        Event saved = eventRepository.save(event);
        return eventMapper.toResponseDTO(saved);
    }

    public List<EventResponseDTO> getAllEvents() {
        return eventRepository.findAll().stream()
                .map(eventMapper::toResponseDTO)
                .toList();
    }

    public List<EventResponseDTO> getActiveEvents() {
        return eventRepository.findByIsActiveTrue().stream()
                .map(eventMapper::toResponseDTO)
                .toList();
    }

    public List<EventResponseDTO> getAvailableEvents() {
        return eventRepository.findAvailableEvents().stream()
                .map(eventMapper::toResponseDTO)
                .toList();
    }

    public EventResponseDTO getEventById(Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new EventNotFoundException("Événement non trouvé"));
        return eventMapper.toResponseDTO(event);
    }

    public List<EventResponseDTO> getAll() {
        return eventRepository.findAll().stream()
                .map(eventMapper::toResponseDTO)
                .toList();
    }

    public List<EventResponseDTO> getEventsBySport(String sport) {
        return eventRepository.findBySportName(sport).stream()
                .map(eventMapper::toResponseDTO)
                .toList();
    }
}