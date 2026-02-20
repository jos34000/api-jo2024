package dev.jos.back.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "jwt")
@Data
public class JwtProperties {

    private String secret;
    private AccessToken accessToken;
    private RefreshToken refreshToken;

    @Data
    public static class AccessToken {
        private Long expiration;
        private String cookieName;
    }

    @Data
    public static class RefreshToken {
        private Long expiration;
        private String cookieName;
    }
}
