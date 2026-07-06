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

import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;
import br.com.jtech.tasklist.adapters.output.repositories.entities.TasklistEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
* class TasklistRepository 
* 
* @author angelo.vicente
*/
@Repository
public interface TasklistRepository extends JpaRepository<TasklistEntity, UUID> {
    List<TasklistEntity> findAllByUserId(UUID userId);
    Optional<TasklistEntity> findByIdAndUserId(UUID id, UUID userId);
    @Modifying
    @Transactional
    void deleteByIdAndUserId(UUID id, UUID userId);
}