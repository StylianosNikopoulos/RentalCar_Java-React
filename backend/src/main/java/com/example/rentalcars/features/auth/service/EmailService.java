package com.example.rentalcars.features.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${APP_FRONTEND_URL}")
    private String frontendUrl;

    @Value("${BREVO_API_KEY}")
    private String brevoApiKey;

    @Value("${MAIL_USERNAME}")
    private String senderEmail;

    @Async
    public void sendPasswordResetEmail(String toEmail, String token) {
        String resetLink = frontendUrl + "/reset-password?token=" + token;
        String brevoUrl = "https://api.brevo.com/v3/smtp/email";

        String htmlContent = """
            <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #eee; border-radius: 5px;">
                <h2 style="color: #ff4d00; text-align: center;">RentalCar</h2>
                <p>Hello,</p>
                <p>You requested to reset your password. Please click the button below to set a new password. This link will expire in 1 hour.</p>
                <div style="text-align: center; margin: 30px 0;">
                    <a href="%s" style="background-color: #ff4d00; color: white; padding: 12px 25px; text-decoration: none; font-weight: bold; border-radius: 3px; display: inline-block;">Reset Password</a>
                </div>
                <p>If the button doesn't work, copy and paste this URL into your browser:</p>
                <p style="color: #666; font-size: 12px; word-break: break-all;">%s</p>
                <hr style="border: none; border-top: 1px solid #eee; margin-top: 30px;">
                <p style="color: #999; font-size: 12px; text-align: center;">If you did not request this, you can safely ignore this email.</p>
            </div>
            """.formatted(resetLink, resetLink);

        Map<String, Object> body = Map.of(
                "sender", Map.of("name", "RentalCars", "email", senderEmail),
                "to", List.of(Map.of("email", toEmail)),
                "subject", "Reset Your Password - RentalCars",
                "htmlContent", htmlContent
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("api-key", brevoApiKey);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(brevoUrl, request, String.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Password reset email successfully sent via Brevo API to {}", toEmail);
            } else {
                log.error("Failed to send email via Brevo. Status: {}, Body: {}", response.getStatusCode(), response.getBody());
            }
        } catch (Exception e) {
            log.error("Error sending password reset email via Brevo API to {}", toEmail, e);
        }
    }
}