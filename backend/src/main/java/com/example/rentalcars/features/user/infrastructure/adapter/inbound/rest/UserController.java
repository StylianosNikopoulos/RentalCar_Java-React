package com.example.rentalcars.features.user.infrastructure.adapter.inbound.rest;

import com.example.rentalcars.features.user.domain.port.inbound.UserService;
import com.example.rentalcars.features.user.infrastructure.adapter.inbound.rest.dto.UpdateUserRequest;
import com.example.rentalcars.features.user.infrastructure.adapter.inbound.rest.dto.UserResponse;
import com.example.rentalcars.features.user.infrastructure.adapter.inbound.rest.mapper.UserMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMyProfile(Principal principal) {
        System.out.println("Principal Name: " + principal.getName());
        var user = userService.getInternalUserByEmail(principal.getName());
        return ResponseEntity.ok(userMapper.toResponse(user));
    }

    @PutMapping("/me")
    public ResponseEntity<UserResponse> updateMyProfile(Principal principal, @Valid @RequestBody UpdateUserRequest request) {
        var currentUser = userService.getInternalUserByEmail(principal.getName());
        var updatedUser = userService.update(currentUser.getId(), request);
        return ResponseEntity.ok(userMapper.toResponse(updatedUser));
    }
}