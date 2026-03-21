package dev.jos.back.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.jos.back.dto.offer.CreateOfferDTO;
import dev.jos.back.dto.offer.OfferResponseDTO;
import dev.jos.back.entities.Offer;
import dev.jos.back.entities.OfferTranslation;
import dev.jos.back.exceptions.offertype.OfferTypeAlreadyExistsException;
import dev.jos.back.mapper.OfferMapper;
import dev.jos.back.repository.OfferRepository;
import dev.jos.back.repository.OfferTranslationRepository;
import dev.jos.back.util.LocaleResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OfferService {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final OfferRepository repository;
    private final OfferMapper mapper;
    private final OfferTranslationRepository offerTranslationRepository;

    private List<String> deserializeFeatures(String json, List<String> fallback) {
        try {
            return OBJECT_MAPPER.readValue(json, new TypeReference<>() {});
        } catch (Exception e) {
            log.warn("Failed to deserialize offer features: {}", e.getMessage());
            return fallback;
        }
    }

    private Map<Long, OfferTranslation> getTranslationMap(List<Offer> offers, String locale) {
        if ("fr".equals(locale)) return Collections.emptyMap();
        List<Long> ids = offers.stream().map(Offer::getId).toList();
        return offerTranslationRepository.findByOfferIdsAndLocale(ids, locale).stream()
                .collect(Collectors.toMap(t -> t.getOffer().getId(), t -> t));
    }

    private OfferResponseDTO mapWithLocale(Offer offer, Map<Long, OfferTranslation> translationMap) {
        OfferTranslation t = translationMap.get(offer.getId());
        if (t == null) return mapper.toResponseDTO(offer);
        List<String> features = deserializeFeatures(t.getFeatures(), offer.getFeatures());
        return mapper.toResponseDTO(offer, t.getName(), t.getDescription(), features);
    }

    public List<OfferResponseDTO> getAllOfferTypes(String locale) {
        String lang = LocaleResolver.resolve(locale);
        List<Offer> offers = repository.findAll();
        Map<Long, OfferTranslation> translations = getTranslationMap(offers, lang);
        return offers.stream().map(o -> mapWithLocale(o, translations)).toList();
    }

    public OfferResponseDTO createOfferType(CreateOfferDTO dto) {
        if (repository.findByName(dto.name()).isPresent()) {
            log.warn("Offre ignorée - déjà existante : {}", dto.name());
            throw new OfferTypeAlreadyExistsException("Cette offre existe déjà.");
        }
        Offer offerType = mapper.toEntity(dto);
        Offer savedOfferType = repository.save(offerType);
        return mapper.toResponseDTO(savedOfferType);
    }

    public List<OfferResponseDTO> createOfferTypeBulk(List<CreateOfferDTO> dtoRequest) {
        List<Offer> offersToSave = new ArrayList<>();
        dtoRequest.forEach(dto -> offersToSave.add(mapper.toEntity(dto)));
        List<Offer> savedOfferTypes = repository.saveAll(offersToSave);
        return savedOfferTypes.stream().map(mapper::toResponseDTO).toList();
    }
}
