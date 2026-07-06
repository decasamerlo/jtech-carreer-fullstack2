package br.com.jtech.tasklist.application.core.domains;

import br.com.jtech.tasklist.adapters.input.protocols.TasklistRequest;
import br.com.jtech.tasklist.adapters.output.repositories.entities.TasklistEntity;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@SuperBuilder
@ToString(callSuper = true)
@NoArgsConstructor
public class Tasklist extends BaseDomain<String> {

    private String name;
    private String userId;

    public static List<Tasklist> of(List<TasklistEntity> entities) {
        return entities.stream().map(Tasklist::of).toList();
    }

    public TasklistEntity toEntity() {
        return TasklistEntity.builder()
            .id(getId() != null ? UUID.fromString(getId()) : UUID.randomUUID())
            .name(getName())
            .userId(userId != null ? UUID.fromString(userId) : null)
            .build();
    }

    public static Tasklist of(TasklistEntity entity) {
        return Tasklist.builder()
            .id(entity.getId().toString())
            .name(entity.getName())
            .userId(entity.getUserId() != null ? entity.getUserId().toString() : null)
            .build();
    }

    public static Tasklist of(TasklistRequest request) {
        return Tasklist.builder()
            .id(request.getId())
            .name(request.getName())
            .build();
    }
}
