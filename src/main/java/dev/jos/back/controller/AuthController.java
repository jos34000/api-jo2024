package dev.jos.back.controller;

import dev.jos.back.dto.user.*;
import dev.jos.back.service.JwtService;
import dev.jos.back.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @PostMapping("/register")
    public ResponseEntity<UserResponseDTO> registerUser(@Valid @RequestBody CreateUserDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(dto));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginRequestDTO request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        String accessToken = jwtService.generateAccessToken(request.getEmail());
        String refreshToken = jwtService.generateRefreshToken(request.getEmail());

        return ResponseEntity.ok(new LoginResponseDTO(accessToken, refreshToken));
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenRefreshResponseDTO> refreshToken(@RequestHeader("Authorization") String refreshToken) {
        String username = jwtService.extractUsername(refreshToken, true);
        String newAccessToken = jwtService.generateAccessToken(username);

        return ResponseEntity.ok(new TokenRefreshResponseDTO(newAccessToken));
    }
}