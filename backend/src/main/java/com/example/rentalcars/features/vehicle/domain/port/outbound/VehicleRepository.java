package com.example.rentalcars.features.vehicle.domain.port.outbound;

import com.example.rentalcars.features.vehicle.domain.model.Vehicle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VehicleRepository {
    Vehicle save(Vehicle vehicle);
    Optional<Vehicle> findById(UUID id);
    Optional<Vehicle> findByIdWithLock(UUID id);
    Page<Vehicle> findAllAvailableVehicles(String search, Pageable pageable);
    boolean existsByLicensePlate(String licensePlate);
    void deleteById(UUID id);
    Page<Vehicle> findAvailableVehicles(LocalDateTime start, LocalDateTime end, String search, Pageable pageable);
    Page<Vehicle> findAll(String search, Pageable pageable);
}
