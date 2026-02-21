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

/**
 * Contrôleur REST pour la gestion des types d'offres.
 * Permet de créer et consulter les différents types d'offres disponibles.
 */
@RestController
@RequestMapping("/api/offer-types")
@RequiredArgsConstructor
public class OfferTypeController {
    private final OfferTypeService offerTypeService;

    /**
     * Crée un nouveau type d'offre.
     * Réservé aux administrateurs.
     *
     * @param dtoRequest les informations du type d'offre à créer
     * @return ResponseEntity contenant le type d'offre créé (201 Created)
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OfferTypeResponseDTO> createOfferType(
            @Valid @RequestBody CreateOfferTypeDTO dtoRequest) {

        OfferTypeResponseDTO created = offerTypeService.createOfferType(dtoRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Récupère tous les types d'offres.
     *
     * @return ResponseEntity contenant la liste de tous les types d'offres (200 OK)
     */
    @GetMapping("/all")
    public ResponseEntity<List<OfferTypeResponseDTO>> getAllOfferTypes() {
        return ResponseEntity.ok(offerTypeService.getAllOfferTypes());
    }
}
