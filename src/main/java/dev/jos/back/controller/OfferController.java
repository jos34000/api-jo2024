package dev.jos.back.controller;

import dev.jos.back.dto.offer.BulkOfferResponseDTO;
import dev.jos.back.dto.offer.CreateOfferDTO;
import dev.jos.back.dto.offer.OfferResponseDTO;
import dev.jos.back.service.OfferService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Contrôleur REST pour la gestion des offres.
 * Permet de créer et consulter les différentes offres disponibles.
 *
 * @see OfferService
 * @see OfferResponseDTO
 */
@RestController
@RequestMapping("/api/offer")
@RequiredArgsConstructor
public class OfferController {
    private final OfferService offerService;

    /**
     * Créer en masse les offres.
     * Réservé aux administrateurs.
     *
     * @param dto une list des informations de l'offre à créer (nom, description, caractéristiques)
     * @return {@code ResponseEntity<BulkOfferResponseDTO>} contenant les offres créees et le nombre d'offres
     * @throws jakarta.validation.ConstraintViolationException                   si les données du type d'offre sont invalides
     * @throws dev.jos.back.exceptions.offertype.OfferTypeAlreadyExistsException si un type d'offre
     *                                                                           avec le même nom existe déjà
     * @throws org.springframework.security.access.AccessDeniedException         si l'utilisateur
     *                                                                           n'a pas le rôle ADMIN
     */
    @PostMapping("/bulk")
    public ResponseEntity<BulkOfferResponseDTO> createOfferType(
            @Valid @RequestBody List<CreateOfferDTO> dto) {

        List<OfferResponseDTO> created = offerService.createOfferTypeBulk(dto);

        BulkOfferResponseDTO response = new BulkOfferResponseDTO(
                created.size(),
                created
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Crée une offre unique.
     * Réservé aux administrateurs.
     *
     * @param dtoRequest les informations de l'offre à créer (nom, description, nombre de billets, prix)
     * @return {@code ResponseEntity<OfferResponseDTO>} contenant l'offre créée avec son identifiant généré
     * @throws jakarta.validation.ConstraintViolationException           si les données de l'offre sont invalides
     * @throws org.springframework.security.access.AccessDeniedException si l'utilisateur n'a pas le rôle ADMIN
     */
    @PostMapping
    public ResponseEntity<OfferResponseDTO> createOfferType(
            @Valid @RequestBody CreateOfferDTO dtoRequest) {

        OfferResponseDTO created = offerService.createOfferType(dtoRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Récupère tous les types d'offres.
     *
     * @return {@code ResponseEntity<List<OfferResponseDTO>>} contenant la liste complète
     * de toutes les offres
     */
    @GetMapping("/all")
    public ResponseEntity<List<OfferResponseDTO>> getAllOfferTypes(
            @RequestHeader(value = "Accept-Language", defaultValue = "fr") String locale) {
        return ResponseEntity.ok(offerService.getAllOfferTypes(locale));
    }
}
