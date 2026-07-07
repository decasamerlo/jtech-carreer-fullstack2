package br.com.jtech.tasklist.application.core.usecases;

import br.com.jtech.tasklist.application.core.domains.Task;
import br.com.jtech.tasklist.application.ports.input.UpdateTaskInputGateway;
import br.com.jtech.tasklist.application.ports.output.GetTasksOutputGateway;
import br.com.jtech.tasklist.application.ports.output.UpdateTaskOutputGateway;

public class UpdateTaskUseCase implements UpdateTaskInputGateway {

    private final UpdateTaskOutputGateway updateTaskOutputGateway;
    private final GetTasksOutputGateway getTasksOutputGateway;

    public UpdateTaskUseCase(UpdateTaskOutputGateway updateTaskOutputGateway,
                             GetTasksOutputGateway getTasksOutputGateway) {
        this.updateTaskOutputGateway = updateTaskOutputGateway;
        this.getTasksOutputGateway = getTasksOutputGateway;
    }

    @Override
    public Task update(Task task, String currentUserId) {
        Task existing = getTasksOutputGateway.findByIdAndUserId(
                java.util.UUID.fromString(task.getId()),
                java.util.UUID.fromString(currentUserId));
        if (existing == null) {
            throw new IllegalArgumentException("Task not found or access denied");
        }
        return updateTaskOutputGateway.update(task, java.util.UUID.fromString(currentUserId));
    }
}
