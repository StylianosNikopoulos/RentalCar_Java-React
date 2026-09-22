package com.example.rentalcars.features.payment.domain.port.outbound;

public interface PaymentEmailPort {
    void sendPaymentConfirmationEmail(String toEmail, String amount, String receiptUrl);
}