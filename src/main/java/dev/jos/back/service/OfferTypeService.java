package dev.jos.back.service;

import dev.jos.back.dto.offertype.CreateOfferTypeDTO;
import dev.jos.back.dto.offertype.OfferTypeResponseDTO;
import dev.jos.back.exceptions.offertype.OfferTypeAlreadyExistsException;
import dev.jos.back.mapper.OfferTypeMapper;
import dev.jos.back.model.OfferType;
import dev.jos.back.repository.OfferTypeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OfferTypeService {
    private final OfferTypeRepository repository;
    private final OfferTypeMapper mapper;

    public List<OfferTypeResponseDTO> getAllOfferTypes() {
        return repository.findAll().stream().map(mapper::toResponseDTO).toList();
    }

    public OfferTypeResponseDTO createOfferType(CreateOfferTypeDTO dto) {
        if (repository.findByName(dto.name()).isPresent()) {
            log.warn("Offre ignorée - déjà existante : {}", dto.name());
            throw new OfferTypeAlreadyExistsException("Cette offre existe déjà.");
        }
        OfferType offerType = mapper.toEntity(dto);
        OfferType savedOfferType = repository.save(offerType);
        return mapper.toResponseDTO(savedOfferType);
    }

    public List<OfferTypeResponseDTO> createOfferTypeBulk(List<CreateOfferTypeDTO> dtoRequest) {
        List<OfferType> offersToSave = new ArrayList<>();
        dtoRequest.forEach(dto -> offersToSave.add(mapper.toEntity(dto)));
        List<OfferType> savedOfferTypes = repository.saveAll(offersToSave);
        return savedOfferTypes.stream().map(mapper::toResponseDTO).toList();
    }
}
