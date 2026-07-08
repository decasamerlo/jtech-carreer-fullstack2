/*
*  @(#)TasklistRepository.java
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

