package br.com.jtech.tasklist.adapters.output;

import br.com.jtech.tasklist.adapters.output.repositories.UserRepository;
import br.com.jtech.tasklist.adapters.output.repositories.entities.UserEntity;
import br.com.jtech.tasklist.application.core.domains.User;
import br.com.jtech.tasklist.application.ports.output.UserOutputGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UserAdapter implements UserOutputGateway {

    private final UserRepository userRepository;

    @Override
    public User save(User user) {
        UserEntity entity = toEntity(user);
        UserEntity saved = userRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<User> findById(String id) {
        return userRepository.findById(UUID.fromString(id)).map(this::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email).map(this::toDomain);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    private UserEntity toEntity(User domain) {
        UserEntity.UserEntityBuilder<?, ?> builder = UserEntity.builder()
            .name(domain.getName())
            .email(domain.getEmail())
            .password(domain.getPassword())
            .role(domain.getRole());
        if (domain.getId() != null) {
            builder.id(UUID.fromString(domain.getId()));
        } else {
            builder.id(java.util.UUID.randomUUID());
        }
        return builder.build();
    }

    private User toDomain(UserEntity entity) {
        return User.builder()
            .id(entity.getId().toString())
            .name(entity.getName())
            .email(entity.getEmail())
            .password(entity.getPassword())
            .role(entity.getRole())
            .createdAt(entity.getCreatedAt())
            .createdBy(entity.getCreatedBy() != null ? entity.getCreatedBy().toString() : null)
            .updatedAt(entity.getUpdatedAt())
            .updatedBy(entity.getUpdatedBy() != null ? entity.getUpdatedBy().toString() : null)
            .deletedAt(entity.getDeletedAt())
            .deletedBy(entity.getDeletedBy() != null ? entity.getDeletedBy().toString() : null)
            .build();
    }
}
