package dev.jos.back.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UpdateLocaleRequestDTO(
        @NotBlank
        @Pattern(regexp = "^(fr|en|de|es)$", message = "Locale non supportée")
        String locale
) {}
