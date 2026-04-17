package dev.jos.back.dto.ticket;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ScanRequestDTO(
        @NotBlank(message = "Le code-barre est obligatoire")
        @Pattern(regexp = "JO2024-[A-Z0-9]{8}", message = "Format de code-barre invalide")
        String barcode
) {}
