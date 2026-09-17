package com.example.rentalcars.features.user.infrastructure.adapter.outbound.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface UserJpaRepository extends JpaRepository<UserJpaEntity, UUID> {
    Optional<UserJpaEntity> findByEmail(String email);
    Optional<UserJpaEntity> findByResetToken(String resetToken);

    @Query(
            value = "SELECT u FROM UserJpaEntity u " +
                    "ORDER BY CASE WHEN u.firstName = 'Deleted' THEN 2 ELSE 1 END ASC, u.createdAt DESC",
            countQuery = "SELECT COUNT(u) FROM UserJpaEntity u"
    )
    Page<UserJpaEntity> findAllOrderedByActiveStatus(Pageable pageable);
}
