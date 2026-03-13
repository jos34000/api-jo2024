package dev.jos.back.service;

import dev.jos.back.dto.sport.CreateSportDTO;
import dev.jos.back.dto.sport.SportResponseDTO;
import dev.jos.back.entities.Sport;
import dev.jos.back.exceptions.sport.SportNotFoundException;
import dev.jos.back.mapper.SportMapper;
import dev.jos.back.repository.SportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

    public SportResponseDTO getSportById(Long id) {
        Sport sport = sportRepository.findById(id).orElseThrow(() -> new SportNotFoundException(String.format("Sport %s non trouvé", id)));
        return sportMapper.toDto(sport);
    }

    public List<SportResponseDTO> getAllSports() {
        List<Sport> sports = sportRepository.findAll();
        return sports.stream().map(sportMapper::toDto).toList();
    }

    @Transactional
    public List<SportResponseDTO> createSportsBulk(List<CreateSportDTO> sportsDto) {

        List<Sport> sportsToSave = new ArrayList<>();

        sportsDto.forEach(dto -> {
            Optional<Sport> existing = sportRepository.findByName(dto.name());
            if (existing.isPresent()) {
                log.warn("Sport ignoré - déjà existant : {}", dto.name());
                return;
            }
            Sport sport = sportMapper.toEntity(dto);
            sportsToSave.add(sport);
        });

        List<Sport> savedSports = sportRepository.saveAll(sportsToSave);
        return savedSports.stream()
                .map(sportMapper::toDto)
                .toList();
    }
}
