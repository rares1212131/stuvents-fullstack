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

    // We can read the 'from' email from application.properties to keep it clean
    @Value("${spring.mail.username}")
    private String fromEmail;

    /**
     * Sends a simple plain text email.
     * This method is a wrapper around JavaMailSender to handle exceptions and set a default 'from' address.
     * @param to The recipient's email address.
     * @param subject The subject of the email.
     * @param text The body content of the email.
     */
    public void sendSimpleEmail(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            mailSender.send(message);
            System.out.println("Verification email sent successfully to " + to);
        } catch (Exception e) {
            // Log the error as before...
            System.err.println("Error while sending email to " + to + ": " + e.getMessage());
            // ...but now, re-throw it so the AuthService knows something went wrong.
            throw new RuntimeException("Failed to send email", e);
        }
    }
}