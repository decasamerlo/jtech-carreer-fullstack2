package br.com.jtech.tasklist.application.ports.output;

import br.com.jtech.tasklist.application.core.domains.Task;

import java.util.List;
import java.util.UUID;

public interface GetTasksOutputGateway {
    List<Task> findByTasklistIdAndUserId(UUID tasklistId, UUID userId);
    Task findByIdAndUserId(UUID id, UUID userId);
    boolean existsByTasklistIdAndTitle(UUID tasklistId, String title);
    boolean existsByTasklistIdAndTitleAndIdNot(UUID tasklistId, String title, UUID excludeId);
}
