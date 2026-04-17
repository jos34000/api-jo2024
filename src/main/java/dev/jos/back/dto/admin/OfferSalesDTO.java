package dev.jos.back.dto.admin;

import java.math.BigDecimal;

public record OfferSalesDTO(
        String offerName,
        Long ticketsSold,
        BigDecimal revenue,
        Double percentage
) {}
