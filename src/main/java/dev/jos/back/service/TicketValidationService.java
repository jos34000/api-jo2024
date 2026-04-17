package dev.jos.back.service;

import dev.jos.back.dto.payment.TicketResponseDTO;
import dev.jos.back.entities.Ticket;
import dev.jos.back.exceptions.ticket.TicketAlreadyScannedException;
import dev.jos.back.exceptions.ticket.TicketNotFoundException;
import dev.jos.back.exceptions.ticket.TicketNotValidException;
import dev.jos.back.mapper.TicketMapper;
import dev.jos.back.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class TicketValidationService {

    private final TicketRepository ticketRepository;
    private final TicketMapper ticketMapper;

    public TicketResponseDTO scan(String barcode, String agentEmail) {
        Ticket ticket = ticketRepository.findByBarcode(barcode)
                .orElseThrow(() -> new TicketNotFoundException("Billet introuvable"));

        if (!Boolean.TRUE.equals(ticket.getIsValid())) {
            throw new TicketNotValidException("Ce billet a été annulé");
        }

        if (Boolean.TRUE.equals(ticket.getIsScanned())) {
            throw new TicketAlreadyScannedException("Ce billet a déjà été scanné");
        }

        if (ticket.getExpiryAt() != null && ticket.getExpiryAt().isBefore(LocalDateTime.now())) {
            throw new TicketNotValidException("Ce billet est expiré");
        }

        ticket.setIsScanned(true);
        ticket.setScannedAt(LocalDateTime.now());
        ticket.setScannedBy(agentEmail);
        ticketRepository.save(ticket);

        return ticketMapper.toTicketResponseDTO(ticket);
    }
}
