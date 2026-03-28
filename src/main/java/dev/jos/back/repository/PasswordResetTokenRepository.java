package dev.jos.back.repository;

import dev.jos.back.entities.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByExpiryAfter(LocalDateTime date);

    Optional<PasswordResetToken> findByHashedToken(String hashedToken);

    @Modifying
    @Query("DELETE FROM PasswordResetToken t WHERE t.expiry < :threshold")
    void deleteExpired(@Param("threshold") LocalDateTime threshold);
}
