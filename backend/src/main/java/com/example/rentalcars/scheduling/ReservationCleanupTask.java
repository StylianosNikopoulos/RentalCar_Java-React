package com.example.rentalcars.scheduling;

import com.example.rentalcars.features.reservation.domain.model.Reservation;
import com.example.rentalcars.features.reservation.domain.enums.ReservationStatus;
import com.example.rentalcars.features.reservation.domain.port.outbound.ReservationRepository;
import com.example.rentalcars.features.vehicle.domain.enums.VehicleStatus;
import com.example.rentalcars.features.vehicle.domain.port.inbound.VehicleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReservationCleanupTask {
    private final ReservationRepository reservationRepository;
    private final VehicleService vehicleService;

    @Scheduled(fixedRate = 300000)
    @Transactional
    public void cancelExpiredReservations(){
        LocalDateTime threshold = LocalDateTime.now().minusHours(1);
        List<Reservation> expired = reservationRepository.findAllExpiredPending(threshold);

        for (Reservation res : expired){
            res.setStatus(ReservationStatus.CANCELED);
            reservationRepository.save(res);
            log.info("Reservation {} canceled due to payment timeout", res.getId());
        }
    }

    @Scheduled(fixedRate = 300000)
    @Transactional
    public void autoStartReservations() {
        LocalDateTime now = LocalDateTime.now();
        List<Reservation> toStart = reservationRepository
                .findByStatusAndPeriodStartBefore(ReservationStatus.CONFIRMED, now);

        for (Reservation res : toStart) {
            res.setStatus(ReservationStatus.ACTIVE);
            reservationRepository.save(res);
            vehicleService.updateVehicleStatus(res.getVehicleId(), VehicleStatus.RENTED);
            log.info("Reservation {} started automatically", res.getId());
        }
    }

    @Scheduled(fixedRate = 300000)
    @Transactional
    public void autoEndReservations() {
        LocalDateTime now = LocalDateTime.now();
        List<Reservation> toComplete = reservationRepository.findByStatusAndPeriodEndBefore(
                ReservationStatus.ACTIVE, now);

        for (Reservation res : toComplete) {
            res.setStatus(ReservationStatus.COMPLETED);
            reservationRepository.save(res);
            vehicleService.updateVehicleStatus(res.getVehicleId(), VehicleStatus.AVAILABLE);
            log.info("Reservation {} completed automatically", res.getId());
        }
    }
}
