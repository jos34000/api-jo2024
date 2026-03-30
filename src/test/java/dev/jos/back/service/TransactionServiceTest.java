package dev.jos.back.service;

import dev.jos.back.dto.payment.CheckoutRequestDTO;
import dev.jos.back.dto.payment.TicketGroupResponseDTO;
import dev.jos.back.dto.payment.TransactionResponseDTO;
import dev.jos.back.entities.*;
import dev.jos.back.exceptions.cart.CartNotFoundException;
import dev.jos.back.exceptions.payment.CartAlreadyConvertedException;
import dev.jos.back.exceptions.payment.CartEmptyException;
import dev.jos.back.exceptions.payment.PaymentDeclinedException;
import dev.jos.back.exceptions.payment.TransactionNotFoundException;
import dev.jos.back.exceptions.user.UserNotFoundException;
import dev.jos.back.mapper.TicketMapper;
import dev.jos.back.repository.*;
import dev.jos.back.util.PaymentResult;
import dev.jos.back.util.enums.CartStatus;
import dev.jos.back.util.enums.PaymentMethod;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock UserRepository userRepository;
    @Mock CartRepository cartRepository;
    @Mock TransactionRepository transactionRepository;
    @Mock TicketRepository ticketRepository;
    @Mock EventRepository eventRepository;
    @Mock PaymentGateway paymentGateway;
    @Mock PdfTicketService pdfTicketService;
    @Mock EmailService emailService;
    @Mock TicketMapper ticketMapper;
    @InjectMocks TransactionService transactionService;

    private static final CheckoutRequestDTO CHECKOUT_DTO =
            new CheckoutRequestDTO("4111111111111111", 12, 2025, "123", PaymentMethod.CREDIT_CARD);

    // ── checkout : cas nominaux ───────────────────────────────────────────────

    @Test
    void checkout_savesTransactionAndTickets_andConvertsCart() {
        User user = buildUser("alice@example.com");
        Event event = buildEvent(1L, 50);
        Offer offer = buildOffer(1);
        CartItems item = buildItem(event, offer, 1, 100.0);
        Cart cart = buildActiveCart(1L, Set.of(item));

        TransactionResponseDTO expectedDTO = TransactionResponseDTO.builder().id(1L).build();

        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(user));
        when(cartRepository.findByUserAndStatus(user, CartStatus.ACTIVE)).thenReturn(Optional.of(cart));
        when(paymentGateway.processPayment("4111111111111111")).thenReturn(PaymentResult.success());
        when(ticketMapper.toTransactionResponseDTO(any(Transaction.class), anyList())).thenReturn(expectedDTO);

        TransactionResponseDTO result = transactionService.checkout("alice@example.com", CHECKOUT_DTO);

        assertThat(result.id()).isEqualTo(1L);
        assertThat(cart.getStatus()).isEqualTo(CartStatus.CONVERTED);
        verify(transactionRepository).save(any(Transaction.class));
        verify(ticketRepository).saveAll(anyList());
        verify(cartRepository).save(cart);
    }

    @Test
    void checkout_generatesCorrectNumberOfTickets_perOfferNumberOfTickets() {
        User user = buildUser("alice@example.com");
        Event event = buildEvent(1L, 50);
        Offer offer = buildOffer(2); // offre duo = 2 billets par quantité
        CartItems item = buildItem(event, offer, 3, 200.0); // 3 × 2 = 6 billets attendus
        Cart cart = buildActiveCart(1L, Set.of(item));

        TransactionResponseDTO expectedDTO = TransactionResponseDTO.builder().id(1L).build();

        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(user));
        when(cartRepository.findByUserAndStatus(user, CartStatus.ACTIVE)).thenReturn(Optional.of(cart));
        when(paymentGateway.processPayment("4111111111111111")).thenReturn(PaymentResult.success());
        when(ticketMapper.toTransactionResponseDTO(any(Transaction.class), anyList())).thenReturn(expectedDTO);

        transactionService.checkout("alice@example.com", CHECKOUT_DTO);

        // 3 quantités × 2 billets/offre = 6 tickets sauvegardés
        verify(ticketRepository).saveAll(argThatHasSize(6));
    }

    @Test
    void checkout_calculatesAmountFromCartItems() {
        User user = buildUser("alice@example.com");
        Event event = buildEvent(1L, 50);
        Offer offer = buildOffer(1);
        CartItems item = buildItem(event, offer, 2, 75.0); // 2 × 75 = 150
        Cart cart = buildActiveCart(1L, Set.of(item));

        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(user));
        when(cartRepository.findByUserAndStatus(user, CartStatus.ACTIVE)).thenReturn(Optional.of(cart));
        when(paymentGateway.processPayment("4111111111111111")).thenReturn(PaymentResult.success());
        when(ticketMapper.toTransactionResponseDTO(any(Transaction.class), anyList()))
                .thenReturn(TransactionResponseDTO.builder().id(1L).build());

        transactionService.checkout("alice@example.com", CHECKOUT_DTO);

        // Vérifie que la transaction sauvegardée a le bon montant
        org.mockito.ArgumentCaptor<Transaction> txCaptor =
                org.mockito.ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(txCaptor.capture());
        assertThat(txCaptor.getValue().getAmount()).isEqualByComparingTo(BigDecimal.valueOf(150.0));
    }

    // ── checkout : cas d'erreur ───────────────────────────────────────────────

    @Test
    void checkout_throwsUserNotFoundException_whenUserNotFound() {
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.checkout("unknown@example.com", CHECKOUT_DTO))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void checkout_throwsCartNotFoundException_whenNoActiveCart() {
        User user = buildUser("alice@example.com");

        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(user));
        when(cartRepository.findByUserAndStatus(user, CartStatus.ACTIVE)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.checkout("alice@example.com", CHECKOUT_DTO))
                .isInstanceOf(CartNotFoundException.class);
    }

    @Test
    void checkout_throwsCartNotFoundException_whenCartExpired() {
        User user = buildUser("alice@example.com");
        Cart cart = new Cart();
        cart.setStatus(CartStatus.ACTIVE);
        cart.setExpiresAt(LocalDateTime.now().minusMinutes(1)); // déjà expiré

        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(user));
        when(cartRepository.findByUserAndStatus(user, CartStatus.ACTIVE)).thenReturn(Optional.of(cart));

        assertThatThrownBy(() -> transactionService.checkout("alice@example.com", CHECKOUT_DTO))
                .isInstanceOf(CartNotFoundException.class);
        verify(cartRepository).save(cart); // sauvegarde du statut ABANDONED
    }

    @Test
    void checkout_throwsCartAlreadyConvertedException_whenCartAlreadyPaid() {
        User user = buildUser("alice@example.com");
        Cart cart = new Cart();
        cart.setStatus(CartStatus.CONVERTED);
        cart.setExpiresAt(LocalDateTime.now().plusMinutes(30)); // non expiré

        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(user));
        when(cartRepository.findByUserAndStatus(user, CartStatus.ACTIVE)).thenReturn(Optional.of(cart));

        assertThatThrownBy(() -> transactionService.checkout("alice@example.com", CHECKOUT_DTO))
                .isInstanceOf(CartAlreadyConvertedException.class);
    }

    @Test
    void checkout_throwsCartEmptyException_whenCartHasNoItems() {
        User user = buildUser("alice@example.com");
        Cart cart = buildActiveCart(1L, Set.of()); // panier vide

        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(user));
        when(cartRepository.findByUserAndStatus(user, CartStatus.ACTIVE)).thenReturn(Optional.of(cart));

        assertThatThrownBy(() -> transactionService.checkout("alice@example.com", CHECKOUT_DTO))
                .isInstanceOf(CartEmptyException.class);
    }

    @Test
    void checkout_throwsPaymentDeclinedException_whenPaymentFails() {
        User user = buildUser("alice@example.com");
        Event event = buildEvent(1L, 50);
        Offer offer = buildOffer(1);
        CartItems item = buildItem(event, offer, 1, 100.0);
        Cart cart = buildActiveCart(1L, Set.of(item));

        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(user));
        when(cartRepository.findByUserAndStatus(user, CartStatus.ACTIVE)).thenReturn(Optional.of(cart));
        when(paymentGateway.processPayment("4111111111111111"))
                .thenReturn(PaymentResult.failure("Carte déclinée"));

        assertThatThrownBy(() -> transactionService.checkout("alice@example.com", CHECKOUT_DTO))
                .isInstanceOf(PaymentDeclinedException.class)
                .hasMessageContaining("Carte déclinée");
    }

    // ── getTransaction ────────────────────────────────────────────────────────

    @Test
    void getTransaction_returnsDTO_whenFound() {
        Transaction transaction = new Transaction();
        transaction.setId(10L);
        transaction.setTickets(Set.of());

        TransactionResponseDTO expectedDTO = TransactionResponseDTO.builder().id(10L).build();

        when(transactionRepository.findByIdAndUser_Email(10L, "alice@example.com"))
                .thenReturn(Optional.of(transaction));
        when(ticketMapper.toTransactionResponseDTO(any(Transaction.class), anyList()))
                .thenReturn(expectedDTO);

        TransactionResponseDTO result = transactionService.getTransaction("alice@example.com", 10L);

        assertThat(result.id()).isEqualTo(10L);
    }

    @Test
    void getTransaction_throwsTransactionNotFoundException_whenNotFound() {
        when(transactionRepository.findByIdAndUser_Email(99L, "alice@example.com"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.getTransaction("alice@example.com", 99L))
                .isInstanceOf(TransactionNotFoundException.class);
    }

    // ── getTicketsPdf ─────────────────────────────────────────────────────────

    @Test
    void getTicketsPdf_throwsUserNotFoundException_whenUserNotFound() {
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.getTicketsPdf("unknown@example.com", 1L))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void getTicketsPdf_throwsTransactionNotFoundException_whenTransactionNotFound() {
        User user = buildUser("alice@example.com");

        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(user));
        when(transactionRepository.findByIdAndUser_Email(99L, "alice@example.com"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.getTicketsPdf("alice@example.com", 99L))
                .isInstanceOf(TransactionNotFoundException.class);
    }

    @Test
    void getTicketsPdf_throwsIllegalStateException_whenNoValidTickets() {
        User user = buildUser("alice@example.com");

        Ticket scanned = new Ticket();
        scanned.setIsValid(true);
        scanned.setIsScanned(true); // billet déjà scanné — exclu

        Transaction transaction = new Transaction();
        transaction.setId(1L);
        transaction.setTickets(Set.of(scanned));

        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(user));
        when(transactionRepository.findByIdAndUser_Email(1L, "alice@example.com"))
                .thenReturn(Optional.of(transaction));

        assertThatThrownBy(() -> transactionService.getTicketsPdf("alice@example.com", 1L))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void getTicketsPdf_returnsPdfBytes_whenValidTicketsExist() {
        User user = buildUser("alice@example.com");

        Ticket validTicket = new Ticket();
        validTicket.setIsValid(true);
        validTicket.setIsScanned(false);

        Transaction transaction = new Transaction();
        transaction.setId(1L);
        transaction.setTickets(Set.of(validTicket));

        byte[] pdfBytes = new byte[]{1, 2, 3};

        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(user));
        when(transactionRepository.findByIdAndUser_Email(1L, "alice@example.com"))
                .thenReturn(Optional.of(transaction));
        when(ticketMapper.toTransactionResponseDTO(any(Transaction.class), anyList()))
                .thenReturn(TransactionResponseDTO.builder().id(1L).build());
        when(pdfTicketService.generate(any(), any())).thenReturn(pdfBytes);

        byte[] result = transactionService.getTicketsPdf("alice@example.com", 1L);

        assertThat(result).isEqualTo(pdfBytes);
    }

    // ── getUserTicketGroups ───────────────────────────────────────────────────

    @Test
    void getUserTicketGroups_throwsUserNotFoundException_whenUserNotFound() {
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.getUserTicketGroups("unknown@example.com"))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void getUserTicketGroups_returnsGroupedDTOs() {
        User user = buildUser("alice@example.com");

        Event event = buildEvent(1L, 50);
        Offer offer = buildOffer(1);
        Transaction transaction = new Transaction();
        transaction.setId(1L);

        Ticket ticket = new Ticket();
        ticket.setTransaction(transaction);
        ticket.setEvent(event);
        ticket.setOffer(offer);

        TicketGroupResponseDTO groupDTO = TicketGroupResponseDTO.builder()
                .transactionId(1L)
                .totalSeats(1)
                .build();

        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(user));
        when(ticketRepository.findAllByUser_EmailOrderByCreatedAtDesc("alice@example.com"))
                .thenReturn(List.of(ticket));
        when(ticketMapper.toTicketGroupResponseDTO(anyList())).thenReturn(groupDTO);

        List<TicketGroupResponseDTO> result = transactionService.getUserTicketGroups("alice@example.com");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).transactionId()).isEqualTo(1L);
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private User buildUser(String email) {
        User user = new User();
        user.setEmail(email);
        user.setFirstName("Alice");
        user.setLocale("fr");
        return user;
    }

    private Event buildEvent(Long id, int slots) {
        Event event = new Event();
        event.setId(id);
        event.setAvailableSlots(slots);
        event.setEventDate(LocalDateTime.of(2024, 7, 26, 10, 0));
        return event;
    }

    private Offer buildOffer(int numberOfTickets) {
        Offer offer = new Offer();
        offer.setNumberOfTickets(numberOfTickets);
        return offer;
    }

    private CartItems buildItem(Event event, Offer offer, int quantity, double unitPrice) {
        CartItems item = new CartItems();
        item.setEvent(event);
        item.setOffer(offer);
        item.setQuantity(quantity);
        item.setUnitPrice(unitPrice);
        return item;
    }

    private Cart buildActiveCart(Long id, Set<CartItems> items) {
        Cart cart = new Cart();
        cart.setId(id);
        cart.setStatus(CartStatus.ACTIVE);
        cart.setExpiresAt(LocalDateTime.now().plusMinutes(30));
        items.forEach(cart.getCartItems()::add);
        return cart;
    }

    /**
     * Matcher Mockito permettant de vérifier la taille d'une liste passée en argument.
     */
    private static <T> List<T> argThatHasSize(int size) {
        return org.mockito.ArgumentMatchers.argThat(list -> list != null && list.size() == size);
    }
}
