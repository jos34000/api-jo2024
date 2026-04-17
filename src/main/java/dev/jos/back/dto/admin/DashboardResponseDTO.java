package dev.jos.back.dto.admin;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record DashboardResponseDTO(
        BigDecimal totalRevenue,
        Long totalTicketsSold,
        Long totalTransactions,
        Map<String, Long> transactionsByStatus,
        List<OfferSalesDTO> salesByOffer,
        List<EventSalesDTO> salesByEvent,
        List<SportSalesDTO> salesBySport
) {}
