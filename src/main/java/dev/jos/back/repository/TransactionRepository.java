package dev.jos.back.repository;

import dev.jos.back.entities.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    
    Optional<Transaction> findByIdAndUser_Email(Long id, String email);
}
