package com.example.rentalcars.features.vehicle.infrastructure.adapter.inbound.rest.dto;

import com.example.rentalcars.features.vehicle.domain.enums.FuelType;
import com.example.rentalcars.features.vehicle.domain.enums.VehicleStatus;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class VehicleResponse {
    private UUID id;
    private String brand;
    private String model;
    private int year;
    @Enumerated(EnumType.STRING)
    private FuelType fuelType;
    private String licensePlate;
    @Enumerated(EnumType.STRING)
    private VehicleStatus status;
    private BigDecimal dailyPrice;
    private List<VehicleImageResponse> images;
}