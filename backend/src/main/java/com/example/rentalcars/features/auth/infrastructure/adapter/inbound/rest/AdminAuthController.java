package com.example.rentalcars.features.auth.infrastructure.adapter.inbound.rest;

import com.example.rentalcars.features.auth.domain.port.inbound.AuthService;
import com.example.rentalcars.features.auth.infrastructure.adapter.inbound.rest.dto.AuthResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/auth")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminAuthController {

    private final AuthService authService;

    @PostMapping("/users/{userId}/refresh")
    public ResponseEntity<AuthResponse> refreshByUserId(@PathVariable UUID userId) {
        return ResponseEntity.ok(authService.refreshTokenByUserId(userId));
    }
}
