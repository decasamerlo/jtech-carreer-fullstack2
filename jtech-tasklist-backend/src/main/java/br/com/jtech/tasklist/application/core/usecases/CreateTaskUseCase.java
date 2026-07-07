package br.com.jtech.tasklist.application.core.usecases;

import br.com.jtech.tasklist.application.core.domains.Task;
import br.com.jtech.tasklist.application.ports.input.CreateTaskInputGateway;
import br.com.jtech.tasklist.application.ports.output.CreateTaskOutputGateway;
import br.com.jtech.tasklist.application.ports.output.GetTasklistsOutputGateway;
import br.com.jtech.tasklist.application.ports.output.GetTasksOutputGateway;

import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
public class CreateTaskUseCase implements CreateTaskInputGateway {

    private final CreateTaskOutputGateway createTaskOutputGateway;
    private final GetTasklistsOutputGateway getTasklistsOutputGateway;
    private final GetTasksOutputGateway getTasksOutputGateway;

    @Override
    public Task create(Task task) {
        var tasklist = getTasklistsOutputGateway.findByIdAndUserId(
                UUID.fromString(task.getTasklistId()),
                UUID.fromString(task.getUserId()));
        if (tasklist == null) {
            throw new IllegalArgumentException("Tasklist not found or access denied");
        }
        boolean duplicate = getTasksOutputGateway.existsByTasklistIdAndTitle(
                UUID.fromString(task.getTasklistId()),
                task.getTitle());
        if (duplicate) {
            throw new IllegalArgumentException("A task with this title already exists in this list");
        }
        return createTaskOutputGateway.create(task);
    }
}
