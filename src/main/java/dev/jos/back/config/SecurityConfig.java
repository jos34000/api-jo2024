package dev.jos.back.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;


@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CorsConfigurationSource corsConfigurationSource;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    String[] publicEndpoints = {
            "/api/auth/**",
            "/api/events/**",
            "/api/sport/**",
            "/api/offer/**",
            "/api/users/forget-password",
            "/api/users/validate-reset-token",
            "/api/2fa/send",
            "/api/2fa/verify",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/actuator/**"
    };

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(publicEndpoints).permitAll()
                        .requestMatchers(HttpMethod.POST,   "/api/events/**", "/api/offer/**", "/api/sport/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT,    "/api/events/**", "/api/offer/**", "/api/sport/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/events/**", "/api/offer/**", "/api/sport/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET,    "/api/user/all").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/user/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH,  "/api/user/*/role").hasRole("ADMIN")
                        .anyRequest().authenticated()
                ).addFilterBefore(jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
