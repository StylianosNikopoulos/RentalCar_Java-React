package com.example.rentalcars.features.user.domain.port.inbound;

import com.example.rentalcars.features.user.infrastructure.adapter.inbound.rest.dto.UpdateUserRequest;
import com.example.rentalcars.features.user.infrastructure.adapter.inbound.rest.dto.UserRequest;
import com.example.rentalcars.features.user.domain.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface UserService {
    User getUserById(UUID id);
    User getUserByEmail(String email);
    User getInternalUserByEmail(String email);
    User register(UserRequest request);
    User update(UUID id, UpdateUserRequest request);
    Page<User> getAllUsers(Pageable pageable);
    void delete(UUID id);
    boolean existsByEmail(String email);
    Optional<User> findByResetToken(String token);
    void save(User user);
    User getInternalUserById(UUID id);
}
