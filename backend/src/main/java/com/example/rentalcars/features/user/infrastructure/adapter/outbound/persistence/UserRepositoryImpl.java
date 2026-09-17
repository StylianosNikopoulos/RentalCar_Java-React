package com.example.rentalcars.features.user.infrastructure.adapter.outbound.persistence;

import com.example.rentalcars.features.user.domain.model.User;
import com.example.rentalcars.features.user.domain.port.outbound.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@AllArgsConstructor
public class UserRepositoryImpl implements UserRepository {

    private final UserJpaRepository jpaRepository;
    private final UserPersistenceMapper userPersistenceMapper;

    @Override
    public User save(User user) {
        var entity = userPersistenceMapper.toEntity(user);

        if (entity.getProfile() != null) {
            entity.getProfile().setUser(entity);
        }

        var saved = jpaRepository.save(entity);
        return userPersistenceMapper.toDomain(saved);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return jpaRepository.findByEmail(email).map(userPersistenceMapper::toDomain);
    }

    @Override
    public Optional<User> findById(UUID id) {
        return jpaRepository.findById(id).map(userPersistenceMapper::toDomain);
    }

    @Override
    public Page<User> findAll(Pageable pageable) {
        return jpaRepository.findAllOrderedByActiveStatus(pageable)
                .map(userPersistenceMapper::toDomain);
    }

    @Override
    public Optional<User> findByResetToken(String token) {
        return jpaRepository.findByResetToken(token)
                .map(userPersistenceMapper::toDomain);
    }
}
