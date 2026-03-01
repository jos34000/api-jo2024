package dev.jos.back.controller;

import dev.jos.back.dto.user.ChangePasswordRequestDTO;
import dev.jos.back.dto.user.ForgetPasswordRequestDTO;
import dev.jos.back.dto.user.UpdateUserRequestDTO;
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
 *
 * @see UserService
 * @see Authentication
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * Récupère les informations de l'utilisateur actuellement authentifié.
     *
     * @param authentication l'objet d'authentification Spring Security injecté automatiquement,
     *                       contenant l'email de l'utilisateur extrait du JWT
     * @return {@code ResponseEntity<UserResponseDTO>} contenant les informations du profil utilisateur
     * @throws UserNotFoundException si l'utilisateur correspondant à l'email du JWT n'existe plus
     *                               dans la base de données (cas rare, peut survenir si l'utilisateur a été supprimé
     *                               pendant que son token était encore valide)
     */
    @GetMapping("/me")
    public ResponseEntity<UserResponseDTO> getCurrentUser(Authentication authentication) {
        String email = authentication.getName();
        UserResponseDTO userDto = userService.getUserResponseDto(email);
        return ResponseEntity.ok(userDto);
    }

    /**
     * Cette opération permet à l'utilisateur de modifier son mot de passe en fournissant
     * son mot de passe actuel (pour vérification) et le nouveau mot de passe souhaité.
     * Le nouveau mot de passe doit respecter les critères de validation définis dans
     * {@link ChangePasswordRequestDTO}.
     *
     * @param auth    l'objet d'authentification Spring Security contenant l'email de l'utilisateur
     * @param request l'objet contenant l'ancien mot de passe et le nouveau mot de passe
     * @return {@code ResponseEntity<Void>} vide (204 No Content) en cas de succès
     * @throws InvalidPasswordException                        si le mot de passe actuel fourni est incorrect
     * @throws UserNotFoundException                           si l'utilisateur n'existe pas ou a été supprimé
     * @throws jakarta.validation.ConstraintViolationException si le nouveau mot de passe
     *                                                         ne respecte pas les critères de validation (longueur minimale, complexité, etc.)
     *
     */
    @PutMapping("/password")
    public ResponseEntity<Void> updatePassword(Authentication auth, @Valid @RequestBody ChangePasswordRequestDTO request) {
        String email = auth.getName();
        String oldPassword = request.oldPassword();
        String newPassword = request.newPassword();

        userService.updatePassword(email, oldPassword, newPassword);
        return ResponseEntity.noContent().build();
    }


    /**
     * Cette opération génère un token de réinitialisation unique et temporaire,
     * puis envoie un email à l'adresse fournie contenant un lien permettant
     * de créer un nouveau mot de passe. Le token a une durée de validité limitée
     * (généralement 15-30 minutes).
     *
     * @param request l'objet contenant l'adresse email pour laquelle réinitialiser le mot de passe
     * @return {@code ResponseEntity<?>} vide (200 OK) systématiquement, que l'email existe ou non
     * @throws jakarta.validation.ConstraintViolationException si l'email fourni a un format invalide
     *
     */
    @PostMapping("/forget-password")
    public ResponseEntity<Void> forgotPassword(@RequestBody ForgetPasswordRequestDTO request) {
        userService.resetPassword(request.email());
        return ResponseEntity.ok().build();
    }


    /**
     * Cette opération vérifie la validité d'un token de réinitialisation reçu par email.
     * Elle est typiquement appelée lorsque l'utilisateur clique sur le lien de réinitialisation
     * ou accède à la page de création d'un nouveau mot de passe, afin de s'assurer que
     * le token est encore valide avant d'afficher le formulaire.
     *
     * @param token le token de réinitialisation à valider (reçu via l'URL du lien email)
     * @return {@code ResponseEntity<?>} avec un statut HTTP indiquant le résultat de la validation
     *
     */
    @GetMapping("/validate-reset-token")
    public ResponseEntity<Void> validateResetToken(@RequestParam String token) {
        TokenValidationResult result = userService.validateResetToken(token);
        return switch (result) {
            case VALID -> ResponseEntity.ok().build();
            case EXPIRED -> ResponseEntity.status(HttpStatus.GONE).build();
            case NOT_FOUND -> ResponseEntity.badRequest().build();
        };
    }


    /**
     * Cette opération permet à l'utilisateur de modifier ses données de profil,
     * notamment son adresse email, son prénom et son nom de famille. Toutes les
     * modifications sont validées selon les règles définies dans {@link UpdateUserRequestDTO}.
     *
     * @param auth    l'objet d'authentification Spring Security contenant l'email actuel de l'utilisateur
     * @param request l'objet contenant les nouvelles informations à mettre à jour (email, prénom, nom)
     * @return {@code ResponseEntity<UserResponseDTO>} contenant les informations utilisateur
     * mises à jour avec les nouvelles valeurs
     * @throws UserNotFoundException                                   si l'utilisateur n'existe pas ou a été supprimé
     * @throws dev.jos.back.exceptions.user.UserAlreadyExistsException si le nouvel email
     *                                                                 est déjà utilisé par un autre utilisateur
     * @throws jakarta.validation.ConstraintViolationException         si les données fournies
     *                                                                 ne respectent pas les critères de validation (format email, longueur des champs, etc.)
     * @see UpdateUserRequestDTO pour les règles de validation des champs
     * @see UserResponseDTO pour la structure des données retournées
     */
    @PutMapping
    public ResponseEntity<UserResponseDTO> updateUser(Authentication auth, @Valid @RequestBody UpdateUserRequestDTO request) {
        String email = auth.getName();
        String newEmail = request.email();
        String newFirstName = request.firstName();
        String newLastName = request.lastName();
        UserResponseDTO response = userService.updateUser(email, newEmail, newFirstName, newLastName);
        return ResponseEntity.ok(response);
    }
}
