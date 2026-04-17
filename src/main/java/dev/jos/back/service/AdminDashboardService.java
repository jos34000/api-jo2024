package dev.jos.back.service;

import dev.jos.back.dto.admin.DashboardResponseDTO;
import dev.jos.back.dto.admin.OfferSalesDTO;
import dev.jos.back.repository.TicketRepository;
import dev.jos.back.repository.TransactionRepository;
import dev.jos.back.util.enums.TransactionStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminDashboardService {

    private final TransactionRepository transactionRepository;
    private final TicketRepository ticketRepository;

    public DashboardResponseDTO getDashboard() {
        BigDecimal totalRevenue = transactionRepository.sumCompletedRevenue();
        Long totalTicketsSold = ticketRepository.countSoldTickets();

        List<Object[]> statusRows = transactionRepository.countByStatus();
        Map<String, Long> transactionsByStatus = new LinkedHashMap<>();
        long totalTransactions = 0L;
        for (Object[] row : statusRows) {
            String key = ((TransactionStatus) row[0]).name();
            long count = (Long) row[1];
            transactionsByStatus.put(key, count);
            totalTransactions += count;
        }

        long total = totalTicketsSold != null ? totalTicketsSold : 0L;
        List<OfferSalesDTO> salesByOffer = ticketRepository.findSalesByOffer().stream()
                .map(o -> new OfferSalesDTO(
                        o.offerName(),
                        o.ticketsSold(),
                        o.revenue(),
                        total > 0 ? Math.round(o.ticketsSold() * 10000.0 / total) / 100.0 : 0.0
                ))
                .toList();

        return new DashboardResponseDTO(
                totalRevenue,
                totalTicketsSold,
                totalTransactions,
                transactionsByStatus,
                salesByOffer,
                ticketRepository.findTop10EventsBySales(),
                ticketRepository.findSalesBySport()
        );
    }
}
