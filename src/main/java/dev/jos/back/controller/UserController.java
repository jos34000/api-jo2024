package dev.jos.back.controller;

import dev.jos.back.dto.user.UserResponseDTO;
import dev.jos.back.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Contrôleur REST pour la gestion des utilisateurs.
 * Fournit des endpoints pour accéder aux informations de l'utilisateur authentifié.
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * Récupère les informations de l'utilisateur actuellement authentifié.
     *
     * @param authentication l'objet d'authentification Spring Security contenant les informations de l'utilisateur
     * @return ResponseEntity contenant les informations de l'utilisateur (200 OK)
     */
    @GetMapping("/me")
    public ResponseEntity<UserResponseDTO> getCurrentUser(Authentication authentication) {
        String email = authentication.getName();
        UserResponseDTO userDto = userService.getUserResponseDto(email);
        return ResponseEntity.ok(userDto);
    }
}
