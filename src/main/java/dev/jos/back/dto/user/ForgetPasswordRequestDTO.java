package dev.jos.back.dto.user;

import lombok.Builder;

@Builder
public record ForgetPasswordRequestDTO(String email) {
}
