package dev.jos.back.dto.user;

import dev.jos.back.model.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateUserDTO(
    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "L'email doit être valide")
    String email,

    @NotBlank(message = "Le mot de passe est obligatoire")
    @Size(min = 8, max = 50, message = "Le mot de passe doit contenir entre 8 et 50 caractères")
    String password,

    @Size(min = 2, max = 30, message = "Le prénom doit contenir entre 2 et 30 caractères")
    String firstName,

    @Size(min = 2, max = 30, message = "Le nom doit contenir entre 2 et 30 caractères")
    String lastName
) {
    public User toEntity() {
        return User.builder()
                .email(this.email)
                .firstName(this.firstName)
                .lastName(this.lastName)
                .mfaEnabled(false)
                .build();
    }
}