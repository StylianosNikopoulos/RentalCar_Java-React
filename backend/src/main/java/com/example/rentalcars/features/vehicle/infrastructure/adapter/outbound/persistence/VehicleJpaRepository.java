package com.example.rentalcars.features.vehicle.infrastructure.adapter.outbound.persistence;

import com.example.rentalcars.features.vehicle.domain.enums.FuelType;
import com.example.rentalcars.features.vehicle.domain.enums.VehicleStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VehicleJpaRepository extends JpaRepository<VehicleJpaEntity, UUID> {
    Optional<VehicleJpaEntity> findByLicensePlate(String licensePlate);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT v FROM VehicleJpaEntity v WHERE v.id = :id")
    Optional<VehicleJpaEntity> findByIdWithLock(@Param("id") UUID id);

    @Query("SELECT v FROM VehicleJpaEntity v WHERE v.status = 'AVAILABLE' " +
            "AND (:search IS NULL " +
            "OR LOWER(v.brand) LIKE :search " +
            "OR LOWER(v.model) LIKE :search) " +
            "AND (:brand IS NULL OR LOWER(v.brand) = :brand) " +
            "AND (:fuelType IS NULL OR v.fuelType = :fuelType) " +
            "AND (:minPrice IS NULL OR v.dailyPrice >= :minPrice) " +
            "AND (:maxPrice IS NULL OR v.dailyPrice <= :maxPrice) " +
            "AND NOT EXISTS (" +
            "SELECT 1 FROM ReservationJpaEntity r " +
            "WHERE r.vehicleId = v.id " +
            "AND r.status <> 'CANCELED' " +
            "AND r.startDate < :end AND r.endDate > :start)")
    Page<VehicleJpaEntity> findAvailableVehiclesWithFilters(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("search") String search,
            @Param("brand") String brand,
            @Param("fuelType") FuelType fuelType,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            Pageable pageable
    );

    @Query("SELECT v FROM VehicleJpaEntity v WHERE v.status = 'AVAILABLE' " +
            "AND (:search IS NULL " +
            "OR LOWER(v.brand) LIKE :search " +
            "OR LOWER(v.model) LIKE :search) " +
            "AND (:brand IS NULL OR LOWER(v.brand) = :brand) " +
            "AND (:fuelType IS NULL OR v.fuelType = :fuelType) " +
            "AND (:minPrice IS NULL OR v.dailyPrice >= :minPrice) " +
            "AND (:maxPrice IS NULL OR v.dailyPrice <= :maxPrice)")
    Page<VehicleJpaEntity> findAllAvailableWithFilters(
            @Param("search") String search,
            @Param("brand") String brand,
            @Param("fuelType") FuelType fuelType,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            Pageable pageable
    );

    @Query("SELECT v FROM VehicleJpaEntity v WHERE " +
            "(:status IS NULL OR v.status = :status) " +
            "AND (:search IS NULL " +
            "OR LOWER(v.brand) LIKE :search " +
            "OR LOWER(v.model) LIKE :search " +
            "OR LOWER(CONCAT(v.brand, ' ', v.model)) LIKE :search) " +
            "AND (:brand IS NULL OR LOWER(v.brand) LIKE :brand) " +
            "AND (:fuelType IS NULL OR v.fuelType = :fuelType) " +
            "AND (:minPrice IS NULL OR v.dailyPrice >= :minPrice) " +
            "AND (:maxPrice IS NULL OR v.dailyPrice <= :maxPrice)")
    Page<VehicleJpaEntity> findAllAdminWithFilters(
            @Param("search") String search,
            @Param("brand") String brand,
            @Param("fuelType") FuelType fuelType,
            @Param("status") VehicleStatus status,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            Pageable pageable
    );

    @Modifying(clearAutomatically = true)
    @Query("UPDATE VehicleJpaEntity v SET v.status = :status WHERE v.id IN :ids")
    int updateStatusForIds(@Param("ids") List<UUID> ids, @Param("status") VehicleStatus status);
}