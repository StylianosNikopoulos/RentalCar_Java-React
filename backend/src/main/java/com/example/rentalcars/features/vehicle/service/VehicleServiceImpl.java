package com.example.rentalcars.features.vehicle.service;

import com.example.rentalcars.core.exception.BusinessException;
import com.example.rentalcars.core.valueobject.DateRange;
import com.example.rentalcars.features.vehicle.domain.model.VehicleImage;
import com.example.rentalcars.features.vehicle.domain.port.inbound.VehicleService;
import com.example.rentalcars.features.vehicle.infrastructure.adapter.inbound.rest.dto.VehicleRequest;
import com.example.rentalcars.features.vehicle.domain.model.LicensePlate;
import com.example.rentalcars.features.vehicle.domain.model.Vehicle;
import com.example.rentalcars.features.vehicle.domain.port.outbound.VehicleRepository;
import com.example.rentalcars.features.vehicle.domain.enums.VehicleStatus;
import com.example.rentalcars.features.vehicle.domain.exception.VehicleNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.util.CollectionUtils;

@Service
@RequiredArgsConstructor
public class VehicleServiceImpl implements VehicleService {

    private final VehicleRepository vehicleRepository;

    @Override
    @Transactional
    public Vehicle createVehicle(VehicleRequest request) {

        if (vehicleRepository.existsByLicensePlate(request.getLicensePlate())){
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

        return vehicleRepository.save(vehicle);
    }

    @Override
    public Page<Vehicle> getAllVehicles(String search, Pageable pageable) {
        return vehicleRepository.findAllVehicles(search, pageable);
    }

    @Override
    public Vehicle getVehicleById(UUID id) {
        return vehicleRepository.findById(id)
                .orElseThrow(()-> new VehicleNotFoundException(id));
    }

    @Override
    @Transactional
    public Vehicle updateVehicle(UUID id, VehicleRequest request) {
        Vehicle existingVehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new VehicleNotFoundException(id));

        List<VehicleImage> updatedImages = mapImageUrlsToDomain(request.getImageUrls(), request.getMainImageUrl(), existingVehicle.getImages());

        Vehicle updatedVehicle = Vehicle.builder()
                .id(existingVehicle.getId())
                .version(existingVehicle.getVersion())
                .createdAt(existingVehicle.getCreatedAt())
                .brand(request.getBrand())
                .model(request.getModel())
                .year(request.getYear())
                .fuelType(request.getFuelType())
                .licensePlate(new LicensePlate(request.getLicensePlate()))
                .status(existingVehicle.getStatus())
                .dailyPrice(request.getDailyPrice())
                .images(updatedImages)
                .build();

        return vehicleRepository.save(updatedVehicle);
    }

    @Override
    @Transactional
    public Vehicle getVehicleByIdWithLock(UUID id) {
        return vehicleRepository.findByIdWithLock(id)
                .orElseThrow(() -> new VehicleNotFoundException(id));
    }

    @Override
    @Transactional
    public void deleteVehicle(UUID id) {
        if (vehicleRepository.findById(id).isEmpty()) {
            throw new VehicleNotFoundException(id);
        }
        vehicleRepository.deleteById(id);
    }

    @Override
    public Page<Vehicle> getAvailableVehicles(LocalDateTime start, LocalDateTime end, String search, Pageable pageable) {
        new DateRange(start, end);
        return vehicleRepository.findAvailableVehicles(start, end, search, pageable);
    }

    @Override
    @Transactional
    public void updateVehicleStatus(UUID vehicleId, VehicleStatus newStatus) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new VehicleNotFoundException(vehicleId));

        Vehicle updatedVehicle = Vehicle.builder()
                .id(vehicle.getId())
                .brand(vehicle.getBrand())
                .model(vehicle.getModel())
                .year(vehicle.getYear())
                .fuelType(vehicle.getFuelType())
                .licensePlate(vehicle.getLicensePlate())
                .dailyPrice(vehicle.getDailyPrice())
                .version(vehicle.getVersion())
                .status(newStatus)
                .build();

        vehicleRepository.save(updatedVehicle);
    }

    private List<VehicleImage> mapImageUrlsToDomain(List<String> urls, String mainUrl, List<VehicleImage> existingImages) {
        if (CollectionUtils.isEmpty(urls)) {
            return !CollectionUtils.isEmpty(existingImages) ? existingImages : List.of();
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
