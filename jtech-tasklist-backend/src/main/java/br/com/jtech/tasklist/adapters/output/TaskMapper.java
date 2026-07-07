package br.com.jtech.tasklist.adapters.output;

import br.com.jtech.tasklist.application.core.domains.Task;
import br.com.jtech.tasklist.adapters.output.repositories.entities.TaskEntity;

import java.util.UUID;

public final class TaskMapper {

    private TaskMapper() {}

    public static TaskEntity toEntity(Task domain) {
        return TaskEntity.builder()
            .id(domain.getId() != null ? UUID.fromString(domain.getId()) : UUID.randomUUID())
            .title(domain.getTitle())
            .description(domain.getDescription())
            .completed(domain.getCompleted() != null ? domain.getCompleted() : false)
            .tasklistId(domain.getTasklistId() != null ? UUID.fromString(domain.getTasklistId()) : null)
            .userId(domain.getUserId() != null ? UUID.fromString(domain.getUserId()) : null)
            .build();
    }

    public static Task toDomain(TaskEntity entity) {
        return Task.builder()
            .id(entity.getId().toString())
            .title(entity.getTitle())
            .description(entity.getDescription())
            .completed(entity.getCompleted())
            .tasklistId(entity.getTasklistId() != null ? entity.getTasklistId().toString() : null)
            .userId(entity.getUserId() != null ? entity.getUserId().toString() : null)
            .build();
    }
}
