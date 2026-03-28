package dev.jos.back.service;

import dev.jos.back.entities.PasswordResetToken;
import dev.jos.back.entities.User;
import dev.jos.back.repository.PasswordResetTokenRepository;
import dev.jos.back.util.enums.TokenValidationResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.Optional;

@Slf4j
@Service
public class HmacResetTokenStore implements ResetTokenStore {

    private final PasswordResetTokenRepository repository;
    private final SecretKeySpec signingKey;

    public HmacResetTokenStore(PasswordResetTokenRepository repository,
                                @Value("${app.reset-token.secret}") String secret) {
        this.repository = repository;
        this.signingKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    }

    @Override
    @Transactional
    public void store(User user, String plainToken, LocalDateTime expiry) {
        repository.save(new PasswordResetToken(user, hmac(plainToken), expiry));
    }

    @Override
    @Transactional(readOnly = true)
    public TokenValidationResult validate(String plainToken) {
        Optional<PasswordResetToken> opt = repository.findByHashedToken(hmac(plainToken));
        if (opt.isEmpty()) return TokenValidationResult.NOT_FOUND;
        PasswordResetToken token = opt.get();
        if (token.isUsed()) return TokenValidationResult.NOT_FOUND;
        if (token.getExpiry().isBefore(LocalDateTime.now())) return TokenValidationResult.EXPIRED;
        return TokenValidationResult.VALID;
    }

    @Override
    @Transactional
    public void consume(String plainToken) {
        repository.findByHashedToken(hmac(plainToken)).ifPresent(t -> {
            t.setUsed(true);
            repository.save(t);
        });
    }

    @Override
    @Transactional
    public void purgeExpired() {
        repository.deleteExpired(LocalDateTime.now());
    }

    private String hmac(String token) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(signingKey);
            byte[] raw = mac.doFinal(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(raw);
        } catch (Exception e) {
            throw new IllegalStateException("HMAC computation failed", e);
        }
    }
}
