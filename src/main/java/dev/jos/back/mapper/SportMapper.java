package dev.jos.back.mapper;

import dev.jos.back.dto.sport.CreateSportDTO;
import dev.jos.back.dto.sport.SportResponseDTO;
import dev.jos.back.entities.Event;
import dev.jos.back.entities.Sport;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SportMapper {
    public SportResponseDTO toDto(Sport sport) {
        List<Event> events = sport.getEvents() != null ? sport.getEvents() : List.of();

        List<String> locations = events.stream()
                .map(Event::getLocation)
                .distinct()
                .sorted()
                .toList();

        return SportResponseDTO.builder()
                .id(sport.getId())
                .name(sport.getName())
                .description(sport.getDescription())
                .icon(sport.getIcon())
                .phases(sport.getPhases())
                .eventCount(events.size())
                .places(locations)
                .build();
    }

    public Sport toEntity(CreateSportDTO dto) {
        return Sport.builder()
                .name(dto.name())
                .description(dto.description())
                .icon(dto.icon())
                .phases(dto.phases())
                .build();
    }
}
