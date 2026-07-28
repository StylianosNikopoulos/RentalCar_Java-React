package com.example.rentalcars.features.payment.service;

import com.example.rentalcars.core.valueobject.Money;
import com.example.rentalcars.features.payment.domain.exception.InvalidPaymentStatusException;
import com.example.rentalcars.features.payment.domain.exception.StripePaymentException;
import com.example.rentalcars.features.payment.domain.exception.PaymentNotFoundException;
import com.example.rentalcars.features.payment.domain.model.Payment;
import com.example.rentalcars.features.payment.domain.model.PaymentStatus;
import com.example.rentalcars.features.payment.domain.port.inbound.PaymentService;
import com.example.rentalcars.features.payment.domain.port.outbound.PaymentRepository;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import com.example.rentalcars.features.reservation.domain.port.inbound.ReservationService;
import com.stripe.exception.StripeException;
import com.stripe.model.Refund;
import com.stripe.param.RefundCreateParams;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
public class PaymentServiceImpl implements PaymentService {
    private final PaymentRepository paymentRepository;
    private final ReservationService reservationService;

    @Value("${APP_FRONTEND_URL}")
    private String frontendUrl;

    public PaymentServiceImpl(PaymentRepository paymentRepository, @Lazy ReservationService reservationService) {
        this.paymentRepository = paymentRepository;
        this.reservationService = reservationService;
    }

    @Override
    @Transactional
    public String initiatePayment(UUID reservationId, Money amount) {
        try {
            SessionCreateParams params = SessionCreateParams.builder()
                    .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setSuccessUrl(frontendUrl + "/reservations?success=true")
                    .setCancelUrl(frontendUrl + "/reservations?canceled=true")
                    .addLineItem(SessionCreateParams.LineItem.builder()
                            .setQuantity(1L)
                            .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                                    .setCurrency("eur")
                                    .setUnitAmount((long) (amount.amount().doubleValue() * 100))
                                    .setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                            .setName("Rental Car" + reservationId.toString().substring(0, 8))
                                            .build())
                                    .build())
                            .build())
                    .putMetadata("reservationId", reservationId.toString())
                    .build();

            Session session = Session.create(params);

            Payment payment = Payment.builder()
                    .id(UUID.randomUUID())
                    .reservationId(reservationId)
                    .amount(amount)
                    .stripePaymentId(session.getId())
                    .status(PaymentStatus.PENDING)
                    .build();

            paymentRepository.save(payment);

            return session.getUrl();

        } catch (StripeException e) {
            throw new StripePaymentException("Stripe Session Creation Failed", "STRIPE_PAYMENT_FAILED");
        }
    }

    @Override
    @Transactional
    public void processSuccessfulPayment(String stripePaymentId) {
        var payment = paymentRepository.findByStripeId(stripePaymentId)
                .orElseThrow(() -> new PaymentNotFoundException(stripePaymentId));

        payment.setStatus(PaymentStatus.COMPLETED);
        paymentRepository.save(payment);
        reservationService.confirmReservation(payment.getReservationId());
        //TODO: Send Confirmation Email
    }

    @Override
    @Transactional
    public void refundPayment(String stripePaymentId) {
        var payment = paymentRepository.findByStripeId(stripePaymentId)
                .orElseThrow(() -> new PaymentNotFoundException(stripePaymentId));

        if (payment.getStatus() != PaymentStatus.COMPLETED) {
            throw new InvalidPaymentStatusException("Refund can only be processed for COMPLETED payments. Current status: " + payment.getStatus());
        }

        try {
            RefundCreateParams params = RefundCreateParams.builder()
                    .setPaymentIntent(stripePaymentId)
                    .build();

            Refund.create(params);
            payment.setStatus(PaymentStatus.REFUNDED);
            paymentRepository.save(payment);

        } catch (StripeException e) {
            throw new StripePaymentException("Error communication with Stripe for Refund", "STRIPE_REFUND_FAILED");
        }
    }

    @Override
    @Transactional
    public void processFailedPayment(String stripePaymentId) {
        var payment = paymentRepository.findByStripeId(stripePaymentId)
                .orElseThrow(()-> new PaymentNotFoundException(stripePaymentId));

        payment.setStatus(PaymentStatus.FAILED);
        paymentRepository.save(payment);
        reservationService.cancelReservationInternal(payment.getReservationId());
    }

    @Override
    public Payment getPaymentByReservationId(UUID reservationId) {
        return paymentRepository.findByReservationId(reservationId)
                .orElseThrow(() -> new PaymentNotFoundException(reservationId.toString()));
    }
}
