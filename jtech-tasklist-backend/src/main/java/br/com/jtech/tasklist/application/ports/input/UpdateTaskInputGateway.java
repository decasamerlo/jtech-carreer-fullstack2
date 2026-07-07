package br.com.jtech.tasklist.application.ports.input;

import br.com.jtech.tasklist.application.core.domains.Task;

public interface UpdateTaskInputGateway {
    Task update(Task task, String currentUserId);
}
