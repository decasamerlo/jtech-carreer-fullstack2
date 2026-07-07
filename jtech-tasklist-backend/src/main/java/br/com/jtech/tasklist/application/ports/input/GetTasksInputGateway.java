package br.com.jtech.tasklist.application.ports.input;

import br.com.jtech.tasklist.application.core.domains.Task;

import java.util.List;

public interface GetTasksInputGateway {
    List<Task> findByTasklistIdAndUserId(String tasklistId, String userId);
    Task findByIdAndUserId(String id, String userId);
}
