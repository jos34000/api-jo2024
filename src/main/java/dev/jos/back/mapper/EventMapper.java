package dev.jos.back.mapper;

import dev.jos.back.dto.event.CreateEventDTO;
import dev.jos.back.dto.event.EventResponseDTO;
import dev.jos.back.model.Event;
import org.springframework.stereotype.Component;

@Component
public class EventMapper {
    public EventResponseDTO toResponseDTO(Event event) {
        return EventResponseDTO.builder()
                .name(event.getName())
                .description(event.getDescription())
                .location(event.getLocation())
                .eventDate(event.getEventDate())
                .capacity(event.getCapacity())
                .availableSlots(event.getAvailableSlots())
                .isActive(event.getIsActive())
                .build();
    }

    public Event toEntity(CreateEventDTO dto) {
        return Event.builder()
                .name(dto.name())
                .description(dto.description())
                .location(dto.location())
                .eventDate(dto.eventDate())
                .capacity(dto.capacity())
                .availableSlots(dto.availableSlots())
                .isActive(dto.isActive())
                .build();
    }
}
