package dev.jos.back.service;

import dev.jos.back.dto.ticket.ScanResponseDTO;
import dev.jos.back.entities.Ticket;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketValidationServiceTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private TicketMapper ticketMapper;

    @InjectMocks
    private TicketValidationService ticketValidationService;

    private static final String COMBINED_KEY = "a".repeat(64);
    private static final String AGENT_EMAIL = "agent@jo2024.fr";

    @Test
    void scan_returnsSuccessOutcome_whenCombinedKeyValid() {
        Ticket ticket = TestFixtures.ticketEntity("JO2024-ABCD1234");
        ScanResponseDTO expected = ScanResponseDTO.builder().outcome("SUCCESS").build();
        when(ticketRepository.findByCombinedKey(COMBINED_KEY)).thenReturn(Optional.of(ticket));
        when(ticketMapper.toScanResponseDTO(ticket, "SUCCESS")).thenReturn(expected);

        ScanResponseDTO result = ticketValidationService.scan(COMBINED_KEY, AGENT_EMAIL);

        assertThat(result.outcome()).isEqualTo("SUCCESS");
        verify(ticketRepository).save(ticket);
    }

    @Test
    void scan_setsScannedAtAndScannedBy_whenSuccess() {
        Ticket ticket = TestFixtures.ticketEntity("JO2024-ABCD1234");
        when(ticketRepository.findByCombinedKey(COMBINED_KEY)).thenReturn(Optional.of(ticket));
        when(ticketMapper.toScanResponseDTO(any(), eq("SUCCESS"))).thenReturn(ScanResponseDTO.builder().outcome("SUCCESS").build());

        ticketValidationService.scan(COMBINED_KEY, AGENT_EMAIL);

        assertThat(ticket.getIsScanned()).isTrue();
        assertThat(ticket.getScannedBy()).isEqualTo(AGENT_EMAIL);
        assertThat(ticket.getScannedAt()).isNotNull();
    }

    @Test
    void scan_returnsAlreadyUsedOutcome_whenTicketAlreadyScanned() {
        Ticket ticket = TestFixtures.ticketEntity("JO2024-ABCD1234");
        ticket.setIsScanned(true);
        ticket.setScannedAt(LocalDateTime.now().minusHours(1));
        ticket.setScannedBy("other@jo2024.fr");
        ScanResponseDTO expected = ScanResponseDTO.builder().outcome("ALREADY_USED").build();
        when(ticketRepository.findByCombinedKey(COMBINED_KEY)).thenReturn(Optional.of(ticket));
        when(ticketMapper.toScanResponseDTO(ticket, "ALREADY_USED")).thenReturn(expected);

        ScanResponseDTO result = ticketValidationService.scan(COMBINED_KEY, AGENT_EMAIL);

        assertThat(result.outcome()).isEqualTo("ALREADY_USED");
        verify(ticketRepository, never()).save(any());
    }

    @Test
    void scan_throwsTicketNotFoundException_whenCombinedKeyNotFound() {
        when(ticketRepository.findByCombinedKey(COMBINED_KEY)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ticketValidationService.scan(COMBINED_KEY, AGENT_EMAIL))
                .isInstanceOf(TicketNotFoundException.class);
    }

    @Test
    void scan_throwsTicketNotValidException_whenCancelled() {
        Ticket ticket = TestFixtures.ticketEntity("JO2024-ABCD1234");
        ticket.setIsValid(false);
        when(ticketRepository.findByCombinedKey(COMBINED_KEY)).thenReturn(Optional.of(ticket));

        assertThatThrownBy(() -> ticketValidationService.scan(COMBINED_KEY, AGENT_EMAIL))
                .isInstanceOf(TicketNotValidException.class)
                .hasMessage("Ce billet a été annulé");
    }

    @Test
    void scan_throwsTicketNotValidException_whenExpired() {
        Ticket ticket = TestFixtures.ticketEntity("JO2024-ABCD1234");
        ticket.setExpiryAt(LocalDateTime.now().minusDays(1));
        when(ticketRepository.findByCombinedKey(COMBINED_KEY)).thenReturn(Optional.of(ticket));

        assertThatThrownBy(() -> ticketValidationService.scan(COMBINED_KEY, AGENT_EMAIL))
                .isInstanceOf(TicketNotValidException.class)
                .hasMessage("Ce billet est expiré");
    }

    @Test
    void scan_doesNotThrow_whenExpiryAtIsNull() {
        Ticket ticket = TestFixtures.ticketEntity("JO2024-ABCD1234");
        ticket.setExpiryAt(null);
        when(ticketRepository.findByCombinedKey(COMBINED_KEY)).thenReturn(Optional.of(ticket));
        when(ticketMapper.toScanResponseDTO(any(), eq("SUCCESS"))).thenReturn(ScanResponseDTO.builder().outcome("SUCCESS").build());

        ticketValidationService.scan(COMBINED_KEY, AGENT_EMAIL);

        verify(ticketRepository).save(ticket);
    }
}
