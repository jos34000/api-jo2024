package dev.jos.back.controller;

import dev.jos.back.dto.twofactor.ToggleTwoFactorRequestDTO;
import dev.jos.back.dto.twofactor.VerifyCodeRequestDTO;
import dev.jos.back.exceptions.twofactor.TwoFactorCodeNotFoundException;
import dev.jos.back.exceptions.twofactor.TwoFactorMaxAttemptsException;
import dev.jos.back.exceptions.user.UserNotFoundException;
import dev.jos.back.service.TwoFactorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/2fa")
@RequiredArgsConstructor
public class TwoFactorController {

    private final TwoFactorService twoFactorService;

    /**
     * Envoie un code OTP à l'adresse email de l'utilisateur authentifié.
     *
     * @param auth l'objet d'authentification Spring Security de l'utilisateur courant
     * @return 204 No Content si l'email a bien été envoyé
     * @throws UserNotFoundException si l'utilisateur n'existe pas en base
     */
    @PostMapping("/send")
    public ResponseEntity<Void> sendCode(Authentication auth) {
        twoFactorService.sendCode(auth.getName());
        return ResponseEntity.noContent().build();
    }

    /**
     * Vérifie le code OTP soumis par l'utilisateur.
     *
     * @param auth    l'objet d'authentification Spring Security de l'utilisateur courant
     * @param request le corps de la requête contenant le code OTP à vérifier
     * @return 204 No Content si le code est valide, 401 Unauthorized sinon
     * @throws TwoFactorCodeNotFoundException si aucun code valide n'existe pour cet utilisateur
     * @throws TwoFactorMaxAttemptsException  si le nombre maximum de tentatives est atteint
     */
    @PostMapping("/verify")
    public ResponseEntity<Void> verifyCode(Authentication auth,
                                           @RequestBody @Valid VerifyCodeRequestDTO request) {
        boolean valid = twoFactorService.verifyCode(auth.getName(), request.code());
        return valid
                ? ResponseEntity.noContent().build()
                : ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    /**
     * Active ou désactive le 2FA pour l'utilisateur authentifié.
     *
     * @param auth    l'objet d'authentification Spring Security de l'utilisateur courant
     * @param request le corps de la requête indiquant l'état souhaité du 2FA
     * @return 204 No Content si la mise à jour a bien été effectuée
     * @throws UserNotFoundException si l'utilisateur n'existe pas en base
     */
    @PatchMapping("/toggle")
    public ResponseEntity<Void> toggle(Authentication auth,
                                       @RequestBody @Valid ToggleTwoFactorRequestDTO request) {
        twoFactorService.toggle(auth.getName(), request.enabled());
        return ResponseEntity.noContent().build();
    }
}