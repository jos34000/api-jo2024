package dev.jos.back.repository;

import dev.jos.back.entities.EventTranslation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public interface EventTranslationRepository extends JpaRepository<EventTranslation, Long> {

    Optional<EventTranslation> findByEvent_IdAndLocale(Long eventId, String locale);

    @Query("SELECT t FROM EventTranslation t WHERE t.event.id IN :eventIds AND t.locale = :locale")
    List<EventTranslation> findByEventIdsAndLocale(@Param("eventIds") List<Long> eventIds,
                                                    @Param("locale") String locale);

    default Map<Long, String> findDescriptionMapByEventIdsAndLocale(List<Long> eventIds, String locale) {
        return findByEventIdsAndLocale(eventIds, locale).stream()
                .collect(Collectors.toMap(
                        t -> t.getEvent().getId(),
                        EventTranslation::getDescription
                ));
    }

    boolean existsByEvent_IdAndLocale(Long eventId, String locale);
}
