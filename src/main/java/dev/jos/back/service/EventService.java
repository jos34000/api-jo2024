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
import dev.jos.back.util.LocaleResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final EventMapper eventMapper;
    private final SportRepository sportRepository;
    private final EventTranslationRepository eventTranslationRepository;

    private Map<Long, EventTranslation> getTranslationMap(List<Event> events, String locale) {
        if ("fr".equals(locale)) return Collections.emptyMap();
        List<Long> ids = events.stream().map(Event::getId).toList();
        return eventTranslationRepository.findByEventIdsAndLocale(ids, locale).stream()
                .collect(Collectors.toMap(t -> t.getEvent().getId(), t -> t));
    }

    private EventResponseDTO mapWithLocale(Event event, Map<Long, EventTranslation> translationMap) {
        EventTranslation t = translationMap.get(event.getId());
        String name = (t != null && t.getName() != null) ? t.getName() : event.getName();
        String description = t != null ? t.getDescription() : event.getDescription();
        return eventMapper.toResponseDTO(event, name, description);
    }

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

    public List<EventResponseDTO> getAllEvents(String locale) {
        String lang = LocaleResolver.resolve(locale);
        List<Event> events = eventRepository.findAll();
        Map<Long, EventTranslation> translations = getTranslationMap(events, lang);
        return events.stream().map(e -> mapWithLocale(e, translations)).toList();
    }

    public List<EventResponseDTO> getActiveEvents(String locale) {
        String lang = LocaleResolver.resolve(locale);
        List<Event> events = eventRepository.findByIsActiveTrue();
        Map<Long, EventTranslation> translations = getTranslationMap(events, lang);
        return events.stream().map(e -> mapWithLocale(e, translations)).toList();
    }

    public List<EventResponseDTO> getAvailableEvents(String locale) {
        String lang = LocaleResolver.resolve(locale);
        List<Event> events = eventRepository.findAvailableEvents();
        Map<Long, EventTranslation> translations = getTranslationMap(events, lang);
        return events.stream().map(e -> mapWithLocale(e, translations)).toList();
    }

    public EventResponseDTO getEventById(Long id, String locale) {
        String lang = LocaleResolver.resolve(locale);
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new EventNotFoundException("Événement non trouvé"));
        EventTranslation t = "fr".equals(lang) ? null :
                eventTranslationRepository.findByEvent_IdAndLocale(event.getId(), lang).orElse(null);
        String name = (t != null && t.getName() != null) ? t.getName() : event.getName();
        String description = t != null ? t.getDescription() : event.getDescription();
        return eventMapper.toResponseDTO(event, name, description);
    }

    public List<EventResponseDTO> getAll(String locale) {
        return getAllEvents(locale);
    }

    public List<EventResponseDTO> getEventsBySport(String sport, String locale) {
        String lang = LocaleResolver.resolve(locale);
        List<Event> events = eventRepository.findBySportName(sport);
        Map<Long, EventTranslation> translations = getTranslationMap(events, lang);
        return events.stream().map(e -> mapWithLocale(e, translations)).toList();
    }

    @Transactional
    public EventResponseDTO updateEvent(Long id, UpdateEventDTO dto) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new EventNotFoundException("Événement non trouvé"));
        if (dto.name() != null) event.setName(dto.name());
        if (dto.description() != null) event.setDescription(dto.description());
        if (dto.icon() != null) event.setIcon(dto.icon());
        if (dto.category() != null) event.setCategory(dto.category());
        if (dto.phase() != null) event.setPhase(dto.phase());
        if (dto.location() != null) event.setLocation(dto.location());
        if (dto.city() != null) event.setCity(dto.city());
        if (dto.eventDate() != null) event.setEventDate(dto.eventDate());
        if (dto.capacity() != null) event.setCapacity(dto.capacity());
        if (dto.availableSlots() != null) event.setAvailableSlots(dto.availableSlots());
        if (dto.isActive() != null) event.setIsActive(dto.isActive());
        if (dto.sport() != null) {
            Sport sport = sportRepository.findByName(dto.sport())
                    .orElseThrow(() -> new SportNotFoundException("Sport non trouvé : " + dto.sport()));
            event.setSport(sport);
        }
        return eventMapper.toResponseDTO(eventRepository.saveAndFlush(event));
    }

    @Transactional
    public void deleteEvent(Long id) {
        if (!eventRepository.existsById(id)) {
            throw new EventNotFoundException("Événement non trouvé");
        }
        eventRepository.deleteById(id);
    }
}
