package dev.jos.back.service;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import dev.jos.back.exceptions.email.EmailNotSentException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final SpringTemplateEngine templateEngine;

    @Value("${resend.api-key}")
    private String apiKey;

    public void sendPasswordResetEmail(String to, String name, String resetLink) {
        Context context = new Context();
        context.setVariable("name", name);
        context.setVariable("resetLink", resetLink);
        String html = templateEngine.process("email/forget-password", context);

        Resend resend = new Resend(apiKey);
        CreateEmailOptions params = CreateEmailOptions.builder()
                .from("jo@jossainson.dev")
                .to(List.of(to))
                .subject("Réinitialisation de mot de passe")
                .html(html)
                .build();
        try {
            resend.emails().send(params);
        } catch (ResendException e) {
            e.printStackTrace();
            throw new EmailNotSentException("Échec de l'envoi d'email");
        }
    }
}
