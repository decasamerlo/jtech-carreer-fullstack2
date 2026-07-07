package br.com.jtech.tasklist.application.core.usecases;

import br.com.jtech.tasklist.application.core.domains.Task;
import br.com.jtech.tasklist.application.ports.input.UpdateTaskInputGateway;
import br.com.jtech.tasklist.application.ports.output.GetTasklistsOutputGateway;
import br.com.jtech.tasklist.application.ports.output.GetTasksOutputGateway;
import br.com.jtech.tasklist.application.ports.output.UpdateTaskOutputGateway;

import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
public class UpdateTaskUseCase implements UpdateTaskInputGateway {

    private final UpdateTaskOutputGateway updateTaskOutputGateway;
    private final GetTasksOutputGateway getTasksOutputGateway;
    private final GetTasklistsOutputGateway getTasklistsOutputGateway;

    @Override
    public Task update(Task task, String currentUserId) {
        Task existing = getTasksOutputGateway.findByIdAndUserId(
                UUID.fromString(task.getId()),
                UUID.fromString(currentUserId));
        if (existing == null) {
            throw new IllegalArgumentException("Task not found or access denied");
        }
        if (!getTasklistsOutputGateway.existsByTasklistIdAndUserId(
                UUID.fromString(existing.getTasklistId()),
                UUID.fromString(currentUserId))) {
            throw new IllegalArgumentException("Tasklist not found or access denied");
        }
        boolean duplicate = getTasksOutputGateway.existsByTasklistIdAndTitleAndIdNot(
                UUID.fromString(existing.getTasklistId()),
                task.getTitle(),
                UUID.fromString(task.getId()));
        if (duplicate) {
            throw new IllegalArgumentException("A task with this title already exists in this list");
        }
        return updateTaskOutputGateway.update(task, UUID.fromString(currentUserId));
    }
}
