package dev.jos.back.mapper;

import dev.jos.back.dto.event.CreateEventDTO;
import dev.jos.back.dto.event.EventResponseDTO;
import dev.jos.back.entities.Event;
import org.springframework.stereotype.Component;

@Component
public class EventMapper {
    public EventResponseDTO toResponseDTO(Event event) {
        return toResponseDTO(event, event.getName(), event.getDescription());
    }

    public EventResponseDTO toResponseDTO(Event event, String name, String description) {
        return EventResponseDTO.builder()
                .id(event.getId())
                .name(name)
                .description(description)
                .icon(event.getIcon())
                .category(event.getCategory())
                .phase(event.getPhase())
                .location(event.getLocation())
                .city(event.getCity())
                .eventDate(event.getEventDate())
                .capacity(event.getCapacity())
                .availableSlots(event.getAvailableSlots())
                .isActive(event.getIsActive())
                .sport(event.getSport().getName())
                .build();
    }

    public Event toEntity(CreateEventDTO dto) {
        return Event.builder()
                .name(dto.name())
                .description(dto.description())
                .icon(dto.icon())
                .category(dto.category())
                .phase(dto.phase())
                .location(dto.location())
                .city(dto.city())
                .eventDate(dto.eventDate())
                .capacity(dto.capacity())
                .availableSlots(dto.availableSlots())
                .isActive(dto.isActive())
                .build();
    }
}
