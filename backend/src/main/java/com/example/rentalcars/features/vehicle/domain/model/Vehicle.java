package com.example.rentalcars.features.vehicle.domain.model;

import com.example.rentalcars.core.domain.AggregateRoot;
import com.example.rentalcars.features.vehicle.domain.enums.FuelType;
import com.example.rentalcars.features.vehicle.domain.enums.VehicleStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class Vehicle extends AggregateRoot {
    private String brand;
    private String model;
    private int year;
    private FuelType fuelType;
    private LicensePlate licensePlate;
    private VehicleStatus status;
    private BigDecimal dailyPrice;
    private Long version;
    private List<VehicleImage> images;

    @Builder
    public Vehicle(UUID id, LocalDateTime createdAt, LocalDateTime updatedAt, String brand, String model, int year, FuelType fuelType,
                   LicensePlate licensePlate, VehicleStatus status,
                   BigDecimal dailyPrice, Long version,
                   List<VehicleImage> images) {
        super(id, createdAt, updatedAt);
        this.brand = brand;
        this.model = model;
        this.year = year;
        this.fuelType = fuelType;
        this.licensePlate = licensePlate;
        this.status = status;
        this.dailyPrice = dailyPrice;
        this.version = version;
        this.images = images;
    }
}