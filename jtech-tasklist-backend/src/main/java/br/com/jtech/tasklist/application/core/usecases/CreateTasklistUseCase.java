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

