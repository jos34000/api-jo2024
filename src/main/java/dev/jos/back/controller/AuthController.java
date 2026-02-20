package dev.jos.back.controller;

import dev.jos.back.dto.user.CreateUserDTO;
import dev.jos.back.dto.user.LoginRequestDTO;
import dev.jos.back.dto.user.LoginResponseDTO;
import dev.jos.back.dto.user.UserResponseDTO;
import dev.jos.back.properties.JwtProperties;
import dev.jos.back.service.CookieService;
import dev.jos.back.service.CustomUserDetailsService;
import dev.jos.back.service.JwtService;
import dev.jos.back.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final CookieService cookieService;
    private final UserService userService;
    private final CustomUserDetailsService userDetailsService;
    private final JwtProperties jwtProperties;

    @PostMapping("/register")
    public ResponseEntity<LoginResponseDTO> register(@Valid @RequestBody CreateUserDTO request) {
        UserResponseDTO userResponse = userService.createUser(request);

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.email());
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        String accessToken = jwtService.generateAccessToken(request.email(), roles);
        String refreshToken = jwtService.generateRefreshToken(request.email());

        LoginResponseDTO response = new LoginResponseDTO(userResponse);

        return ResponseEntity.status(HttpStatus.CREATED)
                .header(HttpHeaders.SET_COOKIE,
                        cookieService.createAccessTokenCookie(accessToken).toString())
                .header(HttpHeaders.SET_COOKIE,
                        cookieService.createRefreshTokenCookie(refreshToken).toString())
                .body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginRequestDTO request) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()
                )
        );

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String email = userDetails != null ? userDetails.getUsername() : null;

        List<String> roles = null;
        if (userDetails != null) {
            roles = userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .toList();
        }

        String accessToken = jwtService.generateAccessToken(email, roles);
        String refreshToken = jwtService.generateRefreshToken(email);

        UserResponseDTO userResponse = userService.getUserResponseDto(email);

        LoginResponseDTO response = new LoginResponseDTO(userResponse);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE,
                        cookieService.createAccessTokenCookie(accessToken).toString())
                .header(HttpHeaders.SET_COOKIE,
                        cookieService.createRefreshTokenCookie(refreshToken).toString())
                .body(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<Void> refresh(
            @CookieValue(name = "${jwt.refresh-token.cookie-name}") String refreshToken) {

        if (!jwtService.validateToken(refreshToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String email = jwtService.getUsernameFromToken(refreshToken);

        UserDetails userDetails = userDetailsService.loadUserByUsername(email);

        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        String newAccessToken = jwtService.generateAccessToken(email, roles);
        String newRefreshToken = jwtService.generateRefreshToken(email);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE,
                        cookieService.createAccessTokenCookie(newAccessToken).toString())
                .header(HttpHeaders.SET_COOKIE,
                        cookieService.createRefreshTokenCookie(newRefreshToken).toString())
                .build();
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE,
                        cookieService.createDeleteCookie(
                                jwtProperties.getAccessToken().getCookieName()).toString())
                .header(HttpHeaders.SET_COOKIE,
                        cookieService.createDeleteCookie(
                                jwtProperties.getRefreshToken().getCookieName()).toString())
                .build();
    }
}