package dev.jos.back.mapper;

import dev.jos.back.dto.sport.SportResponseDTO;
import dev.jos.back.entities.Sport;
import org.springframework.stereotype.Component;

@Component
public class SportMapper {
    public SportResponseDTO toDto(Sport sport) {
        return SportResponseDTO.builder()
                .name(sport.getName())
                .description(sport.getDescription())
                .phases(sport.getPhases())
                .eventCount(sport.getEvents().size())
                .build();
    }
}
