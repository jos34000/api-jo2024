package dev.jos.back.dto.user;

import lombok.Builder;

@Builder
public record ChangePasswordRequestDTO(String oldPassword, String newPassword) {
}
