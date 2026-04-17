package dev.jos.back.service;

import dev.jos.back.dto.payment.TicketResponseDTO;
import dev.jos.back.entities.Ticket;
import dev.jos.back.exceptions.ticket.TicketAlreadyScannedException;
import dev.jos.back.exceptions.ticket.TicketNotFoundException;
import dev.jos.back.exceptions.ticket.TicketNotValidException;
import dev.jos.back.mapper.TicketMapper;
import dev.jos.back.repository.TicketRepository;
import dev.jos.back.support.TestFixtures;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketValidationServiceTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private TicketMapper ticketMapper;

    @InjectMocks
    private TicketValidationService ticketValidationService;

    private static final String BARCODE = "JO2024-ABCD1234";
    private static final String AGENT_EMAIL = "agent@jo2024.fr";

    @Test
    void scan_returnsUsedTicket_whenBarcodeValid() {
        Ticket ticket = TestFixtures.ticketEntity(BARCODE);
        TicketResponseDTO expected = TestFixtures.ticket("ABCD1234", 50.0);
        when(ticketRepository.findByBarcode(BARCODE)).thenReturn(Optional.of(ticket));
        when(ticketMapper.toTicketResponseDTO(ticket)).thenReturn(expected);

        TicketResponseDTO result = ticketValidationService.scan(BARCODE, AGENT_EMAIL);

        assertThat(result).isEqualTo(expected);
        verify(ticketRepository).save(ticket);
    }

    @Test
    void scan_setsScannedAtAndScannedBy_whenSuccess() {
        Ticket ticket = TestFixtures.ticketEntity(BARCODE);
        when(ticketRepository.findByBarcode(BARCODE)).thenReturn(Optional.of(ticket));
        when(ticketMapper.toTicketResponseDTO(any())).thenReturn(TestFixtures.ticket("ABCD1234", 50.0));

        ticketValidationService.scan(BARCODE, AGENT_EMAIL);

        assertThat(ticket.getIsScanned()).isTrue();
        assertThat(ticket.getScannedBy()).isEqualTo(AGENT_EMAIL);
        assertThat(ticket.getScannedAt()).isNotNull();
    }

    @Test
    void scan_throwsTicketNotFoundException_whenBarcodeNotFound() {
        when(ticketRepository.findByBarcode(BARCODE)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ticketValidationService.scan(BARCODE, AGENT_EMAIL))
                .isInstanceOf(TicketNotFoundException.class);
    }

    @Test
    void scan_throwsTicketNotValidException_whenTicketCancelled() {
        Ticket ticket = TestFixtures.ticketEntity(BARCODE);
        ticket.setIsValid(false);
        when(ticketRepository.findByBarcode(BARCODE)).thenReturn(Optional.of(ticket));

        assertThatThrownBy(() -> ticketValidationService.scan(BARCODE, AGENT_EMAIL))
                .isInstanceOf(TicketNotValidException.class)
                .hasMessage("Ce billet a été annulé");
    }

    @Test
    void scan_throwsTicketAlreadyScannedException_whenAlreadyScanned() {
        Ticket ticket = TestFixtures.ticketEntity(BARCODE);
        ticket.setIsScanned(true);
        when(ticketRepository.findByBarcode(BARCODE)).thenReturn(Optional.of(ticket));

        assertThatThrownBy(() -> ticketValidationService.scan(BARCODE, AGENT_EMAIL))
                .isInstanceOf(TicketAlreadyScannedException.class)
                .hasMessage("Ce billet a déjà été scanné");
    }

    @Test
    void scan_throwsTicketNotValidException_whenTicketExpired() {
        Ticket ticket = TestFixtures.ticketEntity(BARCODE);
        ticket.setExpiryAt(LocalDateTime.now().minusDays(1));
        when(ticketRepository.findByBarcode(BARCODE)).thenReturn(Optional.of(ticket));

        assertThatThrownBy(() -> ticketValidationService.scan(BARCODE, AGENT_EMAIL))
                .isInstanceOf(TicketNotValidException.class)
                .hasMessage("Ce billet est expiré");
    }

    @Test
    void scan_doesNotThrow_whenExpiryAtIsNull() {
        Ticket ticket = TestFixtures.ticketEntity(BARCODE);
        ticket.setExpiryAt(null);
        when(ticketRepository.findByBarcode(BARCODE)).thenReturn(Optional.of(ticket));
        when(ticketMapper.toTicketResponseDTO(any())).thenReturn(TestFixtures.ticket("ABCD1234", 50.0));

        ticketValidationService.scan(BARCODE, AGENT_EMAIL);

        verify(ticketRepository).save(ticket);
    }
}
