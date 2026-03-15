package dev.jos.back.service;

import dev.jos.back.dto.cart.CartEventSummaryDTO;
import dev.jos.back.dto.cart.CartOfferSummaryDTO;
import dev.jos.back.dto.payment.CheckoutRequestDTO;
import dev.jos.back.dto.payment.TicketResponseDTO;
import dev.jos.back.dto.payment.TransactionResponseDTO;
import dev.jos.back.entities.*;
import dev.jos.back.exceptions.cart.CartNotFoundException;
import dev.jos.back.exceptions.payment.CartAlreadyConvertedException;
import dev.jos.back.exceptions.payment.CartEmptyException;
import dev.jos.back.exceptions.payment.PaymentDeclinedException;
import dev.jos.back.exceptions.payment.TransactionNotFoundException;
import dev.jos.back.exceptions.user.UserNotFoundException;
import dev.jos.back.repository.CartRepository;
import dev.jos.back.repository.TicketRepository;
import dev.jos.back.repository.TransactionRepository;
import dev.jos.back.repository.UserRepository;
import dev.jos.back.util.enums.CartStatus;
import dev.jos.back.util.enums.TransactionStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Service orchestrant le tunnel de paiement et la génération des billets.
 * <p>
 * Gère la validation du panier, l'appel au système de paiement simulé,
 * la création de la transaction, la génération des billets et la conversion du panier.
 * </p>
 */
@Service
@RequiredArgsConstructor
public class TransactionService {

    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final TransactionRepository transactionRepository;
    private final TicketRepository ticketRepository;
    private final PaymentMockService paymentMockService;

    /**
     * Traite le paiement du panier actif de l'utilisateur.
     * <p>
     * Si le paiement est accepté, une transaction {@code COMPLETED} est créée,
     * les billets sont générés et le panier est marqué {@code CONVERTED}.
     * Si le paiement est refusé, une {@link PaymentDeclinedException} est levée.
     * </p>
     *
     * @param email l'adresse email de l'utilisateur authentifié
     * @param dto   les données de paiement (numéro de carte, expiry, CVV, méthode)
     * @return la transaction créée avec la liste des billets générés
     * @throws UserNotFoundException          si l'utilisateur est introuvable
     * @throws CartNotFoundException          si aucun panier actif n'existe ou s'il a expiré
     * @throws CartAlreadyConvertedException  si le panier a déjà été converti en commande
     * @throws CartEmptyException             si le panier ne contient aucun article
     * @throws PaymentDeclinedException       si le paiement est refusé par le système mock
     */
    @Transactional
    public TransactionResponseDTO checkout(String email, CheckoutRequestDTO dto) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Utilisateur introuvable"));

        Cart cart = cartRepository.findByUserAndStatus(user, CartStatus.ACTIVE)
                .orElseThrow(() -> new CartNotFoundException("Aucun panier actif"));

        if (LocalDateTime.now().isAfter(cart.getExpiresAt())) {
            cart.setStatus(CartStatus.ABANDONED);
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

        PaymentMockService.PaymentResult result = paymentMockService.processPayment(dto.cardNumber());

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

        cart.setStatus(CartStatus.CONVERTED);
        cartRepository.save(cart);

        return toTransactionResponseDTO(transaction, tickets);
    }

    /**
     * Récupère le détail d'une transaction par son identifiant.
     *
     * @param email         l'adresse email de l'utilisateur authentifié
     * @param transactionId l'identifiant de la transaction à récupérer
     * @return la transaction avec ses billets
     * @throws TransactionNotFoundException si la transaction est introuvable ou n'appartient pas à l'utilisateur
     */
    @Transactional(readOnly = true)
    public TransactionResponseDTO getTransaction(String email, Long transactionId) {
        Transaction transaction = transactionRepository.findByIdAndUser_Email(transactionId, email)
                .orElseThrow(() -> new TransactionNotFoundException("Transaction introuvable"));

        List<Ticket> tickets = new ArrayList<>(transaction.getTickets());
        return toTransactionResponseDTO(transaction, tickets);
    }

    private List<Ticket> generateTickets(Cart cart, Transaction transaction, User user) {
        List<Ticket> tickets = new ArrayList<>();

        for (CartItems item : cart.getCartItems()) {
            int totalTicketCount = item.getQuantity() * item.getOffer().getNumberOfTickets();
            double pricePerTicket = item.getUnitPrice() / item.getOffer().getNumberOfTickets();

            for (int i = 0; i < totalTicketCount; i++) {
                String ticketKey = UUID.randomUUID().toString();

                Ticket ticket = new Ticket();
                ticket.setUserKey(UUID.randomUUID().toString());
                ticket.setTransactionKey(transaction.getTransactionKey());
                ticket.setTicketKey(ticketKey);
                ticket.setCombinedKey(UUID.randomUUID().toString());
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

    private TransactionResponseDTO toTransactionResponseDTO(Transaction transaction, List<Ticket> tickets) {
        List<TicketResponseDTO> ticketDTOs = tickets.stream()
                .map(t -> TicketResponseDTO.builder()
                        .id(t.getId())
                        .ticketKey(t.getTicketKey())
                        .barcode(t.getBarcode())
                        .price(t.getPrice())
                        .event(CartEventSummaryDTO.builder()
                                .id(t.getEvent().getId())
                                .name(t.getEvent().getName())
                                .eventDate(t.getEvent().getEventDate())
                                .location(t.getEvent().getLocation())
                                .city(t.getEvent().getCity())
                                .phase(t.getEvent().getPhase())
                                .build())
                        .offer(CartOfferSummaryDTO.builder()
                                .id(t.getOffer().getId())
                                .name(t.getOffer().getName())
                                .numberOfTickets(t.getOffer().getNumberOfTickets())
                                .price(t.getOffer().getPrice())
                                .build())
                        .build())
                .toList();

        return TransactionResponseDTO.builder()
                .id(transaction.getId())
                .transactionKey(transaction.getTransactionKey())
                .status(transaction.getStatus().name())
                .amount(transaction.getAmount())
                .paymentReference(transaction.getPaymentReference())
                .payedDate(transaction.getPayedDate())
                .tickets(ticketDTOs)
                .build();
    }
}
