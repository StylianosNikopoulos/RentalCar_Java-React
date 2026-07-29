package com.example.rentalcars.features.payment.infrastructure.adapter.inbound.rest;

import com.example.rentalcars.features.payment.domain.port.inbound.PaymentService;
import com.stripe.model.Event;
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

    @Value("${STRIPE_WEBHOOK_SECRET}")
    private String endpointSecret;
    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<String> handleStripeEvent(@RequestBody String payload, @RequestHeader("Stripe-Signature") String sigHeader) {
        Event event;
        try {
            event = Webhook.constructEvent(payload, sigHeader, endpointSecret);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Webhook error");
        }

        var dataObjectDeserializer = event.getDataObjectDeserializer();

        if ("checkout.session.completed".equals(event.getType())) {
            Session session = getSessionFromEvent(dataObjectDeserializer);
            if (session != null) {
                try {
                    paymentService.processSuccessfulPayment(session.getId());
                } catch (Exception e) {
                    System.err.println("Error processing success: " + e.getMessage());
                }
            }
        }
        else if ("checkout.session.expired".equals(event.getType())) {
            Session session = getSessionFromEvent(dataObjectDeserializer);
            if (session != null) {
                paymentService.processFailedPayment(session.getId());
            }
        }
        else if ("payment_intent.payment_failed".equals(event.getType())) {
            try {
                if (dataObjectDeserializer.getObject().isPresent()) {
                    var pi = (com.stripe.model.PaymentIntent) dataObjectDeserializer.getObject().get();
                }
            } catch (Exception e) {
                System.err.println("Could not deserialize PaymentIntent");
            }
        }

        return ResponseEntity.ok().build();
    }

    private Session getSessionFromEvent(com.stripe.model.EventDataObjectDeserializer deserializer) {
        try {
            if (deserializer.getObject().isPresent()) {
                return (Session) deserializer.getObject().get();
            } else {
                return (Session) deserializer.deserializeUnsafe();
            }
        } catch (com.stripe.exception.EventDataObjectDeserializationException e) {
            System.err.println("Deserialization failed: " + e.getMessage());
            return null;
        }
    }
}