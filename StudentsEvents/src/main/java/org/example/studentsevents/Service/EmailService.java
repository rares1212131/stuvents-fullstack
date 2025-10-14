package org.example.studentsevents.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Unified EmailService that delegates to the correct sender based on environment.
 * - If use.brevo.api=true, it uses the BrevoEmailService (HTTP API for Render).
 * - Otherwise, it uses the standard JavaMailSender (SMTP for local development).
 */
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final BrevoEmailService brevoEmailService; // The new HTTP-based sender

    @Value("${spring.mail.username}")
    private String fromEmail;

    // This is the toggle. It reads the USE_BREvo_API environment variable.
    @Value("${use.brevo.api:false}")
    private boolean useBrevoApi;

    /**
     * Sends an email. The method signature is unchanged, so AuthService does not need to be modified.
     */
    public void sendSimpleEmail(String to, String subject, String text) {
        if (useBrevoApi) {
            // ---- RENDER/PRODUCTION PATH ----
            // Brevo's API expects HTML content, so we wrap the plain text safely.
            String htmlContent = "<pre style=\"font-family:inherit; white-space:pre-wrap;\">" + escapeHtml(text) + "</pre>";
            try {
                brevoEmailService.sendEmailViaBrevo(to, subject, htmlContent);
                System.out.println("Email for <" + to + "> was handed off to the Brevo API successfully.");
                return; // Stop here after successful sending
            } catch (Exception e) {
                System.err.println("CRITICAL: Failed to send email via Brevo API to " + to + ". Error: " + e.getMessage());
                // Re-throw the exception to ensure the transaction in AuthService rolls back.
                throw new RuntimeException("Failed to send email via Brevo API", e);
            }
        }

        // ---- LOCAL DEVELOPMENT FALLBACK PATH ----
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
            // Re-throw the exception to ensure the transaction in AuthService rolls back.
            throw new RuntimeException("Failed to send email via SMTP", e);
        }
    }

    // A small helper to prevent any HTML characters in the plain text from breaking the structure.
    private String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }
}