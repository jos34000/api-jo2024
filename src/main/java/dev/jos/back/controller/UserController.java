package dev.jos.back.controller;

import dev.jos.back.dto.user.ChangePasswordRequestDTO;
import dev.jos.back.dto.user.ForgetPasswordRequestDTO;
import dev.jos.back.dto.user.UserResponseDTO;
import dev.jos.back.exceptions.user.InvalidPasswordException;
import dev.jos.back.exceptions.user.UserNotFoundException;
import dev.jos.back.service.UserService;
import dev.jos.back.util.TokenValidationResult;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

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

    /**
     * Met à jour le mot de passe de l'utilisateur authentifié
     *
     * @param auth    l'objet d'authentification Spring Security contenant les informations de l'utilisateur
     * @param request l'objet contenant l'ancien et le nouveau mot de passe
     * @return ResponseEntity vide (204 No Content) en cas de succès
     * @throws InvalidPasswordException si l'ancien mot de passe est incorrect
     * @throws UserNotFoundException    si l'utilisateur n'existe pas
     */
    @PutMapping("/password")
    public ResponseEntity<Void> updatePassword(Authentication auth, @Valid @RequestBody ChangePasswordRequestDTO request) {
        String email = auth.getName();
        String oldPassword = request.oldPassword();
        String newPassword = request.newPassword();

        userService.updatePassword(email, oldPassword, newPassword);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/forget-password")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgetPasswordRequestDTO request) {
        userService.resetPassword(request.email());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/validate-reset-token")
    public ResponseEntity<?> validateResetToken(@RequestParam String token) {
        TokenValidationResult result = userService.validateResetToken(token);
        return switch (result) {
            case VALID -> ResponseEntity.ok().build();
            case EXPIRED -> ResponseEntity.status(HttpStatus.GONE).build();
            case NOT_FOUND -> ResponseEntity.badRequest().build();
        };
    }
}
