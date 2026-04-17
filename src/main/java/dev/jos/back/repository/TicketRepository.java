package dev.jos.back.repository;

import dev.jos.back.dto.admin.EventSalesDTO;
import dev.jos.back.dto.admin.OfferSalesDTO;
import dev.jos.back.dto.admin.SportSalesDTO;
import dev.jos.back.entities.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    List<Ticket> findAllByUser_EmailOrderByCreatedAtDesc(String email);

    Optional<Ticket> findByBarcode(String barcode);

    Optional<Ticket> findByCombinedKey(String combinedKey);

    @Query("SELECT COUNT(tk) FROM Ticket tk WHERE tk.transaction.status = 'COMPLETED'")
    Long countSoldTickets();

    @Query("""
            SELECT new dev.jos.back.dto.admin.OfferSalesDTO(
                tk.offer.name, COUNT(tk),
                CAST(SUM(tk.price) AS java.math.BigDecimal), 0.0)
            FROM Ticket tk WHERE tk.transaction.status = 'COMPLETED'
            GROUP BY tk.offer.name ORDER BY COUNT(tk) DESC
            """)
    List<OfferSalesDTO> findSalesByOffer();

    @Query("""
            SELECT new dev.jos.back.dto.admin.EventSalesDTO(
                tk.event.id, tk.event.name, tk.event.sport.name,
                COUNT(tk), CAST(SUM(tk.price) AS java.math.BigDecimal))
            FROM Ticket tk WHERE tk.transaction.status = 'COMPLETED'
            GROUP BY tk.event.id, tk.event.name, tk.event.sport.name
            ORDER BY COUNT(tk) DESC LIMIT 10
            """)
    List<EventSalesDTO> findTop10EventsBySales();

    @Query("""
            SELECT new dev.jos.back.dto.admin.SportSalesDTO(
                tk.event.sport.name, COUNT(tk),
                CAST(SUM(tk.price) AS java.math.BigDecimal))
            FROM Ticket tk WHERE tk.transaction.status = 'COMPLETED'
            GROUP BY tk.event.sport.name ORDER BY COUNT(tk) DESC
            """)
    List<SportSalesDTO> findSalesBySport();
}
