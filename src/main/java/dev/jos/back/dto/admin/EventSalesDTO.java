package dev.jos.back.dto.admin;

import java.math.BigDecimal;

public record EventSalesDTO(
        Long eventId,
        String eventName,
        String sportName,
        Long ticketsSold,
        BigDecimal revenue
) {}
