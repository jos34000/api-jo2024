package dev.jos.back.repository;

import dev.jos.back.entities.SportTranslation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SportTranslationRepository extends JpaRepository<SportTranslation, Long> {

    Optional<SportTranslation> findBySport_IdAndLocale(Long sportId, String locale);

    @Query("SELECT t FROM SportTranslation t WHERE t.sport.id IN :sportIds AND t.locale = :locale")
    List<SportTranslation> findBySportIdsAndLocale(@Param("sportIds") List<Long> sportIds,
                                                    @Param("locale") String locale);

    boolean existsBySport_IdAndLocale(Long sportId, String locale);
}
