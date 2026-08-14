package com.example.rentalcars.features.payment.domain.model;

import com.example.rentalcars.core.domain.AggregateRoot;
import com.example.rentalcars.core.valueobject.Money;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class Payment extends AggregateRoot {
    private UUID reservationId;
    private Money amount;
    private String stripePaymentId;
    private PaymentStatus status;
    private String receiptUrl;

    @Builder
    public Payment(UUID id, LocalDateTime createdAt, LocalDateTime updatedAt, UUID reservationId, Money amount, String stripePaymentId, PaymentStatus status, String receiptUrl) {
        super(id, createdAt, updatedAt);
        this.reservationId = reservationId;
        this.amount = amount;
        this.stripePaymentId = stripePaymentId;
        this.status = status;
        this.receiptUrl = receiptUrl;
    }
}