package dev.jos.back.repository;

import dev.jos.back.entities.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository JPA pour l'entité {@link Transaction}.
 */
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    /**
     * Recherche une transaction par son identifiant et l'email de l'utilisateur propriétaire.
     *
     * @param id    l'identifiant de la transaction
     * @param email l'adresse email de l'utilisateur
     * @return la transaction si elle existe et appartient à l'utilisateur
     */
    Optional<Transaction> findByIdAndUser_Email(Long id, String email);
}
