package com.example.rentalcars.features.payment.infrastructure.adapter.inbound.rest;

import com.example.rentalcars.features.payment.domain.port.inbound.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/payments")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminPaymentController {

    private final PaymentService paymentService;

    @PostMapping("/{paymentId}/refund")
    public ResponseEntity<String> handleManualRefund(@PathVariable String paymentId) {
        paymentService.refundPayment(paymentId);
        return ResponseEntity.ok("Refund processed successfully");
    }
}
