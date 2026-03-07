package dev.jos.back.repository;

import dev.jos.back.entities.OfferType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OfferTypeRepository extends JpaRepository<OfferType, Long> {
    Optional<OfferType> findByName(String name);
}
