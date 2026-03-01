package dev.jos.back.repository;

import dev.jos.back.model.TwoFactorCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface TwoFactorCodeRepository extends JpaRepository<TwoFactorCode, Long> {
    @Query("""
            SELECT t FROM TwoFactorCode t
            WHERE t.user.id = :userId
              AND t.used = false
              AND t.expiresAt > :now
            ORDER BY t.createdAt DESC
            LIMIT 1
            """)
    Optional<TwoFactorCode> findLatestValid(@Param("userId") Long userId,
                                            @Param("now") LocalDateTime now);

    @Modifying
    @Query("UPDATE TwoFactorCode t SET t.used = true WHERE t.user.id = :userId AND t.used = false")
    void invalidateAll(@Param("userId") Long userId);

    @Modifying
    @Query("DELETE FROM TwoFactorCode t WHERE t.expiresAt < :threshold OR t.used = true")
    void deleteExpiredOrUsed(@Param("threshold") LocalDateTime threshold);
}
