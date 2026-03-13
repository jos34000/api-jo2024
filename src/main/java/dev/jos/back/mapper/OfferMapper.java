package dev.jos.back.mapper;

import dev.jos.back.dto.offer.CreateOfferDTO;
import dev.jos.back.dto.offer.OfferResponseDTO;
import dev.jos.back.entities.Offer;
import org.springframework.stereotype.Component;

@Component
public class OfferMapper {
    public Offer toEntity(CreateOfferDTO dto) {
        return Offer.builder()
                .name(dto.name())
                .description(dto.description())
                .price(dto.price())
                .numberOfTickets(dto.numberOfTickets())
                .isActive(dto.isActive())
                .displayOrder(dto.displayOrder())
                .features(dto.features())
                .build();
    }

    public OfferResponseDTO toResponseDTO(Offer entity) {
        return OfferResponseDTO.builder()
                .name(entity.getName())
                .description(entity.getDescription())
                .price(entity.getPrice())
                .numberOfTickets(entity.getNumberOfTickets())
                .isActive(entity.getIsActive())
                .displayOrder(entity.getDisplayOrder())
                .features(entity.getFeatures())
                .build();
    }
}
