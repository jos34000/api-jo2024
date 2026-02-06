package dev.jos.back.service;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private final String TEST_ACCESS_SECRET = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
    private final String TEST_REFRESH_SECRET = "7538782F413F4428472B4B6250645367566B5970404E635266556A586E327235";
    private final long TEST_ACCESS_EXPIRATION = 900000;
    private final long TEST_REFRESH_EXPIRATION = 2592000000L;

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "accessTokenSecret", TEST_ACCESS_SECRET);
        ReflectionTestUtils.setField(jwtService, "accessTokenExpiration", TEST_ACCESS_EXPIRATION);
        ReflectionTestUtils.setField(jwtService, "refreshTokenSecret", TEST_REFRESH_SECRET);
        ReflectionTestUtils.setField(jwtService, "refreshTokenExpiration", TEST_REFRESH_EXPIRATION);
    }

    @Test
    void shouldGenerateValidAccessToken() {
        String email = "jos@test.com";
        String token = jwtService.generateAccessToken(email);

        assertThat(token).isNotNull().isNotEmpty();
        System.out.println("✅ Access token généré : " + token.substring(0, 20) + "...");
    }

    @Test
    void shouldGenerateValidRefreshToken() {
        String email = "jos@test.com";
        String token = jwtService.generateRefreshToken(email);

        assertThat(token).isNotNull().isNotEmpty();
        System.out.println("✅ Refresh token généré : " + token.substring(0, 20) + "...");
    }

    @Test
    void shouldExtractEmailFromAccessToken() {
        String email = "jos@test.com";
        String token = jwtService.generateAccessToken(email);
        String extractedEmail = jwtService.extractUsername(token, false);

        assertThat(extractedEmail).isEqualTo(email);
        System.out.println("✅ Email extrait du access token : " + extractedEmail);
    }

    @Test
    void shouldExtractEmailFromRefreshToken() {
        String email = "jos@test.com";
        String token = jwtService.generateRefreshToken(email);
        String extractedEmail = jwtService.extractUsername(token, true);

        assertThat(extractedEmail).isEqualTo(email);
        System.out.println("✅ Email extrait du refresh token : " + extractedEmail);
    }

    @Test
    void shouldRejectAccessTokenWithRefreshSecret() {
        String email = "jos@test.com";
        String accessToken = jwtService.generateAccessToken(email);

        assertThatThrownBy(() -> jwtService.extractUsername(accessToken, true)).isInstanceOf(SignatureException.class);

        System.out.println("✅ Access token rejeté avec mauvais secret");
    }

    @Test
    void shouldRejectRefreshTokenWithAccessSecret() {
        String email = "jos@test.com";
        String refreshToken = jwtService.generateRefreshToken(email);

        assertThatThrownBy(() -> jwtService.extractUsername(refreshToken, false)).isInstanceOf(SignatureException.class);

        System.out.println("✅ Refresh token rejeté avec mauvais secret");
    }

    @Test
    void shouldRejectExpiredToken() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "accessTokenSecret", TEST_ACCESS_SECRET);
        ReflectionTestUtils.setField(jwtService, "accessTokenExpiration", -1000L); // Expiré

        String email = "jos@test.com";
        String expiredToken = jwtService.generateAccessToken(email);

        assertThatThrownBy(() -> jwtService.extractUsername(expiredToken, false)).isInstanceOf(ExpiredJwtException.class);

        System.out.println("✅ Token expiré correctement rejeté");
    }
}