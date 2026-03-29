package dev.jos.back.service;

import dev.jos.back.dto.payment.CheckoutRequestDTO;
import dev.jos.back.dto.payment.TicketGroupResponseDTO;
import dev.jos.back.dto.payment.TransactionResponseDTO;

import java.util.List;

public interface ICheckoutService {
    TransactionResponseDTO checkout(String email, CheckoutRequestDTO dto);
    TransactionResponseDTO getTransaction(String email, Long transactionId);
    byte[] getTicketsPdf(String email, Long transactionId);
    List<TicketGroupResponseDTO> getUserTicketGroups(String email);
}
