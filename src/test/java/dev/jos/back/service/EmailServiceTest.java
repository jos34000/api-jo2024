package dev.jos.back.service;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.Emails;
import com.resend.services.emails.model.CreateEmailOptions;
import dev.jos.back.dto.payment.TransactionResponseDTO;
import dev.jos.back.exceptions.email.EmailNotSentException;
import dev.jos.back.support.TestFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock SpringTemplateEngine templateEngine;
    @Mock Resend resend;
    @Mock Emails emails;

    private EmailService emailService;

    @BeforeEach
    void setUp() {
        emailService = new EmailService(templateEngine, resend);
        ReflectionTestUtils.setField(emailService, "from", "noreply@jo2024.dev");
        when(templateEngine.process(any(String.class), any(Context.class)))
                .thenReturn("<html>rendered</html>");
        when(resend.emails()).thenReturn(emails);
    }

    // ── sendPasswordResetEmail ────────────────────────────────────────────────

    @Test
    void sendPasswordResetEmail_usesFrenchSubject_whenLocaleIsFr() throws ResendException {
        emailService.sendPasswordResetEmail(
                "alice@example.com", "Alice", "https://jo.dev/reset?t=abc", "fr");

        ArgumentCaptor<CreateEmailOptions> sentCaptor = ArgumentCaptor.forClass(CreateEmailOptions.class);
        verify(emails).send(sentCaptor.capture());
        CreateEmailOptions sent = sentCaptor.getValue();
        assertThat(sent.getSubject()).isEqualTo("Réinitialisation de mot de passe");
        assertThat(sent.getTo()).containsExactly("alice@example.com");
        assertThat(sent.getFrom()).isEqualTo("noreply@jo2024.dev");

        ArgumentCaptor<Context> ctxCaptor = ArgumentCaptor.forClass(Context.class);
        verify(templateEngine).process(eq("email/forget-password"), ctxCaptor.capture());
        Context ctx = ctxCaptor.getValue();
        assertThat(ctx.getVariable("name")).isEqualTo("Alice");
        assertThat(ctx.getVariable("resetLink")).isEqualTo("https://jo.dev/reset?t=abc");
    }

    @Test
    void sendPasswordResetEmail_usesEnglishSubject_whenLocaleIsEn() throws ResendException {
        emailService.sendPasswordResetEmail("alice@example.com", "Alice", "link", "en");

        ArgumentCaptor<CreateEmailOptions> captor = ArgumentCaptor.forClass(CreateEmailOptions.class);
        verify(emails).send(captor.capture());
        assertThat(captor.getValue().getSubject()).isEqualTo("Password reset");
    }

    @Test
    void sendPasswordResetEmail_fallsBackToFrench_whenLocaleUnknown() throws ResendException {
        emailService.sendPasswordResetEmail("alice@example.com", "Alice", "link", "zz");

        ArgumentCaptor<CreateEmailOptions> captor = ArgumentCaptor.forClass(CreateEmailOptions.class);
        verify(emails).send(captor.capture());
        assertThat(captor.getValue().getSubject()).isEqualTo("Réinitialisation de mot de passe");
    }

    @Test
    void sendPasswordResetEmail_throwsEmailNotSentException_whenResendFails() throws ResendException {
        when(emails.send(any(CreateEmailOptions.class)))
                .thenThrow(new ResendException("network down"));

        assertThatThrownBy(() -> emailService.sendPasswordResetEmail(
                "alice@example.com", "Alice", "link", "fr"))
                .isInstanceOf(EmailNotSentException.class);
    }

    // ── sendTwoFactorEmail ────────────────────────────────────────────────────

    @Test
    void sendTwoFactorEmail_usesLocalizedSubject_andPassesCodeAndExpiration() throws ResendException {
        emailService.sendTwoFactorEmail("alice@example.com", "Alice", "123456", 5, "en");

        ArgumentCaptor<CreateEmailOptions> sentCaptor = ArgumentCaptor.forClass(CreateEmailOptions.class);
        verify(emails).send(sentCaptor.capture());
        assertThat(sentCaptor.getValue().getSubject()).isEqualTo("Your JO account - Security code");

        ArgumentCaptor<Context> ctxCaptor = ArgumentCaptor.forClass(Context.class);
        verify(templateEngine).process(eq("email/otp-code"), ctxCaptor.capture());
        Context ctx = ctxCaptor.getValue();
        assertThat(ctx.getVariable("name")).isEqualTo("Alice");
        assertThat(ctx.getVariable("code")).isEqualTo("123456");
        assertThat(ctx.getVariable("expirationMinutes")).isEqualTo(5);
    }

    @Test
    void sendTwoFactorEmail_usesGermanSubject_whenLocaleIsDe() throws ResendException {
        emailService.sendTwoFactorEmail("alice@example.com", "Alice", "987654", 10, "de");

        ArgumentCaptor<CreateEmailOptions> captor = ArgumentCaptor.forClass(CreateEmailOptions.class);
        verify(emails).send(captor.capture());
        assertThat(captor.getValue().getSubject()).isEqualTo("Dein JO-Konto - Sicherheitscode");
    }

    // ── sendTicketsEmail ──────────────────────────────────────────────────────

    @Test
    void sendTicketsEmail_singleArgOverload_defaultsToFrenchLocale() throws ResendException {
        TransactionResponseDTO tx = TestFixtures.transaction(
                List.of(TestFixtures.ticket("T1", 50.0)));

        emailService.sendTicketsEmail("alice@example.com", "Alice", tx, new byte[]{1, 2, 3});

        ArgumentCaptor<CreateEmailOptions> captor = ArgumentCaptor.forClass(CreateEmailOptions.class);
        verify(emails).send(captor.capture());
        assertThat(captor.getValue().getSubject())
                .isEqualTo("Vos billets Paris 2024 — REF-A1B2C3D4");
    }

    @Test
    void sendTicketsEmail_attachesPdfAsBase64_withReferenceFilename() throws ResendException {
        TransactionResponseDTO tx = TestFixtures.transaction(
                List.of(TestFixtures.ticket("T1", 50.0)));

        emailService.sendTicketsEmail("alice@example.com", "Alice", tx, new byte[]{1, 2, 3}, "fr");

        ArgumentCaptor<CreateEmailOptions> captor = ArgumentCaptor.forClass(CreateEmailOptions.class);
        verify(emails).send(captor.capture());
        CreateEmailOptions sent = captor.getValue();
        assertThat(sent.getAttachments()).hasSize(1);
        assertThat(sent.getAttachments().getFirst().getFileName())
                .isEqualTo("billets-jo2024-REF-A1B2C3D4.pdf");
        // Base64("\u0001\u0002\u0003") == "AQID"
        assertThat(sent.getAttachments().getFirst().getContent()).isEqualTo("AQID");
    }

    @Test
    void sendTicketsEmail_passesTicketCountAndFormattedAmount_toTemplate() {
        TransactionResponseDTO tx = TestFixtures.transaction(List.of(
                TestFixtures.ticket("T1", 50.0),
                TestFixtures.ticket("T2", 50.0)));

        emailService.sendTicketsEmail("alice@example.com", "Alice", tx, new byte[]{}, "en");

        ArgumentCaptor<Context> ctxCaptor = ArgumentCaptor.forClass(Context.class);
        verify(templateEngine).process(eq("email/ticket-confirmation"), ctxCaptor.capture());
        Context ctx = ctxCaptor.getValue();
        assertThat(ctx.getVariable("ticketCount")).isEqualTo(2);
        assertThat(ctx.getVariable("paymentReference")).isEqualTo("REF-A1B2C3D4");
        // English locale uses "." as decimal separator
        assertThat((String) ctx.getVariable("amount")).startsWith("50.00");
    }

    @Test
    void sendTicketsEmail_displaysDashForDate_whenPayedDateIsNull() {
        TransactionResponseDTO tx = TransactionResponseDTO.builder()
                .id(1L)
                .paymentReference("REF-NULL-DATE")
                .amount(java.math.BigDecimal.valueOf(0))
                .payedDate(null)
                .tickets(List.of())
                .build();

        emailService.sendTicketsEmail("alice@example.com", "Alice", tx, new byte[]{}, "fr");

        ArgumentCaptor<Context> ctxCaptor = ArgumentCaptor.forClass(Context.class);
        verify(templateEngine).process(eq("email/ticket-confirmation"), ctxCaptor.capture());
        assertThat(ctxCaptor.getValue().getVariable("payedDate")).isEqualTo("—");
    }

    @Test
    void sendTicketsEmail_throwsEmailNotSentException_whenResendFails() throws ResendException {
        TransactionResponseDTO tx = TestFixtures.transaction(
                List.of(TestFixtures.ticket("T1", 50.0)));
        when(emails.send(any(CreateEmailOptions.class)))
                .thenThrow(new ResendException("rate limited"));

        assertThatThrownBy(() -> emailService.sendTicketsEmail(
                "alice@example.com", "Alice", tx, new byte[]{1}, "fr"))
                .isInstanceOf(EmailNotSentException.class);
    }
}
