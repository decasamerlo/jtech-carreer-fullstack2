package br.com.jtech.tasklist.adapters.output;

import br.com.jtech.tasklist.application.core.domains.Tasklist;
import br.com.jtech.tasklist.adapters.output.repositories.entities.TasklistEntity;

import java.util.UUID;

public final class TasklistMapper {

    private TasklistMapper() {}

    public static TasklistEntity toEntity(Tasklist domain) {
        return TasklistEntity.builder()
            .id(domain.getId() != null ? UUID.fromString(domain.getId()) : UUID.randomUUID())
            .name(domain.getName())
            .userId(domain.getUserId() != null ? UUID.fromString(domain.getUserId()) : null)
            .build();
    }

    public static Tasklist toDomain(TasklistEntity entity) {
        return Tasklist.builder()
            .id(entity.getId().toString())
            .name(entity.getName())
            .userId(entity.getUserId() != null ? entity.getUserId().toString() : null)
            .build();
    }
}
