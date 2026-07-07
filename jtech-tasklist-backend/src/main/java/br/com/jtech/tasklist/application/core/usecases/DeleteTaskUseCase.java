package br.com.jtech.tasklist.application.core.usecases;

import br.com.jtech.tasklist.application.core.domains.Task;
import br.com.jtech.tasklist.application.ports.input.DeleteTaskInputGateway;
import br.com.jtech.tasklist.application.ports.output.DeleteTaskOutputGateway;
import br.com.jtech.tasklist.application.ports.output.GetTasksOutputGateway;

import java.util.UUID;

public class DeleteTaskUseCase implements DeleteTaskInputGateway {

    private final DeleteTaskOutputGateway deleteTaskOutputGateway;
    private final GetTasksOutputGateway getTasksOutputGateway;

    public DeleteTaskUseCase(DeleteTaskOutputGateway deleteTaskOutputGateway,
                             GetTasksOutputGateway getTasksOutputGateway) {
        this.deleteTaskOutputGateway = deleteTaskOutputGateway;
        this.getTasksOutputGateway = getTasksOutputGateway;
    }

    @Override
    public void delete(String id, String currentUserId) {
        Task existing = getTasksOutputGateway.findByIdAndUserId(
                UUID.fromString(id),
                UUID.fromString(currentUserId));
        if (existing == null) {
            throw new IllegalArgumentException("Task not found or access denied");
        }
        deleteTaskOutputGateway.delete(id, UUID.fromString(currentUserId));
    }
}
