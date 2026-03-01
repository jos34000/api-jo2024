package dev.jos.back.dto.twofactor;

import jakarta.validation.constraints.NotBlank;

public record VerifyCodeRequestDTO(@NotBlank String code) {
}
