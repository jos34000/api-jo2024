package dev.jos.back.service;

import dev.jos.back.dto.offertype.CreateOfferTypeDTO;
import dev.jos.back.dto.offertype.OfferTypeResponseDTO;
import dev.jos.back.mapper.OfferTypeMapper;
import dev.jos.back.model.OfferType;
import dev.jos.back.repository.OfferTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OfferTypeService {
    private final OfferTypeRepository repository;
    private final OfferTypeMapper mapper;

    public List<OfferTypeResponseDTO> getAllOfferTypes() {
        return repository.findAll().stream().map(mapper::toResponseDTO).toList();
    }

    public OfferTypeResponseDTO createOfferType(CreateOfferTypeDTO dtoRequest) {
        OfferType offerType = mapper.toEntity(dtoRequest);
        OfferType savedOfferType = repository.save(offerType);
        return mapper.toResponseDTO(savedOfferType);
    }
}
