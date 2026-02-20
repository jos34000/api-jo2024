package dev.jos.back.dto.event;

import jakarta.validation.constraints.*;

import java.time.LocalDateTime;

public record CreateEventDTO(
        @NotBlank(message = "Le nom est obligatoire")
        @Size(min = 3, max = 200, message = "Le nom doit contenir entre 3 et 200 caractères")
        String name,

        @NotBlank(message = "La description est obligatoire")
        @Size(min = 10, max = 2000, message = "La description doit contenir entre 10 et 2000 caractères")
        String description,

        @NotBlank(message = "Le lieu est obligatoire")
        @Size(min = 3, max = 200, message = "Le lieu doit contenir entre 3 et 200 caractères")
        String location,

        @NotNull(message = "La date de l'événement est obligatoire")
        LocalDateTime eventDate,

        @NotNull(message = "La capacité est obligatoire")
        @Min(value = 1, message = "La capacité doit être d'au moins 1")
        @Max(value = 500000, message = "La capacité ne peut pas dépasser 500 000")
        Integer capacity,

        @Min(value = 0, message = "Les places disponibles ne peuvent pas être négatives")
        Integer availableSlots,

        Boolean isActive
) {
}