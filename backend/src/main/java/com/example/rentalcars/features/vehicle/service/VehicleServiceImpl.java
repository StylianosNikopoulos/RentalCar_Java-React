package com.example.rentalcars.features.vehicle.service;

import com.example.rentalcars.core.exception.BusinessException;
import com.example.rentalcars.core.valueobject.DateRange;
import com.example.rentalcars.features.vehicle.domain.enums.VehicleStatus;
import com.example.rentalcars.features.vehicle.domain.exception.VehicleNotFoundException;
import com.example.rentalcars.features.vehicle.domain.model.LicensePlate;
import com.example.rentalcars.features.vehicle.domain.model.Vehicle;
import com.example.rentalcars.features.vehicle.domain.model.VehicleImage;
import com.example.rentalcars.features.vehicle.domain.port.inbound.VehicleService;
import com.example.rentalcars.features.vehicle.domain.port.outbound.VehicleRepository;
import com.example.rentalcars.features.vehicle.infrastructure.adapter.inbound.rest.dto.VehicleRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class VehicleServiceImpl implements VehicleService {

    private final VehicleRepository vehicleRepository;

    @Override
    @Transactional
    public Vehicle createVehicle(VehicleRequest request) {
        if (vehicleRepository.existsByLicensePlate(request.getLicensePlate())) {
            log.warn("Vehicle creation failed: Duplicate license plate {}", request.getLicensePlate());
            throw new BusinessException("Vehicle already exists", "DUPLICATE_LICENSE_PLATE");
        }
        List<VehicleImage> domainImages = mapImageUrlsToDomain(request.getImageUrls(), request.getMainImageUrl(), null);

        var vehicle = Vehicle.builder()
                .id(UUID.randomUUID())
                .brand(request.getBrand())
                .model(request.getModel())
                .year(request.getYear())
                .fuelType(request.getFuelType())
                .licensePlate(new LicensePlate(request.getLicensePlate()))
                .status(VehicleStatus.AVAILABLE)
                .dailyPrice(request.getDailyPrice())
                .images(domainImages)
                .build();

        Vehicle saved = vehicleRepository.save(vehicle);
        log.info("Vehicle created with ID: {} [{}]", saved.getId(), saved.getLicensePlate().value());
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public Vehicle getVehicleById(UUID id) {
        return vehicleRepository.findById(id)
                .orElseThrow(() -> new VehicleNotFoundException(id));
    }

    @Override
    @Transactional
    public Vehicle updateVehicle(UUID id, VehicleRequest request) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new VehicleNotFoundException(id));

        List<VehicleImage> updatedImages = mapImageUrlsToDomain(request.getImageUrls(), request.getMainImageUrl(), vehicle.getImages());

        vehicle.setBrand(request.getBrand());
        vehicle.setModel(request.getModel());
        vehicle.setYear(request.getYear());
        vehicle.setFuelType(request.getFuelType());
        vehicle.setLicensePlate(new LicensePlate(request.getLicensePlate()));
        vehicle.setDailyPrice(request.getDailyPrice());
        vehicle.setImages(updatedImages);

        Vehicle savedVehicle = vehicleRepository.save(vehicle);
        log.info("Vehicle {} updated successfully", id);
        return savedVehicle;
    }

    @Override
    @Transactional
    public Vehicle getVehicleByIdWithLock(UUID id) {
        return vehicleRepository.findByIdWithLock(id)
                .orElseThrow(() -> new VehicleNotFoundException(id));
    }

    @Override
    @Transactional
    public void markVehicleOutOfService(UUID id) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new VehicleNotFoundException(id));

        vehicle.setStatus(VehicleStatus.OUT_OF_SERVICE);
        vehicleRepository.save(vehicle);
        log.info("Vehicle {} status changed to OUT_OF_SERVICE", id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Vehicle> getAvailableVehicles(LocalDateTime start, LocalDateTime end, String search, Pageable pageable) {
        new DateRange(start, end);
        return vehicleRepository.findAvailableVehicles(start, end, search, pageable);
    }

    @Override
    @Transactional
    public void updateVehicleStatus(UUID vehicleId, VehicleStatus newStatus) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new VehicleNotFoundException(vehicleId));

        vehicle.setStatus(newStatus);
        vehicleRepository.save(vehicle);
        log.info("Vehicle {} status updated to {}", vehicleId, newStatus);
    }

    @Override
    @Transactional
    public int updateVehiclesStatusBulk(List<UUID> vehicleIds, VehicleStatus newStatus) {
        if (!CollectionUtils.isEmpty(vehicleIds)) {
            int count = vehicleRepository.updateStatusForIds(vehicleIds, newStatus);
            log.info("Bulk status update: {} vehicle(s) set to {}", count, newStatus);
            return count;
        }
        return 0;
    }

    @Override
    @Transactional
    public Vehicle restoreVehicle(UUID id) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new VehicleNotFoundException(id));

        vehicle.setStatus(VehicleStatus.AVAILABLE);
        Vehicle updatedVehicle = vehicleRepository.save(vehicle);
        log.info("Vehicle {} restored to AVAILABLE", id);
        return updatedVehicle;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Vehicle> getAllVehicles(String search, Pageable pageable) {
        return vehicleRepository.findAll(search, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Vehicle> getAllAvailableVehicles(String search, Pageable pageable) {
        return vehicleRepository.findAllAvailableVehicles(search, pageable);
    }

    private List<VehicleImage> mapImageUrlsToDomain(List<String> urls, String mainUrl, List<VehicleImage> existingImages) {
        if (CollectionUtils.isEmpty(urls)) {
            return !CollectionUtils.isEmpty(existingImages) ? existingImages : new ArrayList<>();
        }

        return urls.stream()
                .map(url -> VehicleImage.builder()
                        .id(UUID.randomUUID())
                        .url(url)
                        .isMain(url.equals(mainUrl))
                        .build())
                .toList();
    }
}