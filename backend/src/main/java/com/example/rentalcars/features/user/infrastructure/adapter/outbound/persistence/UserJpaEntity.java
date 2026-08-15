package com.example.rentalcars.features.user.infrastructure.adapter.outbound.persistence;

import com.example.rentalcars.features.user.domain.enums.Role;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.validation.constraints.Email;
import lombok.*;

@Entity
@Table(name = "users")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserJpaEntity {
    @Id
    private UUID id;

    @Column(unique = true, nullable = false)
    @Email(message = "Please provide a valid email address")
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    private Role role;

    private String firstName;

    private String lastName;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    private String resetToken;
    private LocalDateTime resetTokenExpiry;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private CustomerProfileEntity profile;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
