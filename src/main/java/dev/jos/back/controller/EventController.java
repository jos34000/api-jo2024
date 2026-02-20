package dev.jos.back.controller;

import dev.jos.back.dto.event.BulkEventResponseDTO;
import dev.jos.back.dto.event.CreateEventDTO;
import dev.jos.back.dto.event.EventResponseDTO;
import dev.jos.back.service.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Contrôleur REST pour la gestion des événements.
 * Fournit des endpoints pour créer, consulter et filtrer les événements.
 */
@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    /**
     * Crée plusieurs événements en une seule opération.
     * Réservé aux administrateurs.
     *
     * @param events liste des événements à créer
     * @return ResponseEntity contenant le résumé de la création (nombre et liste des événements créés) (201 Created)
     */
    @PostMapping("/bulk")
    @PreAuthorize("hasRole('ADMIN')")
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
     *
     * @param event les informations de l'événement à créer
     * @return ResponseEntity contenant l'événement créé (201 Created)
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EventResponseDTO> createEvent(
            @Valid @RequestBody CreateEventDTO event) {

        EventResponseDTO created = eventService.createEvent(event);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Récupère tous les événements pour les utilisateurs publics.
     *
     * @return ResponseEntity contenant la liste de tous les événements (200 OK)
     */
    @GetMapping("/all")
    public ResponseEntity<List<EventResponseDTO>> getAll() {
        List<EventResponseDTO> events = eventService.getAll();
        return ResponseEntity.ok(events);
    }

    /**
     * Récupère uniquement les événements actifs.
     *
     * @return ResponseEntity contenant la liste des événements actifs (200 OK)
     */
    @GetMapping("/active")
    public ResponseEntity<List<EventResponseDTO>> getActiveEvents() {
        List<EventResponseDTO> events = eventService.getActiveEvents();
        return ResponseEntity.ok(events);
    }

    /**
     * Récupère uniquement les événements disponibles.
     *
     * @return ResponseEntity contenant la liste des événements disponibles (200 OK)
     */
    @GetMapping("/available")
    public ResponseEntity<List<EventResponseDTO>> getAvailableEvents() {
        List<EventResponseDTO> events = eventService.getAvailableEvents();
        return ResponseEntity.ok(events);
    }

    /**
     * Récupère un événement par son identifiant.
     *
     * @param id l'identifiant de l'événement
     * @return ResponseEntity contenant l'événement trouvé (200 OK)
     */
    @GetMapping("/{id}")
    public ResponseEntity<EventResponseDTO> getEventById(@PathVariable Long id) {
        EventResponseDTO event = eventService.getEventById(id);
        return ResponseEntity.ok(event);
    }
}