package dev.jos.back.service;

import dev.jos.back.properties.JwtProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class CookieService {

    private final JwtProperties jwtProperties;

    @Value("${cookie.domain}")
    private String domain;

    @Value("${cookie.secure}")
    private boolean secure;

    @Value("${cookie.same-site}")
    private String sameSite;

    public ResponseCookie createAccessTokenCookie(String token) {
        return createCookie(
                jwtProperties.getAccessToken().getCookieName(),
                token,
                Duration.ofMillis(jwtProperties.getAccessToken().getExpiration())
        );
    }

    public ResponseCookie createRefreshTokenCookie(String token) {
        return createCookie(
                jwtProperties.getRefreshToken().getCookieName(),
                token,
                Duration.ofMillis(jwtProperties.getRefreshToken().getExpiration())
        );
    }

    public ResponseCookie createDeleteCookie(String cookieName) {
        return createCookie(cookieName, "", Duration.ZERO);
    }

    private ResponseCookie createCookie(String name, String value, Duration maxAge) {
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie
                .from(name, value)
                .httpOnly(true)
                .secure(secure)
                .path("/")
                .maxAge(maxAge)
                .sameSite(sameSite);

        if (!domain.isEmpty()) {
            builder.domain(domain);
        }

        return builder.build();
    }
}
