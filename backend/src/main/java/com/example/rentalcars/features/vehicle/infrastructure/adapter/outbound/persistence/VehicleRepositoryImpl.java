package com.example.rentalcars.features.vehicle.infrastructure.adapter.outbound.persistence;

import com.example.rentalcars.features.vehicle.domain.enums.FuelType;
import com.example.rentalcars.features.vehicle.domain.enums.VehicleStatus;
import com.example.rentalcars.features.vehicle.domain.model.Vehicle;
import com.example.rentalcars.features.vehicle.domain.port.outbound.VehicleRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@AllArgsConstructor
public class VehicleRepositoryImpl implements VehicleRepository {

    private final VehicleJpaRepository jpaRepository;
    private final VehiclePersistenceMapper vehiclePersistenceMapper;

    @Override
    public Vehicle save(Vehicle vehicle) {
        var entity = vehiclePersistenceMapper.toEntity(vehicle);
        var saved = jpaRepository.save(entity);
        return vehiclePersistenceMapper.toDomain(saved);
    }

    @Override
    public Optional<Vehicle> findById(UUID id) {
        return jpaRepository.findById(id).map(vehiclePersistenceMapper::toDomain);
    }

    @Override
    public Optional<Vehicle> findByIdWithLock(UUID id) {
        return jpaRepository.findByIdWithLock(id).map(vehiclePersistenceMapper::toDomain);
    }

    @Override
    public boolean existsByLicensePlate(String licensePlate) {
        return jpaRepository.findByLicensePlate(licensePlate).isPresent();
    }

    @Override
    public int updateStatusForIds(List<UUID> vehicleIds, VehicleStatus status) {
        return jpaRepository.updateStatusForIds(vehicleIds, status);
    }

    @Override
    public Page<Vehicle> findAvailableVehicles(LocalDateTime start, LocalDateTime end, String search, String brand, FuelType fuelType, BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable) {
        return jpaRepository.findAvailableVehiclesWithFilters(start, end, search, brand, fuelType, minPrice, maxPrice, pageable)
                .map(vehiclePersistenceMapper::toDomain);
    }

    @Override
    public Page<Vehicle> findAllAvailableVehicles(String search, String brand, FuelType fuelType, BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable) {
        return jpaRepository.findAllAvailableWithFilters(search, brand, fuelType, minPrice, maxPrice, pageable)
                .map(vehiclePersistenceMapper::toDomain);
    }

    @Override
    public Page<Vehicle> findAllAdminVehicles(String search, String brand, FuelType fuelType, VehicleStatus status, BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable) {
        return jpaRepository.findAllAdminWithFilters(search, brand, fuelType, status, minPrice, maxPrice, pageable)
                .map(vehiclePersistenceMapper::toDomain);
    }
}