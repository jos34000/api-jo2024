package dev.jos.back.service;

import dev.jos.back.dto.sport.SportResponseDTO;
import dev.jos.back.entities.Sport;
import dev.jos.back.exceptions.sport.SportNotFoundException;
import dev.jos.back.mapper.SportMapper;
import dev.jos.back.repository.SportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class SportService {
    private final SportRepository sportRepository;
    private final SportMapper sportMapper;

    public SportResponseDTO getSportByName(String name) {
        Sport sport = sportRepository.findByName(name).orElseThrow(() -> new SportNotFoundException(String.format("Sport %s non trouvé", name)));
        return sportMapper.toDto(sport);
    }
}
