package dev.jos.back.service;

import dev.jos.back.properties.JwtProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseCookie;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class CookieServiceTest {

    private CookieService cookieService;

    @BeforeEach
    void setUp() {
        // JwtProperties est un POJO @Data — instanciation directe sans Mockito
        JwtProperties props = new JwtProperties();
        props.setSecret("any-secret");

        JwtProperties.AccessToken accessToken = new JwtProperties.AccessToken();
        accessToken.setCookieName("access_token");
        accessToken.setExpiration(900_000L);
        props.setAccessToken(accessToken);

        JwtProperties.RefreshToken refreshToken = new JwtProperties.RefreshToken();
        refreshToken.setCookieName("refresh_token");
        refreshToken.setExpiration(604_800_000L);
        props.setRefreshToken(refreshToken);

        cookieService = new CookieService(props);

        // Les champs @Value ne sont pas injectés hors contexte Spring
        ReflectionTestUtils.setField(cookieService, "domain", "");
        ReflectionTestUtils.setField(cookieService, "secure", false);
        ReflectionTestUtils.setField(cookieService, "sameSite", "Lax");
    }

    // ── createAccessTokenCookie ───────────────────────────────────────────────

    @Test
    void createAccessTokenCookie_hasCorrectNameValueAndAttributes() {
        ResponseCookie cookie = cookieService.createAccessTokenCookie("my-access-token");

        assertThat(cookie.getName()).isEqualTo("access_token");
        assertThat(cookie.getValue()).isEqualTo("my-access-token");
        assertThat(cookie.isHttpOnly()).isTrue();
        assertThat(cookie.getPath()).isEqualTo("/");
        assertThat(cookie.getMaxAge()).isEqualTo(Duration.ofMillis(900_000L));
        assertThat(cookie.getSameSite()).isEqualTo("Lax");
    }

    @Test
    void createAccessTokenCookie_setsDomain_whenDomainNotEmpty() {
        ReflectionTestUtils.setField(cookieService, "domain", "example.com");

        ResponseCookie cookie = cookieService.createAccessTokenCookie("token");

        assertThat(cookie.getDomain()).isEqualTo("example.com");
    }

    @Test
    void createAccessTokenCookie_doesNotSetDomain_whenDomainIsEmpty() {
        ResponseCookie cookie = cookieService.createAccessTokenCookie("token");

        assertThat(cookie.getDomain()).isNull();
    }

    @Test
    void createAccessTokenCookie_isSecure_whenConfigured() {
        ReflectionTestUtils.setField(cookieService, "secure", true);

        ResponseCookie cookie = cookieService.createAccessTokenCookie("token");

        assertThat(cookie.isSecure()).isTrue();
    }

    // ── createRefreshTokenCookie ──────────────────────────────────────────────

    @Test
    void createRefreshTokenCookie_hasCorrectNameAndExpiry() {
        ResponseCookie cookie = cookieService.createRefreshTokenCookie("my-refresh-token");

        assertThat(cookie.getName()).isEqualTo("refresh_token");
        assertThat(cookie.getValue()).isEqualTo("my-refresh-token");
        assertThat(cookie.getMaxAge()).isEqualTo(Duration.ofMillis(604_800_000L));
        assertThat(cookie.isHttpOnly()).isTrue();
    }

    // ── createDeleteCookie ────────────────────────────────────────────────────

    @Test
    void createDeleteCookie_hasEmptyValueAndZeroMaxAge() {
        ResponseCookie cookie = cookieService.createDeleteCookie("access_token");

        assertThat(cookie.getName()).isEqualTo("access_token");
        assertThat(cookie.getValue()).isEmpty();
        assertThat(cookie.getMaxAge()).isEqualTo(Duration.ZERO);
    }
}
