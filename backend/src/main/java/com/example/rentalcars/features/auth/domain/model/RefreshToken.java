package com.example.rentalcars.features.auth.domain.model;

import com.example.rentalcars.core.domain.AggregateRoot;
import com.example.rentalcars.features.user.domain.model.User;
import lombok.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class RefreshToken extends AggregateRoot {
    private String token;
    private User user;
    private Instant expiryDate;
    private boolean revoked;

    @Builder
    public RefreshToken(UUID id, LocalDateTime createdAt, LocalDateTime updatedAt,
                        String token, User user, Instant expiryDate, boolean revoked) {
        super(id, createdAt, updatedAt);
        this.token = token;
        this.user = user;
        this.expiryDate = expiryDate;
        this.revoked = revoked;
    }

    public boolean isExpired() {
        return expiryDate.isBefore(Instant.now());
    }
}