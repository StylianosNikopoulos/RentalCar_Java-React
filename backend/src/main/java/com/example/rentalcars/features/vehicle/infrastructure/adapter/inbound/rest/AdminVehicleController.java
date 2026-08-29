package com.example.rentalcars.features.vehicle.infrastructure.adapter.inbound.rest;

import com.example.rentalcars.features.vehicle.domain.port.inbound.VehicleService;
import com.example.rentalcars.features.vehicle.infrastructure.adapter.inbound.rest.dto.VehicleRequest;
import com.example.rentalcars.features.vehicle.infrastructure.adapter.inbound.rest.dto.VehicleResponse;
import com.example.rentalcars.features.vehicle.infrastructure.adapter.inbound.rest.mapper.VehicleRestMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/vehicles")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminVehicleController {

    private final VehicleService vehicleService;
    private final VehicleRestMapper vehicleRestMapper;

    @GetMapping
    public ResponseEntity<Page<VehicleResponse>> getAllVehicles(
            @RequestParam(required = false) String search,
            @PageableDefault(size = 9, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        var vehicles = vehicleService.getAllVehicles(search, pageable).map(vehicleRestMapper::toResponse);
        return ResponseEntity.ok(vehicles);
    }

    @PostMapping
    public ResponseEntity<VehicleResponse> createVehicle(@Valid @RequestBody VehicleRequest request){
        var vehicle = vehicleService.createVehicle(request);
        return new ResponseEntity<>(vehicleRestMapper.toResponse(vehicle), HttpStatus.CREATED);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<VehicleResponse> updateVehicle(@PathVariable UUID id, @Valid @RequestBody VehicleRequest request) {
        var updatedVehicle = vehicleService.updateVehicle(id,request);
        return ResponseEntity.ok(updatedVehicle != null ? vehicleRestMapper.toResponse(updatedVehicle) : null);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVehicle(@PathVariable UUID id) {
        vehicleService.deleteVehicle(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/restore")
    public ResponseEntity<VehicleResponse> restoreVehicle(@PathVariable UUID id) {
        var restoredVehicle = vehicleService.restoreVehicle(id);
        return ResponseEntity.ok(vehicleRestMapper.toResponse(restoredVehicle));
    }
}