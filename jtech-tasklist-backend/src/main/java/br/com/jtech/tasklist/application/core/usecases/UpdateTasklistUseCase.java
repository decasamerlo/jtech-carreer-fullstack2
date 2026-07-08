package br.com.jtech.tasklist.application.core.usecases;

import br.com.jtech.tasklist.application.core.domains.Tasklist;
import br.com.jtech.tasklist.application.ports.input.UpdateTasklistInputGateway;
import br.com.jtech.tasklist.application.ports.output.GetTasklistsOutputGateway;
import br.com.jtech.tasklist.application.ports.output.UpdateTasklistOutputGateway;

import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
public class UpdateTasklistUseCase implements UpdateTasklistInputGateway {

    private final UpdateTasklistOutputGateway updateTasklistOutputGateway;
    private final GetTasklistsOutputGateway getTasklistsOutputGateway;

    @Override
    public Tasklist update(Tasklist tasklist, String currentUserId) {
        Tasklist existing = getTasklistsOutputGateway.findByIdAndUserId(
                UUID.fromString(tasklist.getId()),
                UUID.fromString(currentUserId));
        if (existing == null) {
            throw new IllegalArgumentException("Tasklist not found or access denied");
        }
        String trimmedName = tasklist.getName().trim();
        tasklist.setName(trimmedName);
        boolean duplicate = getTasklistsOutputGateway.existsByUserIdAndNameAndIdNot(
                UUID.fromString(currentUserId),
                trimmedName,
                UUID.fromString(tasklist.getId()));
        if (duplicate) {
            throw new IllegalArgumentException("A list with this name already exists");
        }
        return updateTasklistOutputGateway.update(tasklist, UUID.fromString(currentUserId));
    }
}
