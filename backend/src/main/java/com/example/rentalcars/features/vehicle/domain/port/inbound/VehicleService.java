package com.example.rentalcars.features.vehicle.domain.port.inbound;

import com.example.rentalcars.features.vehicle.domain.enums.VehicleStatus;
import com.example.rentalcars.features.vehicle.infrastructure.adapter.inbound.rest.dto.VehicleRequest;
import com.example.rentalcars.features.vehicle.domain.model.Vehicle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.UUID;

public interface VehicleService {
    Vehicle createVehicle(VehicleRequest request);
    Page<Vehicle> getAllVehicles(String search, Pageable pageable);
    Vehicle getVehicleById(UUID id);
    Vehicle updateVehicle(UUID id, VehicleRequest request);
    Vehicle getVehicleByIdWithLock(UUID id);
    void deleteVehicle(UUID id);
    Page<Vehicle> getAvailableVehicles(LocalDateTime start, LocalDateTime end, String search, Pageable pageable);
    void updateVehicleStatus(UUID vehicleId, VehicleStatus newStatus);
}
