package dev.jos.back.repository;

import dev.jos.back.entities.OfferTranslation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OfferTranslationRepository extends JpaRepository<OfferTranslation, Long> {

    Optional<OfferTranslation> findByOffer_IdAndLocale(Long offerId, String locale);

    @Query("SELECT t FROM OfferTranslation t WHERE t.offer.id IN :offerIds AND t.locale = :locale")
    List<OfferTranslation> findByOfferIdsAndLocale(@Param("offerIds") List<Long> offerIds,
                                                    @Param("locale") String locale);

    boolean existsByOffer_IdAndLocale(Long offerId, String locale);
}
