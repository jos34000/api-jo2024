package dev.jos.back.service;

import dev.jos.back.dto.admin.DashboardResponseDTO;
import dev.jos.back.dto.admin.EventSalesDTO;
import dev.jos.back.dto.admin.OfferSalesDTO;
import dev.jos.back.dto.admin.SportSalesDTO;
import dev.jos.back.repository.TicketRepository;
import dev.jos.back.repository.TransactionRepository;
import dev.jos.back.util.enums.TransactionStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminDashboardServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private TicketRepository ticketRepository;

    @InjectMocks
    private AdminDashboardService adminDashboardService;

    @Test
    void getDashboard_returnsCorrectTotalRevenue_whenCompletedTransactionsExist() {
        when(transactionRepository.sumCompletedRevenue()).thenReturn(new BigDecimal("1045.00"));
        when(ticketRepository.countSoldTickets()).thenReturn(28L);
        when(transactionRepository.countByStatus()).thenReturn(List.of());
        when(ticketRepository.findSalesByOffer()).thenReturn(List.of());
        when(ticketRepository.findTop10EventsBySales()).thenReturn(List.of());
        when(ticketRepository.findSalesBySport()).thenReturn(List.of());

        DashboardResponseDTO result = adminDashboardService.getDashboard();

        assertThat(result.totalRevenue()).isEqualByComparingTo(new BigDecimal("1045.00"));
    }

    @Test
    void getDashboard_returnsZeroRevenue_whenNoCompletedTransactions() {
        when(transactionRepository.sumCompletedRevenue()).thenReturn(BigDecimal.ZERO);
        when(ticketRepository.countSoldTickets()).thenReturn(0L);
        when(transactionRepository.countByStatus()).thenReturn(List.of());
        when(ticketRepository.findSalesByOffer()).thenReturn(List.of());
        when(ticketRepository.findTop10EventsBySales()).thenReturn(List.of());
        when(ticketRepository.findSalesBySport()).thenReturn(List.of());

        DashboardResponseDTO result = adminDashboardService.getDashboard();

        assertThat(result.totalRevenue()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.totalTicketsSold()).isZero();
    }

    @Test
    void getDashboard_computesCorrectPercentage_forOfferSales() {
        when(transactionRepository.sumCompletedRevenue()).thenReturn(new BigDecimal("200.00"));
        when(ticketRepository.countSoldTickets()).thenReturn(10L);
        when(transactionRepository.countByStatus()).thenReturn(List.of());
        when(ticketRepository.findSalesByOffer()).thenReturn(List.of(
                new OfferSalesDTO("Solo", 4L, new BigDecimal("160.00"), 0.0),
                new OfferSalesDTO("Duo", 6L, new BigDecimal("40.00"), 0.0)
        ));
        when(ticketRepository.findTop10EventsBySales()).thenReturn(List.of());
        when(ticketRepository.findSalesBySport()).thenReturn(List.of());

        DashboardResponseDTO result = adminDashboardService.getDashboard();

        assertThat(result.salesByOffer()).hasSize(2);
        assertThat(result.salesByOffer().get(0).percentage()).isEqualTo(40.0);
        assertThat(result.salesByOffer().get(1).percentage()).isEqualTo(60.0);
    }

    @Test
    void getDashboard_returnsZeroPercentage_whenTotalTicketsSoldIsZero() {
        when(transactionRepository.sumCompletedRevenue()).thenReturn(BigDecimal.ZERO);
        when(ticketRepository.countSoldTickets()).thenReturn(0L);
        when(transactionRepository.countByStatus()).thenReturn(List.of());
        when(ticketRepository.findSalesByOffer()).thenReturn(List.of(
                new OfferSalesDTO("Solo", 0L, BigDecimal.ZERO, 0.0)
        ));
        when(ticketRepository.findTop10EventsBySales()).thenReturn(List.of());
        when(ticketRepository.findSalesBySport()).thenReturn(List.of());

        DashboardResponseDTO result = adminDashboardService.getDashboard();

        assertThat(result.salesByOffer().get(0).percentage()).isEqualTo(0.0);
    }

    @Test
    void getDashboard_returnsCorrectTransactionCounts_groupedByStatus() {
        when(transactionRepository.sumCompletedRevenue()).thenReturn(BigDecimal.ZERO);
        when(ticketRepository.countSoldTickets()).thenReturn(0L);
        when(transactionRepository.countByStatus()).thenReturn(List.of(
                new Object[]{TransactionStatus.COMPLETED, 12L},
                new Object[]{TransactionStatus.CANCELED, 1L},
                new Object[]{TransactionStatus.FAILED, 1L}
        ));
        when(ticketRepository.findSalesByOffer()).thenReturn(List.of());
        when(ticketRepository.findTop10EventsBySales()).thenReturn(List.of());
        when(ticketRepository.findSalesBySport()).thenReturn(List.of());

        DashboardResponseDTO result = adminDashboardService.getDashboard();

        assertThat(result.transactionsByStatus()).containsEntry("COMPLETED", 12L);
        assertThat(result.transactionsByStatus()).containsEntry("CANCELED", 1L);
        assertThat(result.transactionsByStatus()).containsEntry("FAILED", 1L);
    }

    @Test
    void getDashboard_sumsTotalTransactions_fromAllStatusGroups() {
        when(transactionRepository.sumCompletedRevenue()).thenReturn(BigDecimal.ZERO);
        when(ticketRepository.countSoldTickets()).thenReturn(0L);
        when(transactionRepository.countByStatus()).thenReturn(List.of(
                new Object[]{TransactionStatus.COMPLETED, 12L},
                new Object[]{TransactionStatus.CANCELED, 1L},
                new Object[]{TransactionStatus.FAILED, 1L}
        ));
        when(ticketRepository.findSalesByOffer()).thenReturn(List.of());
        when(ticketRepository.findTop10EventsBySales()).thenReturn(List.of());
        when(ticketRepository.findSalesBySport()).thenReturn(List.of());

        DashboardResponseDTO result = adminDashboardService.getDashboard();

        assertThat(result.totalTransactions()).isEqualTo(14L);
    }

    @Test
    void getDashboard_returnsTop10Events_fromRepository() {
        List<EventSalesDTO> events = List.of(
                new EventSalesDTO(2L, "100m Hommes - Finale", "Athlétisme", 3L, new BigDecimal("120.00")),
                new EventSalesDTO(108L, "Basketball Hommes - Finale", "Basketball", 4L, new BigDecimal("150.00"))
        );
        when(transactionRepository.sumCompletedRevenue()).thenReturn(BigDecimal.ZERO);
        when(ticketRepository.countSoldTickets()).thenReturn(0L);
        when(transactionRepository.countByStatus()).thenReturn(List.of());
        when(ticketRepository.findSalesByOffer()).thenReturn(List.of());
        when(ticketRepository.findTop10EventsBySales()).thenReturn(events);
        when(ticketRepository.findSalesBySport()).thenReturn(List.of());

        DashboardResponseDTO result = adminDashboardService.getDashboard();

        assertThat(result.salesByEvent()).isEqualTo(events);
    }

    @Test
    void getDashboard_returnsSalesBySport_fromRepository() {
        List<SportSalesDTO> sports = List.of(
                new SportSalesDTO("Athlétisme", 10L, new BigDecimal("400.00")),
                new SportSalesDTO("Basketball", 6L, new BigDecimal("230.00"))
        );
        when(transactionRepository.sumCompletedRevenue()).thenReturn(BigDecimal.ZERO);
        when(ticketRepository.countSoldTickets()).thenReturn(0L);
        when(transactionRepository.countByStatus()).thenReturn(List.of());
        when(ticketRepository.findSalesByOffer()).thenReturn(List.of());
        when(ticketRepository.findTop10EventsBySales()).thenReturn(List.of());
        when(ticketRepository.findSalesBySport()).thenReturn(sports);

        DashboardResponseDTO result = adminDashboardService.getDashboard();

        assertThat(result.salesBySport()).isEqualTo(sports);
    }
}
