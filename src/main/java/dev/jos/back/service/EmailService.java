package dev.jos.back.service;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.Attachment;
import com.resend.services.emails.model.CreateEmailOptions;
import dev.jos.back.dto.payment.TransactionResponseDTO;
import dev.jos.back.exceptions.email.EmailNotSentException;
import dev.jos.back.util.enums.SupportedLocale;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private static final EnumMap<SupportedLocale, DateTimeFormatter> DATE_FORMATTERS =
            new EnumMap<>(Map.of(
                    SupportedLocale.FR, DateTimeFormatter.ofPattern("d MMMM yyyy 'à' HH:mm", Locale.FRENCH),
                    SupportedLocale.EN, DateTimeFormatter.ofPattern("MMMM d, yyyy 'at' HH:mm", Locale.ENGLISH),
                    SupportedLocale.DE, DateTimeFormatter.ofPattern("d. MMMM yyyy 'um' HH:mm", Locale.GERMAN),
                    SupportedLocale.ES, DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy 'a las' HH:mm", Locale.of("es"))
            ));

    private static final EnumMap<SupportedLocale, String> RESET_SUBJECTS =
            new EnumMap<>(Map.of(
                    SupportedLocale.FR, "Réinitialisation de mot de passe",
                    SupportedLocale.EN, "Password reset",
                    SupportedLocale.DE, "Passwort zurücksetzen",
                    SupportedLocale.ES, "Restablecimiento de contraseña"
            ));

    private static final EnumMap<SupportedLocale, String> OTP_SUBJECTS =
            new EnumMap<>(Map.of(
                    SupportedLocale.FR, "Votre compte JO - Code de sécurité",
                    SupportedLocale.EN, "Your JO account - Security code",
                    SupportedLocale.DE, "Dein JO-Konto - Sicherheitscode",
                    SupportedLocale.ES, "Tu cuenta JO - Código de seguridad"
            ));

    private static final EnumMap<SupportedLocale, String> TICKET_SUBJECTS =
            new EnumMap<>(Map.of(
                    SupportedLocale.FR, "Vos billets Paris 2024 — ",
                    SupportedLocale.EN, "Your Paris 2024 tickets — ",
                    SupportedLocale.DE, "Ihre Paris 2024-Tickets — ",
                    SupportedLocale.ES, "Sus entradas para París 2024 — "
            ));

    private final SpringTemplateEngine templateEngine;
    private final Resend resend;

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
            resend.emails().send(params);
        } catch (ResendException e) {
            log.error("Échec envoi email avec PJ à {}: {}", to, e.getMessage(), e);
            throw new EmailNotSentException("Échec de l'envoi d'email");
        }
    }

    public void sendPasswordResetEmail(String to, String name, String resetLink, String locale) {
        SupportedLocale sl = SupportedLocale.from(locale);
        Context context = new Context(sl.javaLocale);
        context.setVariable("name", name);
        context.setVariable("resetLink", resetLink);
        sendEmail(to, RESET_SUBJECTS.get(sl), templateEngine.process("email/forget-password", context));
    }

    public void sendTwoFactorEmail(String email, String name, String code, int expirationMinutes, String locale) {
        SupportedLocale sl = SupportedLocale.from(locale);
        Context context = new Context(sl.javaLocale);
        context.setVariable("name", name);
        context.setVariable("code", code);
        context.setVariable("expirationMinutes", expirationMinutes);
        sendEmail(email, OTP_SUBJECTS.get(sl), templateEngine.process("email/otp-code", context));
    }

    public void sendTicketsEmail(String to, String name, TransactionResponseDTO transaction, byte[] pdfBytes) {
        sendTicketsEmail(to, name, transaction, pdfBytes, "fr");
    }
    
    public void sendTicketsEmail(String to, String name, TransactionResponseDTO transaction,
                                 byte[] pdfBytes, String locale) {
        SupportedLocale sl = SupportedLocale.from(locale);
        DateTimeFormatter dateFmt = DATE_FORMATTERS.get(sl);
        String formattedDate = transaction.payedDate() != null
                ? transaction.payedDate().format(dateFmt)
                : "—";
        String formattedAmount = String.format(sl.javaLocale, "%.2f\u00a0€", transaction.amount());

        Context context = new Context(sl.javaLocale);
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

        String subjectPrefix = TICKET_SUBJECTS.get(sl);
        sendEmailWithAttachment(to, subjectPrefix + transaction.paymentReference(), html, attachment);
    }
}
