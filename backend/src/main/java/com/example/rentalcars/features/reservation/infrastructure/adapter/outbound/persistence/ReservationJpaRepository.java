package com.example.rentalcars.features.reservation.infrastructure.adapter.outbound.persistence;

import com.example.rentalcars.features.reservation.domain.enums.ReservationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface ReservationJpaRepository extends JpaRepository<ReservationJpaEntity, UUID> {
    @Query(
            value = "SELECT r FROM ReservationJpaEntity r WHERE r.user.id = :userId " +
                    "ORDER BY CASE r.status " +
                    "  WHEN 'ACTIVE' THEN 1 " +
                    "  WHEN 'PENDING' THEN 2 " +
                    "  WHEN 'CONFIRMED' THEN 3 " +
                    "  WHEN 'COMPLETED' THEN 4 " +
                    "  WHEN 'CANCELED' THEN 5 " +
                    "  ELSE 6 END ASC, r.createdAt DESC",
            countQuery = "SELECT COUNT(r) FROM ReservationJpaEntity r WHERE r.user.id = :userId")
    Page<ReservationJpaEntity> findByUserId(@Param("userId") UUID userId, Pageable pageable);

    @Query(
            value = "SELECT r FROM ReservationJpaEntity r " +
                    "ORDER BY CASE r.status " +
                    "  WHEN 'ACTIVE' THEN 1 " +
                    "  WHEN 'PENDING' THEN 2 " +
                    "  WHEN 'CONFIRMED' THEN 3 " +
                    "  WHEN 'COMPLETED' THEN 4 " +
                    "  WHEN 'CANCELED' THEN 5 " +
                    "  ELSE 6 END ASC, r.createdAt DESC",
            countQuery = "SELECT COUNT(r) FROM ReservationJpaEntity r")
    Page<ReservationJpaEntity> findAll(Pageable pageable);

    @Query("SELECT COUNT(r) > 0 FROM ReservationJpaEntity r " +
            "WHERE r.vehicleId = :vehicleId " +
            "AND r.status != 'CANCELED' " +
            "AND (:startDate <= r.endDate AND :endDate >= r.startDate)")
    boolean existsOverlappingReservations(
            @Param("vehicleId") UUID vehicleId,
            @Param(("startDate")) LocalDateTime startDate,
            @Param("endDate")LocalDateTime endDate
            );

    List<ReservationJpaEntity> findByVehicleId(UUID vehicleId);
    List<ReservationJpaEntity> findAllByUserIdAndStatusIn(UUID userId, List<ReservationStatus> statuses);
    List<ReservationJpaEntity> findAllByStatusAndCreatedAtBefore(ReservationStatus status, LocalDateTime dateTime);

    @Query("SELECT r FROM ReservationJpaEntity r WHERE r.status = :status AND r.startDate < :dateTime")
    List<ReservationJpaEntity> findAllByStatusAndPeriodStartBefore(
            @Param("status") ReservationStatus status,
            @Param("dateTime") LocalDateTime dateTime
    );

    @Query("SELECT r FROM ReservationJpaEntity r WHERE r.status = :status AND r.endDate < :dateTime")
    List<ReservationJpaEntity> findAllByStatusAndPeriodEndBefore(
            @Param("status") ReservationStatus status,
            @Param("dateTime") LocalDateTime dateTime
    );
}
