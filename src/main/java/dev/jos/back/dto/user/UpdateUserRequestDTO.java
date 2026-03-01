package dev.jos.back.dto.user;

import lombok.Builder;

@Builder
public record UpdateUserRequestDTO(String firstName, String lastName, String email) {
}
