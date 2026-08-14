package com.example.rentalcars.features.payment.domain.port.inbound;

import com.example.rentalcars.core.valueobject.Money;
import com.example.rentalcars.features.payment.domain.model.Payment;
import java.util.UUID;

public interface PaymentService {
    String initiatePayment(UUID reservationId, Money amount);
    void processSuccessfulPayment(String stripeIntentId, String receiptUrl);
    void refundPayment(String stripePaymentId);
    void processFailedPayment(String stripePaymentId);
    Payment getPaymentByReservationId(UUID reservationId);
}