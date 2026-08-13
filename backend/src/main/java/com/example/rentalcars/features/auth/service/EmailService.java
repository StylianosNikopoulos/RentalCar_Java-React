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

    @Value("${MAILJET_API_KEY}")
    private String mailjetApiKey;

    @Value("${MAILJET_SECRET_KEY}")
    private String mailjetSecretKey;

    @Value("${MAIL_USERNAME}")
    private String senderEmail;

    @Async
    public void sendPasswordResetEmail(String toEmail, String token) {
        log.info("Preparing password reset email for recipient: {}", toEmail);
        String resetLink = frontendUrl + "/reset-password?token=" + token;

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

        sendEmailViaMailjet(toEmail, "Reset Your Password - RentalCars", htmlContent);
    }

    @Async
    public void sendPaymentConfirmationEmail(String toEmail, String amount, String receiptUrl) {
        log.info("Preparing payment confirmation email for recipient: {} (Amount: {})", toEmail, amount);

        String receiptButtonHtml = (receiptUrl != null && !receiptUrl.isBlank()) ? """
            <div style="text-align: center; margin: 30px 0;">
                <a href="%s" style="background-color: #ff4d00; color: white; padding: 12px 25px; text-decoration: none; font-weight: bold; border-radius: 3px; display: inline-block;">View Official Stripe Receipt</a>
            </div>
            """.formatted(receiptUrl) : "";

        String htmlContent = """
            <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #eee; border-radius: 5px;">
                <h2 style="color: #ff4d00; text-align: center;">RentalCars</h2>
                <h3 style="color: #333; text-align: center;">Payment Successful!</h3>
                <p>Hello,</p>
                <p>Thank you for your payment. Your reservation has been successfully confirmed!</p>
                <p><strong>Total Amount Paid:</strong> %s</p>
                %s
                <hr style="border: none; border-top: 1px solid #eee; margin-top: 30px;">
                <p style="color: #999; font-size: 12px; text-align: center;">Thank you for choosing RentalCars!</p>
            </div>
            """.formatted(amount, receiptButtonHtml);

        sendEmailViaMailjet(toEmail, "Payment Confirmation - RentalCars", htmlContent);
    }

    private void sendEmailViaMailjet(String toEmail, String subject, String htmlContent) {
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