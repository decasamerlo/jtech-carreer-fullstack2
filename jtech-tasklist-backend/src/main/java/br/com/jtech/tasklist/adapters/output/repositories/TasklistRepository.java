package br.com.jtech.tasklist.adapters.output.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import br.com.jtech.tasklist.adapters.output.repositories.entities.TasklistEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TasklistRepository extends JpaRepository<TasklistEntity, UUID> {
    List<TasklistEntity> findAllByUserId(UUID userId);
    Optional<TasklistEntity> findByIdAndUserId(UUID id, UUID userId);

    @Query("SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END FROM TasklistEntity t WHERE t.userId = :userId AND LOWER(t.name) = LOWER(:name)")
    boolean existsByUserIdAndName(@Param("userId") UUID userId, @Param("name") String name);

    @Query("SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END FROM TasklistEntity t WHERE t.userId = :userId AND LOWER(t.name) = LOWER(:name) AND t.id <> :id")
    boolean existsByUserIdAndNameAndIdNot(@Param("userId") UUID userId, @Param("name") String name, @Param("id") UUID id);
}

