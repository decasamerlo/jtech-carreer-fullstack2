package br.com.jtech.tasklist.adapters.output.repositories.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@Entity
@Table(name = "task")
public class TaskEntity extends BaseEntity<UUID> {

    @Column(nullable = false, length = 255)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private Boolean completed;

    @Column(name = "tasklist_id", nullable = false)
    private UUID tasklistId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;
}
