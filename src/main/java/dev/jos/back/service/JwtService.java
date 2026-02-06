package dev.jos.back.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
public class JwtService {

    @Value("${jwt.access-token.secret}")
    private String accessTokenSecret;

    @Value("${jwt.access-token.expiration}")
    private long accessTokenExpiration;

    @Value("${jwt.refresh-token.secret}")
    private String refreshTokenSecret;

    @Value("${jwt.refresh-token.expiration}")
    private long refreshTokenExpiration;

    private SecretKey getKey(String secret) {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    }

    public String generateAccessToken(String username) {
        return Jwts.builder().subject(username).issuedAt(new Date()).expiration(new Date(System.currentTimeMillis() + accessTokenExpiration)).signWith(getKey(accessTokenSecret)).compact();
    }

    public String extractUsername(String token, boolean isRefreshToken) {
        String secret = isRefreshToken ? refreshTokenSecret : accessTokenSecret;
        return Jwts.parser().verifyWith(getKey(secret)).build().parseSignedClaims(token).getPayload().getSubject();
    }


    public String generateRefreshToken(String username) {
        return Jwts.builder().subject(username).issuedAt(new Date()).expiration(new Date(System.currentTimeMillis() + refreshTokenExpiration)).signWith(getKey(refreshTokenSecret)).compact();
    }
}