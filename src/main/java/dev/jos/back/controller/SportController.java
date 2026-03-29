package dev.jos.back.controller;

import dev.jos.back.dto.sport.BulkSportResponseDTO;
import dev.jos.back.dto.sport.CreateSportDTO;
import dev.jos.back.dto.sport.SportResponseDTO;
import dev.jos.back.dto.sport.UpdateSportDTO;
import dev.jos.back.service.SportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Contrôleur REST pour la gestion des sports.
 * Fournit des endpoints pour créer, consulter et filtrer les sports.
 *
 * @see SportService
 * @see SportResponseDTO
 */
@RestController
@RequestMapping("/api/sport")
@RequiredArgsConstructor
public class SportController {

    private final SportService sportService;

    /**
     * Récupère la liste de tous les sports disponibles.
     *
     * @return {@code ResponseEntity<List<SportResponseDTO>>} contenant la liste complète des sports
     */
    @GetMapping
    public ResponseEntity<List<SportResponseDTO>> getAllSports(
            @RequestHeader(value = "Accept-Language", defaultValue = "fr") String locale) {
        return ResponseEntity.ok(sportService.getAllSports(locale));
    }

    /**
     * Récupère le sport selon son id.
     *
     * @param id le sport recherché
     * @return {@code ResponseEntity<SportResponseDTO>} contenant les détails complets du sport
     */
    @GetMapping("/{id}")
    public ResponseEntity<SportResponseDTO> getSport(
            @PathVariable Long id,
            @RequestHeader(value = "Accept-Language", defaultValue = "fr") String locale) {
        SportResponseDTO sport = sportService.getSportById(id, locale);
        return ResponseEntity.ok(sport);
    }

    /**
     * Crée plusieurs sports en une seule opération.
     * Réservé aux administrateurs.
     *
     * @param sports liste des sports à créer
     * @return {@code ResponseEntity<BulkSportResponseDTO>} contenant le résumé de la création
     * (nombre total de sports créés et liste détaillée des sports)
     * @throws jakarta.validation.ConstraintViolationException           si un ou plusieurs sports
     *                                                                   contiennent des données invalides
     * @throws org.springframework.security.access.AccessDeniedException si l'utilisateur
     *                                                                   n'a pas le rôle ADMIN
     */
    @PostMapping("/bulk")
    public ResponseEntity<BulkSportResponseDTO> createSportsBulk(
            @Valid @RequestBody List<CreateSportDTO> sports) {

        List<SportResponseDTO> created = sportService.createSportsBulk(sports);

        BulkSportResponseDTO response = new BulkSportResponseDTO(
                created,
                created.size()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Met à jour un sport existant.
     * Réservé aux administrateurs.
     */
    @PutMapping("/{id}")
    public ResponseEntity<SportResponseDTO> updateSport(
            @PathVariable Long id,
            @Valid @RequestBody UpdateSportDTO dto) {
        SportResponseDTO updated = sportService.updateSport(id, dto);
        return ResponseEntity.ok(updated);
    }

    /**
     * Supprime un sport par son identifiant.
     * Réservé aux administrateurs.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSport(@PathVariable Long id) {
        sportService.deleteSport(id);
        return ResponseEntity.noContent().build();
    }
}
