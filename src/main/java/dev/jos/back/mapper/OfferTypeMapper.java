package dev.jos.back.mapper;

import dev.jos.back.dto.offertype.CreateOfferDTO;
import dev.jos.back.dto.offertype.OfferResponseDTO;
import dev.jos.back.entities.Offer;
import org.springframework.stereotype.Component;

@Component
public class OfferTypeMapper {
    public Offer toEntity(CreateOfferDTO dto) {
        return Offer.builder()
                .name(dto.name())
                .description(dto.description())
                .price(dto.price())
                .numberOfTickets(dto.numberOfTickets())
                .isActive(dto.isActive())
                .displayOrder(dto.displayOrder())
                .build();
    }

    public OfferResponseDTO toResponseDTO(Offer entity) {
        return OfferResponseDTO.builder()
                .name(entity.getName())
                .description(entity.getDescription())
                .price(entity.getPrice())
                .numberOfTickets(entity.getNumberOfTickets())
                .isActive(entity.getIsActive())
                .build();
    }
}
