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
     *
     * @param events liste des événements à créer
     * @return {@code ResponseEntity<BulkEventResponseDTO>} contenant le résumé de la création
     * (nombre total d'événements créés et liste détaillée des événements)
     * @throws jakarta.validation.ConstraintViolationException           si un ou plusieurs événements
     *                                                                   contiennent des données invalides
     * @throws org.springframework.security.access.AccessDeniedException si l'utilisateur
     *                                                                   n'a pas le rôle ADMIN
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
     * @param event les informations de l'événement à créer (titre, description, dates, etc.)
     * @return {@code ResponseEntity<EventResponseDTO>} contenant l'événement créé avec son identifiant généré
     * @throws jakarta.validation.ConstraintViolationException           si les données de l'événement sont invalides
     * @throws org.springframework.security.access.AccessDeniedException si l'utilisateur
     *                                                                   n'a pas le rôle ADMIN
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
     * @return {@code ResponseEntity<List<EventResponseDTO>>} contenant la liste complète
     * de tous les événements
     */
    @GetMapping("/all")
    public ResponseEntity<List<EventResponseDTO>> getAll() {
        List<EventResponseDTO> events = eventService.getAll();
        return ResponseEntity.ok(events);
    }

    /**
     * Récupère uniquement les événements actifs.
     *
     * @return {@code ResponseEntity<List<EventResponseDTO>>} contenant la liste des événements actifs
     *
     */
    @GetMapping("/active")
    public ResponseEntity<List<EventResponseDTO>> getActiveEvents() {
        List<EventResponseDTO> events = eventService.getActiveEvents();
        return ResponseEntity.ok(events);
    }

    /**
     * Récupère uniquement les événements disponibles.
     *
     * @return {@code ResponseEntity<List<EventResponseDTO>>} contenant la liste des événements disponibles
     *
     */
    @GetMapping("/available")
    public ResponseEntity<List<EventResponseDTO>> getAvailableEvents() {
        List<EventResponseDTO> events = eventService.getAvailableEvents();
        return ResponseEntity.ok(events);
    }

    /**
     * Récupère un événement par son identifiant.
     *
     * @param id l'identifiant unique de l'événement
     * @return {@code ResponseEntity<EventResponseDTO>} contenant les détails complets de l'événement
     * @throws dev.jos.back.exceptions.event.EventNotFoundException si aucun événement
     *                                                              ne correspond à cet identifiant
     */
    @GetMapping("/{id}")
    public ResponseEntity<EventResponseDTO> getEventById(@PathVariable Long id) {
        EventResponseDTO event = eventService.getEventById(id);
        return ResponseEntity.ok(event);
    }
}