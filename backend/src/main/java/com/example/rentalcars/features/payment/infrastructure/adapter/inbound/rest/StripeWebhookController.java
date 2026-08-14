package com.example.rentalcars.features.payment.infrastructure.adapter.inbound.rest;

import com.example.rentalcars.features.payment.domain.port.inbound.PaymentService;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments/webhook")
@RequiredArgsConstructor
public class StripeWebhookController {

    private final PaymentService paymentService;

    @Value("${stripe.webhook.secret}")
    private String endpointSecret;

    @PostMapping
    public ResponseEntity<String> handleStripeEvent(@RequestBody String payload, @RequestHeader("Stripe-Signature") String sigHeader) {

        Event event;
        try {
            event = Webhook.constructEvent(payload, sigHeader, endpointSecret);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Webhook error: " + e.getMessage());
        }

        EventDataObjectDeserializer deserializer = event.getDataObjectDeserializer();

        if ("checkout.session.completed".equals(event.getType())) {
            Session session = getSessionFromEvent(deserializer);
            if (session != null) {
                paymentService.processSuccessfulPayment(session.getId(), session.getPaymentIntent());
            }
        }
        else if ("checkout.session.expired".equals(event.getType())) {
            Session session = getSessionFromEvent(deserializer);
            if (session != null) {
                paymentService.processFailedPayment(session.getId());
            }
        }

        return ResponseEntity.ok().build();
    }

    private Session getSessionFromEvent(EventDataObjectDeserializer deserializer) {
        if (deserializer.getObject().isPresent() && deserializer.getObject().get() instanceof Session session) {
            return session;
        }

        try {
            if (deserializer.deserializeUnsafe() instanceof Session session) {
                return session;
            }
        } catch (Exception ignored) {
        }

        return null;
    }
}