package dev.jos.back.controller;

import dev.jos.back.dto.sport.SportResponseDTO;
import dev.jos.back.service.SportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
     * Récupère le sport selon le nom.
     *
     * @param name le sport recherché
     * @return {@code ResponseEntity<SportResponseDTO>} contenant les détails complets du sport
     */
    @GetMapping("/{name}")
    public ResponseEntity<SportResponseDTO> getSport(@PathVariable String name) {
        SportResponseDTO sport = sportService.getSportByName(name);
        return ResponseEntity.ok(sport);
    }
}
