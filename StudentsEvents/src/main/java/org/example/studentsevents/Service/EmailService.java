package org.example.studentsevents.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final BrevoEmailService brevoEmailService;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${use.brevo.api:false}")
    private boolean useBrevoApi;

    public void sendSimpleEmail(String to, String subject, String text) {
        if (useBrevoApi) {

            String htmlContent = "<pre style=\"font-family:inherit; white-space:pre-wrap;\">" + escapeHtml(text) + "</pre>";
            try {
                brevoEmailService.sendEmailViaBrevo(to, subject, htmlContent);
                System.out.println("Email for <" + to + "> was handed off to the Brevo API successfully.");
                return;
            } catch (Exception e) {
                System.err.println("CRITICAL: Failed to send email via Brevo API to " + to + ". Error: " + e.getMessage());

                throw new RuntimeException("Failed to send email via Brevo API", e);
            }
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            mailSender.send(message);
            System.out.println("Email for <" + to + "> was sent via SMTP successfully.");
        } catch (Exception e) {
            System.err.println("CRITICAL: Failed to send email via SMTP to " + to + ". Error: " + e.getMessage());

            throw new RuntimeException("Failed to send email via SMTP", e);
        }
    }

    private String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }
}