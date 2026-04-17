package dev.jos.back.service;

import dev.jos.back.dto.ticket.ScanResponseDTO;
import dev.jos.back.entities.Ticket;
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

    public ScanResponseDTO scan(String combinedKey, String agentEmail) {
        Ticket ticket = ticketRepository.findByCombinedKey(combinedKey)
                .orElseThrow(() -> new TicketNotFoundException("Billet introuvable"));

        if (!Boolean.TRUE.equals(ticket.getIsValid())) {
            throw new TicketNotValidException("Ce billet a été annulé");
        }

        if (Boolean.TRUE.equals(ticket.getIsScanned())) {
            return ticketMapper.toScanResponseDTO(ticket, "ALREADY_USED");
        }

        ticket.setIsScanned(true);
        ticket.setScannedAt(LocalDateTime.now());
        ticket.setScannedBy(agentEmail);
        ticketRepository.save(ticket);

        return ticketMapper.toScanResponseDTO(ticket, "SUCCESS");
    }
}
