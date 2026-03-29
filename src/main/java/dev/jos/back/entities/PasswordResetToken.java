package dev.jos.back.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class PasswordResetToken {
    @Id
    @GeneratedValue
    private Long id;

    private String hashedToken;
    private LocalDateTime expiry;
    private boolean used = false;

    @ManyToOne
    private User user;

    public PasswordResetToken(User user, String hashedToken, LocalDateTime expiry) {
        this.user = user;
        this.hashedToken = hashedToken;
        this.expiry = expiry;
        this.used = false;
    }
}
