package com.example.rentalcars.features.vehicle.domain.port.inbound;

import com.example.rentalcars.features.vehicle.domain.enums.FuelType;
import com.example.rentalcars.features.vehicle.domain.enums.VehicleStatus;
import com.example.rentalcars.features.vehicle.infrastructure.adapter.inbound.rest.dto.VehicleRequest;
import com.example.rentalcars.features.vehicle.domain.model.Vehicle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface VehicleService {
    Vehicle createVehicle(VehicleRequest request);
    Vehicle getVehicleById(UUID id);
    Vehicle updateVehicle(UUID id, VehicleRequest request);
    Vehicle getVehicleByIdWithLock(UUID id);
    void markVehicleOutOfService(UUID id);
    void updateVehicleStatus(UUID vehicleId, VehicleStatus newStatus);
    Vehicle restoreVehicle(UUID id);
    int updateVehiclesStatusBulk(List<UUID> vehicleIds, VehicleStatus newStatus);
    Page<Vehicle> getAllVehiclesAdmin(String search, String brand, FuelType fuelType, VehicleStatus status, BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);
    Page<Vehicle> getAllAvailableVehicles(String search, String brand, FuelType fuelType, BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);
    Page<Vehicle> getAvailableVehicles(LocalDateTime start, LocalDateTime end, String search, String brand, FuelType fuelType, BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);
}
