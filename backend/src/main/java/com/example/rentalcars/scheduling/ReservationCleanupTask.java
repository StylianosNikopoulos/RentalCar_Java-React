package com.example.rentalcars.scheduling;

import com.example.rentalcars.features.reservation.domain.port.inbound.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class ReservationCleanupTask {
    private final ReservationService reservationService;

    @Scheduled(fixedRate = 300000)
    @Transactional
    public void cancelExpiredReservations(){
        reservationService.findAllExpiredPending();
    }

    @Scheduled(fixedRate = 300000)
    @Transactional
    public void autoStartReservations() {
        reservationService.findByStatusAndPeriodStartBefore();
    }

    @Scheduled(fixedRate = 300000)
    @Transactional
    public void autoEndReservations() {
        reservationService.findByStatusAndPeriodEndBefore();
    }
}
