package dev.jos.back.controller;

import dev.jos.back.dto.event.BulkEventResponseDTO;
import dev.jos.back.dto.event.CreateEventDTO;
import dev.jos.back.dto.event.EventResponseDTO;
import dev.jos.back.dto.event.UpdateEventDTO;
import dev.jos.back.service.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Contrôleur REST pour la gestion des événements.
 * Fournit des endpoints pour créer, consulter et filtrer les événements.
 * Supporte l'internationalisation via l'en-tête {@code Accept-Language}.
 *
 * @see EventService
 * @see EventResponseDTO
 */
@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    /**
     * Crée plusieurs événements en une seule opération.
     * Réservé aux administrateurs.
     */
    @PostMapping("/bulk")
    public ResponseEntity<BulkEventResponseDTO> createEventsBulk(
            @Valid @RequestBody List<CreateEventDTO> events) {

        List<EventResponseDTO> created = eventService.createEventsBulk(events);

        BulkEventResponseDTO response = new BulkEventResponseDTO(
                created.size(),
                created
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Crée un nouvel événement.
     * Réservé aux administrateurs.
     */
    @PostMapping
    public ResponseEntity<EventResponseDTO> createEvent(
            @Valid @RequestBody CreateEventDTO event) {

        EventResponseDTO created = eventService.createEvent(event);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Récupère tous les événements.
     *
     * @param locale locale extraite de l'en-tête {@code Accept-Language} (ex. {@code fr}, {@code en-US})
     */
    @GetMapping("/all")
    public ResponseEntity<List<EventResponseDTO>> getAll(
            @RequestHeader(value = "Accept-Language", defaultValue = "fr") String locale) {
        List<EventResponseDTO> events = eventService.getAll(locale);
        return ResponseEntity.ok(events);
    }

    /**
     * Récupère uniquement les événements actifs.
     *
     * @param locale locale extraite de l'en-tête {@code Accept-Language}
     */
    @GetMapping("/active")
    public ResponseEntity<List<EventResponseDTO>> getActiveEvents(
            @RequestHeader(value = "Accept-Language", defaultValue = "fr") String locale) {
        List<EventResponseDTO> events = eventService.getActiveEvents(locale);
        return ResponseEntity.ok(events);
    }

    /**
     * Récupère uniquement les événements disponibles.
     *
     * @param locale locale extraite de l'en-tête {@code Accept-Language}
     */
    @GetMapping("/available")
    public ResponseEntity<List<EventResponseDTO>> getAvailableEvents(
            @RequestHeader(value = "Accept-Language", defaultValue = "fr") String locale) {
        List<EventResponseDTO> events = eventService.getAvailableEvents(locale);
        return ResponseEntity.ok(events);
    }

    /**
     * Récupère un événement par son identifiant.
     *
     * @param id     l'identifiant unique de l'événement
     * @param locale locale extraite de l'en-tête {@code Accept-Language}
     */
    @GetMapping("/{id}")
    public ResponseEntity<EventResponseDTO> getEventById(
            @PathVariable Long id,
            @RequestHeader(value = "Accept-Language", defaultValue = "fr") String locale) {
        EventResponseDTO event = eventService.getEventById(id, locale);
        return ResponseEntity.ok(event);
    }

    /**
     * Récupère la liste des événements du même sport.
     *
     * @param sport  le nom du sport recherché
     * @param locale locale extraite de l'en-tête {@code Accept-Language}
     */
    @GetMapping("/sport/{sport}")
    public ResponseEntity<List<EventResponseDTO>> getEventsBySport(
            @PathVariable String sport,
            @RequestHeader(value = "Accept-Language", defaultValue = "fr") String locale) {
        List<EventResponseDTO> events = eventService.getEventsBySport(sport, locale);
        return ResponseEntity.ok(events);
    }

    /**
     * Met à jour un événement existant.
     * Réservé aux administrateurs.
     */
    @PutMapping("/{id}")
    public ResponseEntity<EventResponseDTO> updateEvent(
            @PathVariable Long id,
            @Valid @RequestBody UpdateEventDTO dto) {
        EventResponseDTO updated = eventService.updateEvent(id, dto);
        return ResponseEntity.ok(updated);
    }

    /**
     * Supprime un événement par son identifiant.
     * Réservé aux administrateurs.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long id) {
        eventService.deleteEvent(id);
        return ResponseEntity.noContent().build();
    }
}
