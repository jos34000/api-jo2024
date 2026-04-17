package dev.jos.back.service;

import dev.jos.back.dto.payment.CheckoutRequestDTO;
import dev.jos.back.dto.payment.TicketGroupResponseDTO;
import dev.jos.back.dto.payment.TransactionResponseDTO;
import dev.jos.back.mapper.TicketMapper;
import dev.jos.back.entities.*;
import dev.jos.back.exceptions.cart.CartNotFoundException;
import dev.jos.back.exceptions.payment.CartAlreadyConvertedException;
import dev.jos.back.exceptions.payment.CartEmptyException;
import dev.jos.back.exceptions.payment.PaymentDeclinedException;
import dev.jos.back.exceptions.payment.TransactionNotFoundException;
import dev.jos.back.exceptions.user.UserNotFoundException;
import dev.jos.back.repository.CartRepository;
import dev.jos.back.repository.EventRepository;
import dev.jos.back.repository.TicketRepository;
import dev.jos.back.repository.TransactionRepository;
import dev.jos.back.repository.UserRepository;
import dev.jos.back.util.PaymentResult;
import dev.jos.back.util.enums.CartStatus;
import dev.jos.back.util.enums.TransactionStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;


@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService implements ICheckoutService {

    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final TransactionRepository transactionRepository;
    private final TicketRepository ticketRepository;
    private final EventRepository eventRepository;
    private final PaymentGateway paymentGateway;
    private final PdfTicketService pdfTicketService;
    private final EmailService emailService;
    private final TicketMapper ticketMapper;

    @Transactional
    public TransactionResponseDTO checkout(String email, CheckoutRequestDTO dto) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Utilisateur introuvable"));

        Cart cart = cartRepository.findByUserAndStatus(user, CartStatus.ACTIVE)
                .orElseThrow(() -> new CartNotFoundException("Aucun panier actif"));

        if (cart.expireIfNeeded()) {
            cartRepository.save(cart);
            throw new CartNotFoundException("Le panier a expiré");
        }

        if (cart.getStatus() == CartStatus.CONVERTED) {
            throw new CartAlreadyConvertedException("Ce panier a déjà été payé");
        }

        if (cart.getCartItems().isEmpty()) {
            throw new CartEmptyException("Le panier est vide");
        }

        BigDecimal amount = cart.getCartItems().stream()
                .map(item -> BigDecimal.valueOf(item.getUnitPrice() * item.getQuantity()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        PaymentResult result = paymentGateway.processPayment(dto.cardNumber());

        if (!result.succeeded()) {
            throw new PaymentDeclinedException(result.declineReason());
        }

        String transactionKey = UUID.randomUUID().toString();
        String paymentReference = "REF-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Transaction transaction = new Transaction();
        transaction.setTransactionKey(transactionKey);
        transaction.setPaymentReference(paymentReference);
        transaction.setStatus(TransactionStatus.COMPLETED);
        transaction.setAmount(amount);
        transaction.setPaymentMethod(dto.paymentMethod());
        transaction.setPayedDate(LocalDateTime.now());
        transaction.setUser(user);
        transaction.setCart(cart);
        transactionRepository.save(transaction);

        List<Ticket> tickets = generateTickets(cart, transaction, user);
        ticketRepository.saveAll(tickets);

        Map<Event, Long> ticketsPerEvent = tickets.stream()
                .collect(Collectors.groupingBy(Ticket::getEvent, Collectors.counting()));
        ticketsPerEvent.forEach((event, count) -> {
            event.setAvailableSlots(Math.max(0, event.getAvailableSlots() - count.intValue()));
            eventRepository.save(event);
        });

        cart.setStatus(CartStatus.CONVERTED);
        cartRepository.save(cart);

        TransactionResponseDTO responseDTO = ticketMapper.toTransactionResponseDTO(transaction, tickets);

        try {
            String userLocale = user.getLocale() != null ? user.getLocale() : "fr";
            byte[] pdf = pdfTicketService.generate(responseDTO, userLocale);
            emailService.sendTicketsEmail(user.getEmail(), user.getFirstName(), responseDTO, pdf, userLocale);
        } catch (Exception e) {
            log.warn("Envoi email billets échoué pour transaction {} : {}", responseDTO.id(), e.getMessage());
        }

        return responseDTO;
    }

    @Transactional(readOnly = true)
    public TransactionResponseDTO getTransaction(String email, Long transactionId) {
        Transaction transaction = transactionRepository.findByIdAndUser_Email(transactionId, email)
                .orElseThrow(() -> new TransactionNotFoundException("Transaction introuvable"));

        List<Ticket> tickets = new ArrayList<>(transaction.getTickets());
        return ticketMapper.toTransactionResponseDTO(transaction, tickets);
    }

    @Transactional(readOnly = true)
    public byte[] getTicketsPdf(String email, Long transactionId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Utilisateur introuvable"));

        Transaction transaction = transactionRepository.findByIdAndUser_Email(transactionId, email)
                .orElseThrow(() -> new TransactionNotFoundException("Transaction introuvable"));

        List<Ticket> validTickets = transaction.getTickets().stream()
                .filter(t -> Boolean.TRUE.equals(t.getIsValid()) && !Boolean.TRUE.equals(t.getIsScanned()))
                .toList();

        if (validTickets.isEmpty()) {
            throw new IllegalStateException("Aucun billet valide disponible pour cette transaction");
        }

        TransactionResponseDTO dto = ticketMapper.toTransactionResponseDTO(transaction, validTickets);
        String userLocale = user.getLocale() != null ? user.getLocale() : "fr";
        return pdfTicketService.generate(dto, userLocale);
    }

    @Transactional(readOnly = true)
    public List<TicketGroupResponseDTO> getUserTicketGroups(String email) {
        userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Utilisateur introuvable"));

        List<Ticket> tickets = ticketRepository.findAllByUser_EmailOrderByCreatedAtDesc(email);

        Map<String, List<Ticket>> grouped = tickets.stream()
                .collect(Collectors.groupingBy(t ->
                        t.getTransaction().getId() + "-" + t.getEvent().getId() + "-" + t.getOffer().getId()));

        return grouped.values().stream()
                .map(ticketMapper::toTicketGroupResponseDTO)
                .sorted(Comparator.comparing(TicketGroupResponseDTO::purchasedAt).reversed())
                .toList();
    }

    private List<Ticket> generateTickets(Cart cart, Transaction transaction, User user) {
        List<Ticket> tickets = new ArrayList<>();

        for (CartItems item : cart.getCartItems()) {
            int totalTicketCount = item.getQuantity() * item.getOffer().getNumberOfTickets();
            double pricePerTicket = item.getUnitPrice() / item.getOffer().getNumberOfTickets();

            for (int i = 0; i < totalTicketCount; i++) {
                String ticketKey = UUID.randomUUID().toString();

                Ticket ticket = new Ticket();
                String userKey = UUID.randomUUID().toString();
                ticket.setUserKey(userKey);
                ticket.setTransactionKey(transaction.getTransactionKey());
                ticket.setTicketKey(ticketKey);
                ticket.setCombinedKey(computeCombinedKey(userKey, ticketKey));
                ticket.setBarcode("JO2024-" + ticketKey.substring(0, 8).toUpperCase());
                ticket.setPrice(pricePerTicket);
                ticket.setExpiryAt(item.getEvent().getEventDate());
                ticket.setIsValid(true);
                ticket.setIsScanned(false);
                ticket.setTransaction(transaction);
                ticket.setUser(user);
                ticket.setEvent(item.getEvent());
                ticket.setOffer(item.getOffer());
                tickets.add(ticket);
            }
        }

        return tickets;
    }

    private String computeCombinedKey(String userKey, String ticketKey) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest((userKey + ":" + ticketKey).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

}
