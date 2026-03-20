package dev.jos.back.repository;

import dev.jos.back.entities.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    
    List<Ticket> findAllByUser_EmailOrderByCreatedAtDesc(String email);
}
