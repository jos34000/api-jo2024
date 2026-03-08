package dev.jos.back.service;

import dev.jos.back.dto.offertype.CreateOfferDTO;
import dev.jos.back.dto.offertype.OfferResponseDTO;
import dev.jos.back.entities.Offer;
import dev.jos.back.exceptions.offertype.OfferTypeAlreadyExistsException;
import dev.jos.back.mapper.OfferTypeMapper;
import dev.jos.back.repository.OfferRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OfferService {
    private final OfferRepository repository;
    private final OfferTypeMapper mapper;

    public List<OfferResponseDTO> getAllOfferTypes() {
        return repository.findAll().stream().map(mapper::toResponseDTO).toList();
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
