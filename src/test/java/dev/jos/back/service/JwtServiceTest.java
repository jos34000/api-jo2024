package dev.jos.back.service;

import dev.jos.back.properties.JwtProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    // 32 chars ASCII = 256 bits, taille minimale pour HMAC-SHA256
    private static final String SECRET  = "12345678901234567890123456789012";
    private static final long   ACCESS_EXP  = 900_000L;       // 15 min
    private static final long   REFRESH_EXP = 604_800_000L;   // 7 jours

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        // JwtProperties est un POJO @Data — on l'instancie directement sans Mockito
        JwtProperties props = new JwtProperties();
        props.setSecret(SECRET);

        JwtProperties.AccessToken accessToken = new JwtProperties.AccessToken();
        accessToken.setExpiration(ACCESS_EXP);
        accessToken.setCookieName("access_token");
        props.setAccessToken(accessToken);

        JwtProperties.RefreshToken refreshToken = new JwtProperties.RefreshToken();
        refreshToken.setExpiration(REFRESH_EXP);
        refreshToken.setCookieName("refresh_token");
        props.setRefreshToken(refreshToken);

        jwtService = new JwtService(props);
    }

    // ── generateAccessToken ───────────────────────────────────────────────────

    @Test
    void generateAccessToken_returnsValidToken_withCorrectSubject() {
        String token = jwtService.generateAccessToken("alice@example.com", List.of("ROLE_USER"));

        assertThat(token).isNotBlank();
        assertThat(jwtService.validateToken(token)).isTrue();
        assertThat(jwtService.getUsernameFromToken(token)).isEqualTo("alice@example.com");
    }

    @Test
    void generateAccessToken_withMultipleRoles_parsesWithoutException() {
        String token = jwtService.generateAccessToken("admin@example.com", List.of("ROLE_ADMIN", "ROLE_USER"));

        assertThat(jwtService.validateToken(token)).isTrue();
        assertThat(jwtService.getUsernameFromToken(token)).isEqualTo("admin@example.com");
    }

    // ── generateRefreshToken ──────────────────────────────────────────────────

    @Test
    void generateRefreshToken_returnsValidToken_withCorrectSubject() {
        String token = jwtService.generateRefreshToken("bob@example.com");

        assertThat(token).isNotBlank();
        assertThat(jwtService.validateToken(token)).isTrue();
        assertThat(jwtService.getUsernameFromToken(token)).isEqualTo("bob@example.com");
    }

    // ── validateToken ─────────────────────────────────────────────────────────

    @Test
    void validateToken_returnsFalse_forMalformedToken() {
        assertThat(jwtService.validateToken("not.a.valid.jwt")).isFalse();
    }

    @Test
    void validateToken_returnsFalse_forBlankToken() {
        assertThat(jwtService.validateToken("")).isFalse();
    }

    @Test
    void validateToken_returnsFalse_forTokenSignedWithDifferentSecret() {
        // Générer un token avec un service configuré sur une autre clé
        JwtProperties otherProps = new JwtProperties();
        otherProps.setSecret("differentkeydifferentkeydifferenx");
        JwtProperties.AccessToken at = new JwtProperties.AccessToken();
        at.setExpiration(ACCESS_EXP);
        otherProps.setAccessToken(at);
        String foreignToken = new JwtService(otherProps).generateAccessToken("eve@example.com", List.of());

        // Valider avec le service d'origine — doit échouer car la clé est différente
        assertThat(jwtService.validateToken(foreignToken)).isFalse();
    }
}
