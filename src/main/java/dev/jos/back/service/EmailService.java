package dev.jos.back.service;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.Attachment;
import com.resend.services.emails.model.CreateEmailOptions;
import dev.jos.back.dto.payment.TransactionResponseDTO;
import dev.jos.back.exceptions.email.EmailNotSentException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;
import java.util.Locale;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("d MMMM yyyy 'à' HH:mm", Locale.FRENCH);

    private final SpringTemplateEngine templateEngine;
    @Value("${resend.api-key}")
    private String apiKey;
    @Value("${resend.from}")
    private String from;

    private void sendEmail(String to, String subject, String html) {
        CreateEmailOptions params = CreateEmailOptions.builder()
                .from(from)
                .to(List.of(to))
                .subject(subject)
                .html(html)
                .build();
        try {
            Resend resend = new Resend(apiKey);
            resend.emails().send(params);
        } catch (ResendException e) {
            log.error("Échec envoi email à {}: {}", to, e.getMessage(), e);
            throw new EmailNotSentException("Échec de l'envoi d'email");
        }
    }

    private void sendEmailWithAttachment(String to, String subject, String html, Attachment attachment) {
        CreateEmailOptions params = CreateEmailOptions.builder()
                .from(from)
                .to(List.of(to))
                .subject(subject)
                .html(html)
                .attachments(List.of(attachment))
                .build();
        try {
            Resend resend = new Resend(apiKey);
            resend.emails().send(params);
        } catch (ResendException e) {
            log.error("Échec envoi email avec PJ à {}: {}", to, e.getMessage(), e);
            throw new EmailNotSentException("Échec de l'envoi d'email");
        }
    }

    public void sendPasswordResetEmail(String to, String name, String resetLink) {
        Context context = new Context();
        context.setVariable("name", name);
        context.setVariable("resetLink", resetLink);
        sendEmail(to, "Réinitialisation de mot de passe",
                templateEngine.process("email/forget-password", context));
    }

    public void sendTwoFactorEmail(String email, String name, String code, int expirationMinutes) {
        Context context = new Context();
        context.setVariable("name", name);
        context.setVariable("code", code);
        context.setVariable("expirationMinutes", expirationMinutes);
        sendEmail(email, "Votre compte JO - Code de sécurité",
                templateEngine.process("email/otp-code", context));
    }

    public void sendTicketsEmail(String to, String name, TransactionResponseDTO transaction, byte[] pdfBytes) {
        String formattedDate = transaction.payedDate() != null
                ? transaction.payedDate().format(DATE_FMT)
                : "—";
        String formattedAmount = String.format(Locale.FRENCH, "%.2f\u00a0€", transaction.amount());

        Context context = new Context();
        context.setVariable("name", name);
        context.setVariable("paymentReference", transaction.paymentReference());
        context.setVariable("payedDate", formattedDate);
        context.setVariable("ticketCount", transaction.tickets().size());
        context.setVariable("amount", formattedAmount);

        String html = templateEngine.process("email/ticket-confirmation", context);

        String filename = "billets-jo2024-" + transaction.paymentReference() + ".pdf";
        Attachment attachment = Attachment.builder()
                .fileName(filename)
                .content(Base64.getEncoder().encodeToString(pdfBytes))
                .build();

        sendEmailWithAttachment(to, "Vos billets Paris 2024 — " + transaction.paymentReference(), html, attachment);
    }
}
