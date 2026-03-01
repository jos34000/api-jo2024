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
 *
 * @see OfferTypeService
 * @see OfferTypeResponseDTO
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
     * @param dtoRequest les informations du type d'offre à créer (nom, description, caractéristiques)
     * @return {@code ResponseEntity<OfferTypeResponseDTO>} contenant le type d'offre créé
     * avec son identifiant généré
     * @throws jakarta.validation.ConstraintViolationException                   si les données du type d'offre sont invalides
     * @throws dev.jos.back.exceptions.offertype.OfferTypeAlreadyExistsException si un type d'offre
     *                                                                           avec le même nom existe déjà
     * @throws org.springframework.security.access.AccessDeniedException         si l'utilisateur
     *                                                                           n'a pas le rôle ADMIN
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
     * @return {@code ResponseEntity<List<OfferTypeResponseDTO>>} contenant la liste complète
     * de tous les types d'offres
     */
    @GetMapping("/all")
    public ResponseEntity<List<OfferTypeResponseDTO>> getAllOfferTypes() {
        return ResponseEntity.ok(offerTypeService.getAllOfferTypes());
    }
}
