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

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    @PostMapping("/bulk")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BulkEventResponseDTO> createEventsBulk(
            @Valid @RequestBody List<CreateEventDTO> events) {

        List<EventResponseDTO> created = eventService.createEventsBulk(events);

        BulkEventResponseDTO response = new BulkEventResponseDTO(
                created.size(),
                events.size() - created.size(),
                created
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EventResponseDTO> createEvent(
            @Valid @RequestBody CreateEventDTO event) {

        EventResponseDTO created = eventService.createEvent(event);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<EventResponseDTO>> getAllEvents() {
        List<EventResponseDTO> events = eventService.getAllEvents();
        return ResponseEntity.ok(events);
    }

    @GetMapping("/all")
    public ResponseEntity<List<EventResponseDTO>> getAll() {
        List<EventResponseDTO> events = eventService.getAll();
        return ResponseEntity.ok(events);
    }

    @GetMapping("/active")
    public ResponseEntity<List<EventResponseDTO>> getActiveEvents() {
        List<EventResponseDTO> events = eventService.getActiveEvents();
        return ResponseEntity.ok(events);
    }

    @GetMapping("/available")
    public ResponseEntity<List<EventResponseDTO>> getAvailableEvents() {
        List<EventResponseDTO> events = eventService.getAvailableEvents();
        return ResponseEntity.ok(events);
    }


    @GetMapping("/{id}")
    public ResponseEntity<EventResponseDTO> getEventById(@PathVariable Long id) {
        EventResponseDTO event = eventService.getEventById(id);
        return ResponseEntity.ok(event);
    }
}