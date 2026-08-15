package com.example.rentalcars.features.reservation.infrastructure.adapter.outbound.persistence;

import com.example.rentalcars.core.valueobject.DateRange;
import com.example.rentalcars.features.reservation.domain.model.Reservation;
import com.example.rentalcars.features.reservation.domain.enums.ReservationStatus;
import com.example.rentalcars.features.reservation.domain.port.outbound.ReservationRepository;
import com.example.rentalcars.features.user.infrastructure.adapter.outbound.persistence.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ReservationRepositoryImpl implements ReservationRepository {

    private final ReservationJpaRepository jpaRepository;
    private final ReservationPersistenceMapper reservationMapper;
    private final UserJpaRepository userJpaRepository;

    @Override
    public Reservation save(Reservation reservation) {
        var entity = reservationMapper.toEntity(reservation);
        if (reservation.getUserId() != null) {
            entity.setUser(userJpaRepository.getReferenceById(reservation.getUserId()));
        }
        var saved = jpaRepository.save(entity);
        return reservationMapper.toDomain(saved);
    }

    @Override
    public Optional<Reservation> findById(UUID id) {
        return jpaRepository.findById(id).map(reservationMapper::toDomain);
    }

    @Override
    public Page<Reservation> findByUserId(UUID userId, Pageable pageable) {
        return jpaRepository.findByUserId(userId, pageable)
                .map(reservationMapper::toDomain);
    }

    @Override
    public Page<Reservation> findAll(Pageable pageable) {
        return jpaRepository.findAll(pageable).map(reservationMapper::toDomain);
    }

    @Override
    public List<Reservation> findByVehicleId(UUID vehicleId) {
        return jpaRepository.findByVehicleId(vehicleId).stream()
                .map(reservationMapper::toDomain)
                .toList();
    }

    @Override
    public boolean existsOverlap(UUID vehicleId, DateRange period) {
        return jpaRepository.existsOverlappingReservations(vehicleId, period.start(), period.end());
    }

    @Override
    public List<Reservation> findAllByUserIdAndStatusIn(UUID userId, List<ReservationStatus> statuses) {
        return jpaRepository.findAllByUserIdAndStatusIn(userId, statuses)
                .stream()
                .map(reservationMapper::toDomain)
                .toList();
    }

    @Override
    public List<Reservation> findAllExpiredPending(LocalDateTime threshold) {
        return jpaRepository.findAllByStatusAndCreatedAtBefore(ReservationStatus.PENDING, threshold)
                .stream()
                .map(reservationMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Reservation> findByStatusAndPeriodStartBefore(ReservationStatus status, LocalDateTime now) {
        return jpaRepository.findAllByStatusAndPeriodStartBefore(status, now)
                .stream()
                .map(reservationMapper::toDomain)
                .toList();
    }

    @Override
    public List<Reservation> findByStatusAndPeriodEndBefore(ReservationStatus status, LocalDateTime now) {
        return jpaRepository.findAllByStatusAndPeriodEndBefore(status, now)
                .stream()
                .map(reservationMapper::toDomain)
                .toList();
    }
}
