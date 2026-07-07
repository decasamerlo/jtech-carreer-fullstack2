package br.com.jtech.tasklist.application.core.usecases;

import br.com.jtech.tasklist.application.core.domains.Task;
import br.com.jtech.tasklist.application.ports.input.CreateTaskInputGateway;
import br.com.jtech.tasklist.application.ports.output.CreateTaskOutputGateway;
import br.com.jtech.tasklist.application.ports.output.GetTasklistsOutputGateway;

import java.util.UUID;

public class CreateTaskUseCase implements CreateTaskInputGateway {

    private final CreateTaskOutputGateway createTaskOutputGateway;
    private final GetTasklistsOutputGateway getTasklistsOutputGateway;

    public CreateTaskUseCase(CreateTaskOutputGateway createTaskOutputGateway,
                             GetTasklistsOutputGateway getTasklistsOutputGateway) {
        this.createTaskOutputGateway = createTaskOutputGateway;
        this.getTasklistsOutputGateway = getTasklistsOutputGateway;
    }

    @Override
    public Task create(Task task) {
        var tasklist = getTasklistsOutputGateway.findByIdAndUserId(
                UUID.fromString(task.getTasklistId()),
                UUID.fromString(task.getUserId()));
        if (tasklist == null) {
            throw new IllegalArgumentException("Tasklist not found or access denied");
        }
        return createTaskOutputGateway.create(task);
    }
}
