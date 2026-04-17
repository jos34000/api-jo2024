package dev.jos.back.dto.admin;

import java.math.BigDecimal;

public record SportSalesDTO(
        String sportName,
        Long ticketsSold,
        BigDecimal revenue
) {}
