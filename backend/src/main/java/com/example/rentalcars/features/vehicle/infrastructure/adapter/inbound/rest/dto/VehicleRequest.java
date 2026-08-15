package com.example.rentalcars.features.vehicle.infrastructure.adapter.inbound.rest.dto;

import com.example.rentalcars.features.vehicle.domain.enums.FuelType;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class VehicleRequest {
    @NotBlank(message = "Brand is required")
    private String brand;

    @NotBlank(message = "Model is required")
    private String model;

    @Min(value = 1900, message = "Year must be valid")
    private int year;

    @NotNull(message = "Fuel type is required")
    private FuelType fuelType;

    @NotBlank(message = "License plate is required")
    private String licensePlate;

    @NotNull(message = "Daily price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    private BigDecimal dailyPrice;

    @NotEmpty(message = "At least one image is required")
    private List<String> imageUrls;

    @NotBlank(message = "Main image URL is required")
    private String mainImageUrl;
}