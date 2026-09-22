package com.example.rentalcars.shared.email;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class MailjetEmailSender {

    private final RestTemplate restTemplate;

    @Value("${MAILJET_API_KEY}")
    private String mailjetApiKey;

    @Value("${MAILJET_SECRET_KEY}")
    private String mailjetSecretKey;

    @Value("${MAIL_USERNAME}")
    private String senderEmail;

    public void sendEmail(String toEmail, String subject, String htmlContent) {
        String mailjetUrl = "https://api.mailjet.com/v3.1/send";

        Map<String, Object> body = Map.of(
                "Messages", List.of(
                        Map.of(
                                "From", Map.of("Email", senderEmail, "Name", "RentalCars"),
                                "To", List.of(Map.of("Email", toEmail)),
                                "Subject", subject,
                                "HTMLPart", htmlContent
                        )
                )
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBasicAuth(mailjetApiKey, mailjetSecretKey);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(mailjetUrl, request, String.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Email successfully sent via Mailjet API to recipient: {} [Subject: '{}']", toEmail, subject);
            } else {
                log.error("Failed to send email via Mailjet to recipient: {}. Status: {}, Body: {}", toEmail, response.getStatusCode(), response.getBody());
            }
        } catch (Exception e) {
            log.error("Error sending email via Mailjet API to recipient: {} [Subject: '{}']", toEmail, subject, e);
        }
    }
}