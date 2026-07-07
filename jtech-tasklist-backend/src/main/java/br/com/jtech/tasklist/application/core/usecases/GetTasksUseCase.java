package br.com.jtech.tasklist.application.core.usecases;

import br.com.jtech.tasklist.application.core.domains.Task;
import br.com.jtech.tasklist.application.ports.input.GetTasksInputGateway;
import br.com.jtech.tasklist.application.ports.output.GetTasksOutputGateway;

import java.util.List;
import java.util.UUID;

public class GetTasksUseCase implements GetTasksInputGateway {

    private final GetTasksOutputGateway getTasksOutputGateway;

    public GetTasksUseCase(GetTasksOutputGateway getTasksOutputGateway) {
        this.getTasksOutputGateway = getTasksOutputGateway;
    }

    @Override
    public List<Task> findByTasklistIdAndUserId(String tasklistId, String userId) {
        return getTasksOutputGateway.findByTasklistIdAndUserId(
                UUID.fromString(tasklistId),
                UUID.fromString(userId));
    }

    @Override
    public Task findByIdAndUserId(String id, String userId) {
        return getTasksOutputGateway.findByIdAndUserId(
                UUID.fromString(id),
                UUID.fromString(userId));
    }
}
