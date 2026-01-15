package org.example.studentsevents.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class BrevoEmailService {

    private final RestTemplate restTemplate;

    @Value("${BREVO_API_KEY:}")
    private String apiKey;

    @Value("${brevo.sender.email:no-reply@yourdomain.com}")
    private String senderEmail;

    @Value("${brevo.sender.name:STUvents}")
    private String senderName;

    public void sendEmailViaBrevo(String to, String subject, String htmlContent) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("BREVO_API_KEY environment variable is not set.");
        }

        String url = "https://api.brevo.com/v3/smtp/email";

        Map<String, Object> payload = Map.of(
                "sender", Map.of("name", senderName, "email", senderEmail),
                "to", List.of(Map.of("email", to)),
                "subject", subject,
                "htmlContent", htmlContent
        );

        HttpHeaders headers = new HttpHeaders();
        headers.set("api-key", apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

        ResponseEntity<String> resp = restTemplate.exchange(url, HttpMethod.POST, request, String.class);

        if (!resp.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Brevo API returned a non-successful status: " + resp.getStatusCode() + " with body: " + resp.getBody());
        }
    }
}