package dev.jos.back.controller;

import dev.jos.back.dto.offertype.CreateOfferTypeDTO;
import dev.jos.back.dto.offertype.OfferTypeResponseDTO;
import dev.jos.back.service.OfferTypeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/offer-types")
@RequiredArgsConstructor
public class OfferTypeController {
    private final OfferTypeService offerTypeService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OfferTypeResponseDTO> createOfferType(
            @Valid @RequestBody CreateOfferTypeDTO dtoRequest) {

        OfferTypeResponseDTO created = offerTypeService.createOfferType(dtoRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/all")
    public ResponseEntity<List<OfferTypeResponseDTO>> getAllOfferTypes() {
        return ResponseEntity.ok(offerTypeService.getAllOfferTypes());
    }
}
