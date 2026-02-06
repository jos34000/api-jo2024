package dev.jos.back.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Length(min =  60, max = 60)
    private String passwordHash;
    @Length(min = 2, max = 30)
    private String firstName;
    @Length(min = 2, max = 30)
    private String lastName;
    @Email
    @Column(unique = true, nullable = false)
    private String email;
    private boolean mfaEnabled;
    @Column(length = 32)
    private String mfaSecret;

    @CreationTimestamp
    private LocalDateTime createdDate;
    @UpdateTimestamp
    private LocalDateTime lastModifiedDate;


    @OneToMany(mappedBy = "user")
    private Set<Transaction> transactions;

    @OneToMany(mappedBy = "user")
    private Set<Cart> carts;

    @OneToMany(mappedBy = "user")
    private Set<Ticket> tickets;
}


