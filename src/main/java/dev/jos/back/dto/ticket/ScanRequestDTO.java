package dev.jos.back.dto.ticket;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ScanRequestDTO(
        @NotBlank(message = "La clé combinée est obligatoire")
        @Pattern(regexp = "[0-9a-f]{64}", message = "Format de clé invalide")
        String combinedKey
) {}
