package br.com.jtech.tasklist.application.core.usecases;

import br.com.jtech.tasklist.application.core.domains.Tasklist;
import br.com.jtech.tasklist.application.ports.input.DeleteTasklistInputGateway;
import br.com.jtech.tasklist.application.ports.output.DeleteTasklistOutputGateway;
import br.com.jtech.tasklist.application.ports.output.GetTasklistsOutputGateway;

import java.util.UUID;

public class DeleteTasklistUseCase implements DeleteTasklistInputGateway {

    private final DeleteTasklistOutputGateway deleteTasklistOutputGateway;
    private final GetTasklistsOutputGateway getTasklistsOutputGateway;

    public DeleteTasklistUseCase(DeleteTasklistOutputGateway deleteTasklistOutputGateway,
                                 GetTasklistsOutputGateway getTasklistsOutputGateway) {
        this.deleteTasklistOutputGateway = deleteTasklistOutputGateway;
        this.getTasklistsOutputGateway = getTasklistsOutputGateway;
    }

    @Override
    public void delete(String id, String currentUserId) {
        Tasklist existing = getTasklistsOutputGateway.findByIdAndUserId(
                UUID.fromString(id),
                UUID.fromString(currentUserId));
        if (existing == null) {
            throw new IllegalArgumentException("Tasklist not found or access denied");
        }
        deleteTasklistOutputGateway.delete(id, UUID.fromString(currentUserId));
    }
}
