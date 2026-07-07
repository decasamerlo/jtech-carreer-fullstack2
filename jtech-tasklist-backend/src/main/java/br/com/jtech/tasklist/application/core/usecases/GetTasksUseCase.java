package br.com.jtech.tasklist.application.core.usecases;

import br.com.jtech.tasklist.application.core.domains.Task;
import br.com.jtech.tasklist.application.ports.input.GetTasksInputGateway;
import br.com.jtech.tasklist.application.ports.output.GetTasklistsOutputGateway;
import br.com.jtech.tasklist.application.ports.output.GetTasksOutputGateway;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
public class GetTasksUseCase implements GetTasksInputGateway {

    private final GetTasksOutputGateway getTasksOutputGateway;
    private final GetTasklistsOutputGateway getTasklistsOutputGateway;

    @Override
    public List<Task> findByTasklistIdAndUserId(String tasklistId, String userId) {
        UUID tasklistUuid = UUID.fromString(tasklistId);
        UUID userUuid = UUID.fromString(userId);
        if (!getTasklistsOutputGateway.existsByTasklistIdAndUserId(tasklistUuid, userUuid)) {
            throw new IllegalArgumentException("Tasklist not found or access denied");
        }
        return getTasksOutputGateway.findByTasklistIdAndUserId(tasklistUuid, userUuid);
    }

    @Override
    public Task findByIdAndUserId(String id, String userId) {
        UUID idUuid = UUID.fromString(id);
        UUID userUuid = UUID.fromString(userId);
        Task task = getTasksOutputGateway.findByIdAndUserId(idUuid, userUuid);
        if (task != null && !getTasklistsOutputGateway.existsByTasklistIdAndUserId(
                UUID.fromString(task.getTasklistId()), userUuid)) {
            throw new IllegalArgumentException("Tasklist not found or access denied");
        }
        return task;
    }
}
