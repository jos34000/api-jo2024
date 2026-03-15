package dev.jos.back.repository;

import dev.jos.back.entities.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository JPA pour l'entité {@link Ticket}.
 */
public interface TicketRepository extends JpaRepository<Ticket, Long> {
}
