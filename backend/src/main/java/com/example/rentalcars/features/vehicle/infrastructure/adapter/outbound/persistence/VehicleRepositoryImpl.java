package com.example.rentalcars.features.vehicle.infrastructure.adapter.outbound.persistence;

import com.example.rentalcars.features.vehicle.domain.model.Vehicle;
import com.example.rentalcars.features.vehicle.domain.port.outbound.VehicleRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
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
    public Page<Vehicle> findAll(String search, Pageable pageable) {
        return jpaRepository.findAllWithSearch(search, pageable)
                .map(vehiclePersistenceMapper::toDomain);
    }

    @Override
    public Page<Vehicle> findAllAvailableVehicles(String search, Pageable pageable) {
        return jpaRepository.findAllAvailableWithSearch(search, pageable)
                .map(vehiclePersistenceMapper::toDomain);
    }

    @Override
    public boolean existsByLicensePlate(String licensePlate) {
        return jpaRepository.findByLicensePlate(licensePlate).isPresent();
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public Page<Vehicle> findAvailableVehicles(LocalDateTime start, LocalDateTime end, String search, Pageable pageable) {
        Page<VehicleJpaEntity> entities = jpaRepository.findAvailableVehicles(start, end, search, pageable);
        return entities.map(vehiclePersistenceMapper::toDomain);
    }
}
