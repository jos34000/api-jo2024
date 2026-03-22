package dev.jos.back.service;

import dev.jos.back.dto.sport.CreateSportDTO;
import dev.jos.back.dto.sport.SportResponseDTO;
import dev.jos.back.entities.Sport;
import dev.jos.back.entities.SportTranslation;
import dev.jos.back.exceptions.sport.SportNotFoundException;
import dev.jos.back.mapper.SportMapper;
import dev.jos.back.repository.SportRepository;
import dev.jos.back.repository.SportTranslationRepository;
import dev.jos.back.util.LocaleResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class SportService {
    private final SportRepository sportRepository;
    private final SportMapper sportMapper;
    private final SportTranslationRepository sportTranslationRepository;

    private Map<Long, SportTranslation> getTranslationMap(List<Sport> sports, String locale) {
        if ("fr".equals(locale)) return Collections.emptyMap();
        List<Long> ids = sports.stream().map(Sport::getId).toList();
        return sportTranslationRepository.findBySportIdsAndLocale(ids, locale).stream()
                .collect(Collectors.toMap(t -> t.getSport().getId(), t -> t));
    }

    private SportResponseDTO mapWithLocale(Sport sport, Map<Long, SportTranslation> translationMap) {
        SportTranslation t = translationMap.get(sport.getId());
        String name = t != null ? t.getName() : sport.getName();
        String description = t != null ? t.getDescription() : sport.getDescription();
        return sportMapper.toDto(sport, name, description);
    }

    public SportResponseDTO getSportByName(String name, String locale) {
        String lang = LocaleResolver.resolve(locale);
        Sport sport = sportRepository.findByName(name)
                .orElseThrow(() -> new SportNotFoundException(String.format("Sport %s non trouvé", name)));
        SportTranslation t = "fr".equals(lang) ? null :
                sportTranslationRepository.findBySport_IdAndLocale(sport.getId(), lang).orElse(null);
        String translatedName = t != null ? t.getName() : sport.getName();
        String translatedDesc = t != null ? t.getDescription() : sport.getDescription();
        return sportMapper.toDto(sport, translatedName, translatedDesc);
    }

    public SportResponseDTO getSportById(Long id, String locale) {
        String lang = LocaleResolver.resolve(locale);
        Sport sport = sportRepository.findById(id)
                .orElseThrow(() -> new SportNotFoundException(String.format("Sport %s non trouvé", id)));
        SportTranslation t = "fr".equals(lang) ? null :
                sportTranslationRepository.findBySport_IdAndLocale(sport.getId(), lang).orElse(null);
        String translatedName = t != null ? t.getName() : sport.getName();
        String translatedDesc = t != null ? t.getDescription() : sport.getDescription();
        return sportMapper.toDto(sport, translatedName, translatedDesc);
    }

    public List<SportResponseDTO> getAllSports(String locale) {
        String lang = LocaleResolver.resolve(locale);
        List<Sport> sports = sportRepository.findAll();
        Map<Long, SportTranslation> translations = getTranslationMap(sports, lang);
        return sports.stream().map(s -> mapWithLocale(s, translations)).toList();
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
