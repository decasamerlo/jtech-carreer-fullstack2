/*
 *  @(#)TaskRepository.java
 *
 *  Copyright (c) J-Tech Solucoes em Informatica.
 *  All Rights Reserved.
 *
 *  This software is the confidential and proprietary information of J-Tech.
 *  ("Confidential Information"). You shall not disclose such Confidential
 *  Information and shall use it only in accordance with the terms of the
 *  license agreement you entered into with J-Tech.
 *
 */
package br.com.jtech.tasklist.adapters.output.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import br.com.jtech.tasklist.adapters.output.repositories.entities.TaskEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TaskRepository extends JpaRepository<TaskEntity, UUID> {
    List<TaskEntity> findByTasklistIdAndUserId(UUID tasklistId, UUID userId);
    Optional<TaskEntity> findByIdAndUserId(UUID id, UUID userId);

    @Query("SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END FROM TaskEntity t WHERE t.tasklistId = :tasklistId AND LOWER(t.title) = LOWER(:title) AND t.deletedAt IS NULL")
    boolean existsByTasklistIdAndTitle(@Param("tasklistId") UUID tasklistId, @Param("title") String title);

    @Query("SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END FROM TaskEntity t WHERE t.tasklistId = :tasklistId AND LOWER(t.title) = LOWER(:title) AND t.id <> :id AND t.deletedAt IS NULL")
    boolean existsByTasklistIdAndTitleAndIdNot(@Param("tasklistId") UUID tasklistId, @Param("title") String title, @Param("id") UUID id);
}

