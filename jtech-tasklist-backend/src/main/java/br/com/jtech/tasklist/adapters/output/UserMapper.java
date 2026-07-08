package br.com.jtech.tasklist.adapters.output;

import br.com.jtech.tasklist.adapters.output.repositories.entities.UserEntity;
import br.com.jtech.tasklist.application.core.domains.User;

import java.util.UUID;

public final class UserMapper {

    private UserMapper() {}

    public static UserEntity toEntity(User domain) {
        return UserEntity.builder()
            .id(domain.getId() != null ? UUID.fromString(domain.getId()) : UUID.randomUUID())
            .name(domain.getName())
            .email(domain.getEmail())
            .password(domain.getPassword())
            .role(domain.getRole())
            .build();
    }

    public static User toDomain(UserEntity entity) {
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
