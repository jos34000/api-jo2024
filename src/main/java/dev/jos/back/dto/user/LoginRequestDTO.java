package dev.jos.back.dto.user;


public record LoginRequestDTO(String email, String password) {
    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }
}
