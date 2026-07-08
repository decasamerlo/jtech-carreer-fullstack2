/*
*  @(#)TasklistUseCase.java
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
package br.com.jtech.tasklist.application.core.usecases;


import br.com.jtech.tasklist.application.core.domains.Tasklist;
import br.com.jtech.tasklist.application.ports.input.CreateTasklistInputGateway;
import br.com.jtech.tasklist.application.ports.output.CreateTasklistOutputGateway;
import br.com.jtech.tasklist.application.ports.output.GetTasklistsOutputGateway;

import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
public class CreateTasklistUseCase implements CreateTasklistInputGateway {

    private final CreateTasklistOutputGateway createTasklistOutputGateway;
    private final GetTasklistsOutputGateway getTasklistsOutputGateway;

    public Tasklist create(Tasklist tasklist) {
        String trimmedName = tasklist.getName().trim();
        tasklist.setName(trimmedName);
        boolean duplicate = getTasklistsOutputGateway.existsByUserIdAndName(
                UUID.fromString(tasklist.getUserId()),
                trimmedName);
        if (duplicate) {
            throw new IllegalArgumentException("A list with this name already exists");
        }
        return createTasklistOutputGateway.create(tasklist);
    }
}

