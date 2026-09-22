package com.example.rentalcars.features.vehicle.infrastructure.adapter.inbound.rest;

import com.example.rentalcars.features.vehicle.domain.enums.FuelType;
import com.example.rentalcars.features.vehicle.infrastructure.adapter.inbound.rest.dto.VehicleResponse;
import com.example.rentalcars.features.vehicle.domain.port.inbound.VehicleService;
import com.example.rentalcars.features.vehicle.infrastructure.adapter.inbound.rest.mapper.VehicleRestMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/vehicles")
@RequiredArgsConstructor
public class VehicleController {

    private final VehicleService vehicleService;
    private final VehicleRestMapper vehicleRestMapper;

    @GetMapping
    public ResponseEntity<Page<VehicleResponse>> getAllAvailableVehicles(@RequestParam(required = false) String search,
                                                                         @RequestParam(required = false) String brand,
                                                                         @RequestParam(required = false) FuelType fuelType,
                                                                         @RequestParam(required = false) BigDecimal minPrice,
                                                                         @RequestParam(required = false) BigDecimal maxPrice,
                                                                         @PageableDefault(size = 9, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {

        var vehicles = vehicleService.getAllAvailableVehicles(search, brand, fuelType, minPrice, maxPrice, pageable)
                .map(vehicleRestMapper::toResponse);
        return ResponseEntity.ok(vehicles);
    }

    @GetMapping("/{id}")
    public ResponseEntity<VehicleResponse> getVehicleById(@PathVariable UUID id){
        return ResponseEntity.ok(vehicleRestMapper.toResponse(vehicleService.getVehicleById(id)));
    }

    @GetMapping("/available")
    public ResponseEntity<Page<VehicleResponse>> getAvailableVehicles(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
                                                                      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
                                                                      @RequestParam(required = false) String search,
                                                                      @RequestParam(required = false) String brand,
                                                                      @RequestParam(required = false) FuelType fuelType,
                                                                      @RequestParam(required = false) BigDecimal minPrice,
                                                                      @RequestParam(required = false) BigDecimal maxPrice,
                                                                      @PageableDefault(size = 9, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<VehicleResponse> available = vehicleService.getAvailableVehicles(start, end, search, brand, fuelType, minPrice, maxPrice, pageable)
                .map(vehicleRestMapper::toResponse);

        return ResponseEntity.ok(available);
    }

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("UP");
    }
}
