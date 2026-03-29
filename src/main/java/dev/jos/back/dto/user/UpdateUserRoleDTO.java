package dev.jos.back.dto.user;

import dev.jos.back.util.enums.Role;
import jakarta.validation.constraints.NotNull;

public record UpdateUserRoleDTO(
        @NotNull(message = "Le rôle est obligatoire")
        Role role
) {
}
