package dev.jos.back.model;

import dev.jos.back.util.PaymentMethod;
import dev.jos.back.util.TransactionStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "transactions")
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String transactionKey;

    @Enumerated(EnumType.STRING)
    private TransactionStatus status;

    private double amount;

    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    @Column(unique = true, nullable = false)
    private String paymentReference;

    @CreationTimestamp
    private LocalDateTime createdDate;
    private LocalDateTime payedDate;
    private LocalDateTime cancelledDate;



    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
