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
            // Set the 'from' address. Note: With services like Brevo or SendGrid,
            // this might be overridden by their settings, but it's good practice to set it.
            message.setFrom("rrspld@gmail.com");
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);

            mailSender.send(message);
            System.out.println("Verification email sent successfully to " + to);
        } catch (Exception e) {
            // In a production app, you would use a logger like SLF4J
            // For now, printing to the console is fine for debugging.
            System.err.println("Error while sending email to " + to + ": " + e.getMessage());
            // It's often better not to throw an exception here, so a failed email
            // doesn't cause the entire registration process to fail.
            // We can add more robust retry logic or queuing later if needed.
        }
    }
}