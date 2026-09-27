package com.example.rentalcars.features.payment.infrastructure.adapter.inbound.rest.dto;

import com.example.rentalcars.core.valueobject.Money;
import com.example.rentalcars.features.payment.domain.enums.PaymentStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class PaymentResponse {
    private UUID id;
    private UUID reservationId;
    private String userEmail;
    private Money amount;
    private String stripePaymentId;
    private PaymentStatus status;
    private String receiptUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}