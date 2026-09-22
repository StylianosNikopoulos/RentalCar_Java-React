package com.example.rentalcars.features.vehicle.domain.port.outbound;

import com.example.rentalcars.features.vehicle.domain.enums.FuelType;
import com.example.rentalcars.features.vehicle.domain.enums.VehicleStatus;
import com.example.rentalcars.features.vehicle.domain.model.Vehicle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VehicleRepository {
    Vehicle save(Vehicle vehicle);
    Optional<Vehicle> findById(UUID id);
    Optional<Vehicle> findByIdWithLock(UUID id);
    boolean existsByLicensePlate(String licensePlate);
    int updateStatusForIds(List<UUID> vehicleIds, VehicleStatus status);
    Page<Vehicle> findAvailableVehicles(LocalDateTime start, LocalDateTime end, String search, String brand, FuelType fuelType, BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);
    Page<Vehicle> findAllAvailableVehicles(String search, String brand, FuelType fuelType, BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);
    Page<Vehicle> findAllAdminVehicles(String search, String brand, FuelType fuelType, VehicleStatus status, BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);
}
