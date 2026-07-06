package br.com.jtech.tasklist.application.core.usecases;

import br.com.jtech.tasklist.application.core.domains.Tasklist;
import br.com.jtech.tasklist.application.ports.input.UpdateTasklistInputGateway;
import br.com.jtech.tasklist.application.ports.output.GetTasklistsOutputGateway;
import br.com.jtech.tasklist.application.ports.output.UpdateTasklistOutputGateway;

public class UpdateTasklistUseCase implements UpdateTasklistInputGateway {

    private final UpdateTasklistOutputGateway updateTasklistOutputGateway;
    private final GetTasklistsOutputGateway getTasklistsOutputGateway;

    public UpdateTasklistUseCase(UpdateTasklistOutputGateway updateTasklistOutputGateway,
                                 GetTasklistsOutputGateway getTasklistsOutputGateway) {
        this.updateTasklistOutputGateway = updateTasklistOutputGateway;
        this.getTasklistsOutputGateway = getTasklistsOutputGateway;
    }

    @Override
    public Tasklist update(Tasklist tasklist, String currentUserId) {
        Tasklist existing = getTasklistsOutputGateway.findByIdAndUserId(
                java.util.UUID.fromString(tasklist.getId()),
                java.util.UUID.fromString(currentUserId));
        if (existing == null) {
            throw new IllegalArgumentException("Tasklist not found or access denied");
        }
        return updateTasklistOutputGateway.update(tasklist, java.util.UUID.fromString(currentUserId));
    }
}
