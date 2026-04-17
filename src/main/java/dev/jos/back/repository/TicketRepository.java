package dev.jos.back.repository;

import dev.jos.back.entities.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    List<Ticket> findAllByUser_EmailOrderByCreatedAtDesc(String email);

    Optional<Ticket> findByBarcode(String barcode);

    Optional<Ticket> findByCombinedKey(String combinedKey);
}
