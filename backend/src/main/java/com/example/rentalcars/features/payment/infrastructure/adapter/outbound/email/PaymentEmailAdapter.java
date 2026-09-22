package com.example.rentalcars.features.payment.infrastructure.adapter.outbound.email;

import com.example.rentalcars.features.payment.domain.port.outbound.PaymentEmailPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEmailAdapter implements PaymentEmailPort {

    private final RestTemplate restTemplate;

    @Value("${MAILJET_API_KEY}")
    private String mailjetApiKey;

    @Value("${MAILJET_SECRET_KEY}")
    private String mailjetSecretKey;

    @Value("${MAIL_USERNAME}")
    private String senderEmail;

    @Override
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
                log.info("Payment email successfully sent via Mailjet API to recipient: {} [Subject: '{}']", toEmail, subject);
            } else {
                log.error("Failed to send payment email via Mailjet to recipient: {}. Status: {}, Body: {}", toEmail, response.getStatusCode(), response.getBody());
            }
        } catch (Exception e) {
            log.error("Error sending payment email via Mailjet API to recipient: {} [Subject: '{}']", toEmail, subject, e);
        }
    }
}