package dev.jos.back.mapper;

import dev.jos.back.dto.offertype.CreateOfferTypeDTO;
import dev.jos.back.dto.offertype.OfferTypeResponseDTO;
import dev.jos.back.model.OfferType;
import org.springframework.stereotype.Component;

@Component
public class OfferTypeMapper {
    public OfferType toEntity(CreateOfferTypeDTO dto) {
        return OfferType.builder()
                .name(dto.name())
                .description(dto.description())
                .price(dto.price())
                .numberOfTickets(dto.numberOfTickets())
                .isActive(dto.isActive())
                .displayOrder(dto.displayOrder())
                .build();
    }

    public OfferTypeResponseDTO toResponseDTO(OfferType entity) {
        return OfferTypeResponseDTO.builder()
                .name(entity.getName())
                .description(entity.getDescription())
                .price(entity.getPrice())
                .numberOfTickets(entity.getNumberOfTickets())
                .isActive(entity.getIsActive())
                .build();
    }
}
