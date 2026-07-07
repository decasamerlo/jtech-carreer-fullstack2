package br.com.jtech.tasklist.application.ports.output;

import br.com.jtech.tasklist.application.core.domains.Task;

import java.util.UUID;

public interface UpdateTaskOutputGateway {
    Task update(Task task, UUID userId);
}
