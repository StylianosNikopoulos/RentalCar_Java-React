package com.example.rentalcars.features.reservation.service;

import com.example.rentalcars.core.valueobject.Money;
import com.example.rentalcars.features.payment.domain.port.inbound.PaymentService;
import com.example.rentalcars.features.reservation.domain.exception.CarNotAvailableException;
import com.example.rentalcars.features.reservation.domain.exception.InvalidReservationDatesException;
import com.example.rentalcars.features.reservation.domain.exception.ReservationNotFoundException;
import com.example.rentalcars.features.reservation.domain.model.Reservation;
import com.example.rentalcars.features.reservation.domain.enums.ReservationStatus;
import com.example.rentalcars.features.reservation.domain.port.inbound.ReservationService;
import com.example.rentalcars.features.reservation.domain.port.outbound.ReservationRepository;
import com.example.rentalcars.features.user.domain.port.inbound.UserService;
import com.example.rentalcars.features.vehicle.domain.enums.VehicleStatus;
import com.example.rentalcars.features.vehicle.domain.port.inbound.VehicleService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReservationServiceImpl implements ReservationService {

    private final ReservationRepository reservationRepository;
    private final PaymentService paymentService;
    private final VehicleService vehicleService;
    private final UserService userService;

    @Override
    @Transactional
    public Reservation createReservation(Reservation reservation) {
        var auth = SecurityContextHolder.getContext().getAuthentication();

        var user = userService.getUserByEmail(auth.getName());
        reservation.setUserId(user.getId());

        if (reservation.getPeriod() == null) {
            throw new InvalidReservationDatesException("Reservation period is missing.");
        }

        if (reservation.getPeriod().isPast()) {
            throw new InvalidReservationDatesException("Pickup date cannot be in the past.");
        }

        var vehicle = vehicleService.getVehicleByIdWithLock(reservation.getVehicleId());

        if (reservationRepository.existsOverlap(reservation.getVehicleId(), reservation.getPeriod())) {
            throw new CarNotAvailableException();
        }
        reservation.setVehicleName(vehicle.getModel());
        reservation.setVehicleBrand(vehicle.getBrand());
        reservation.calculateTotal(new Money(vehicle.getDailyPrice(),"EUR"));
        reservation.setStatus(ReservationStatus.PENDING);
        reservation.setEmail(user.getEmail());
        return reservationRepository.save(reservation);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Reservation> getMyReservations(Pageable pageable) {   //TODO PERFORMANCE ISSUE
        var auth = SecurityContextHolder.getContext().getAuthentication();
        var user = userService.getUserByEmail(auth.getName());
        return reservationRepository.findByUserId(user.getId(), pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Reservation> getAllReservations(Pageable pageable) {
        Page<Reservation> reservations = reservationRepository.findAll(pageable);

        for (Reservation res : reservations.getContent()) {
            try {
                var user = userService.getUserById(res.getUserId());
                if (user != null) {
                    res.setEmail(user.getEmail());
                }
            } catch (Exception e) {
                res.setEmail("Unknown User");
            }
        }
        return reservations;
    }
    @Override
    @Transactional(readOnly = true)
    public Reservation getReservationById(UUID id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ReservationNotFoundException(id));

        validateOwnership(reservation);
        return reservation;
    }

    @Override
    @Transactional
    public void cancelReservation(UUID reservationId, String userEmail) {
        var reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ReservationNotFoundException(reservationId));

        validateOwnership(reservation);

        if (reservation.getStatus() == ReservationStatus.CONFIRMED) {
            var payment = paymentService.getPaymentByReservationId(reservationId);
            paymentService.refundPayment(payment.getStripePaymentId());
        }

        reservation.cancel();
        reservationRepository.save(reservation);
        vehicleService.updateVehicleStatus(reservation.getVehicleId(), VehicleStatus.AVAILABLE);
    }

    @Override
    @Transactional
    public Reservation confirmReservation(UUID reservationId) {
        var reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ReservationNotFoundException(reservationId));
        reservation.confirm();
        return reservationRepository.save(reservation);
    }

    @Override
    @Transactional
    public void cancelAllActiveReservationsByUserId(UUID userId) {
        List<Reservation> activeReservations = reservationRepository.findAllByUserIdAndStatusIn(
                userId,
                List.of(ReservationStatus.PENDING, ReservationStatus.CONFIRMED)
        );

        for (Reservation res : activeReservations) {
            res.setStatus(ReservationStatus.CANCELED);
            reservationRepository.save(res);
        }
    }

    @Override
    @Transactional
    public void cancelReservationInternal(UUID reservationId) {
        var reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ReservationNotFoundException(reservationId));
        reservation.setStatus(ReservationStatus.CANCELED);
        reservationRepository.save(reservation);
    }

    @Override
    @Transactional
    public Reservation markAsCompleted(UUID reservationId) {
        var reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ReservationNotFoundException(reservationId));

        reservation.markAsCompleted();
        var updatedReservation = reservationRepository.save(reservation);

        vehicleService.updateVehicleStatus(reservation.getVehicleId(), VehicleStatus.AVAILABLE);
        return updatedReservation;
    }

    @Override
    public List<Reservation> getReservationsByVehicleId(UUID vehicleId) {
        return reservationRepository.findByVehicleId(vehicleId).stream()
                .filter(res -> res.getStatus() != ReservationStatus.CANCELED)
                .toList();
    }

    // Helper method for security
    private void validateOwnership(Reservation reservation) {
        var auth = SecurityContextHolder.getContext().getAuthentication();

        if (isAdmin(auth)) {
            return;
        }

        var user = userService.getUserByEmail(auth.getName());

        if (!reservation.getUserId().equals(user.getId())) {
            throw new AccessDeniedException("You don't have permission to do this action");
        }
    }

    // Helper method for admin check
    private boolean isAdmin(Authentication auth) {
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }
}