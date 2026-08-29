package com.example.rentalcars.features.vehicle.infrastructure.adapter.outbound.persistence;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface VehicleJpaRepository extends JpaRepository<VehicleJpaEntity, UUID> {
    Optional<VehicleJpaEntity> findByLicensePlate(String licensePlate);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT v FROM VehicleJpaEntity v WHERE v.id = :id")
    Optional<VehicleJpaEntity> findByIdWithLock(@Param("id") UUID id);

    @Query("SELECT v FROM VehicleJpaEntity v WHERE v.status = 'AVAILABLE' " +
            "AND (:search IS NULL OR TRIM(:search) = '' " +
            "OR LOWER(v.brand) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(v.model) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "AND v.id NOT IN (" +
            "SELECT r.vehicleId FROM ReservationJpaEntity r " +
            "WHERE r.status <> 'CANCELED' " +
            "AND r.startDate < :end AND r.endDate > :start)")
    Page<VehicleJpaEntity> findAvailableVehicles(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("search") String search,
            Pageable pageable
            );

    Page<VehicleJpaEntity> findAll(Pageable pageable);

    @Query("SELECT v FROM VehicleJpaEntity v WHERE " +
            "(:search IS NULL OR TRIM(:search) = '' " +
            "OR LOWER(v.brand) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(v.model) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<VehicleJpaEntity> findAllWithSearch(@Param("search") String search, Pageable pageable);


    @Query("SELECT v FROM VehicleJpaEntity v WHERE " +
            "v.status = 'AVAILABLE' " +
            "AND (:search IS NULL OR TRIM(:search) = '' " +
            "OR LOWER(v.brand) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(v.model) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<VehicleJpaEntity> findAllAvailableWithSearch(@Param("search") String search, Pageable pageable);
}