package br.com.jtech.tasklist.adapters.output.repositories.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@Entity
@Table(name = "tasklist", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "name"}, name = "uk_tasklist_user_name"))
public class TasklistEntity extends BaseEntity<UUID> {

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "user_id", nullable = false)
    private UUID userId;
}
