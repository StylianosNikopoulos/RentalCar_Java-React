package com.example.rentalcars.features.user.domain.model;

import com.example.rentalcars.core.domain.AggregateRoot;
import com.example.rentalcars.features.user.domain.enums.Role;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class User extends AggregateRoot {
    private  String email;
    private  String passwordHash;
    private Role role;
    private  String firstName;
    private  String lastName;
    private  CustomerProfile profile;
    private String resetToken;
    private LocalDateTime resetTokenExpiry;

    @Builder
    public User(UUID id, LocalDateTime createdAt, LocalDateTime updatedAt, String email, String passwordHash, Role role,
                String firstName, String lastName, CustomerProfile profile, String resetToken, LocalDateTime resetTokenExpiry) {
        super(id, createdAt, updatedAt);
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.firstName = firstName;
        this.lastName = lastName;
        this.profile = profile;
        this.resetToken = resetToken;
        this.resetTokenExpiry = resetTokenExpiry;
    }

}