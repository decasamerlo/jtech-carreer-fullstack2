package br.com.jtech.tasklist.application.core.usecases;

import br.com.jtech.tasklist.application.core.domains.Tasklist;
import br.com.jtech.tasklist.application.ports.input.GetTasklistsInputGateway;
import br.com.jtech.tasklist.application.ports.output.GetTasklistsOutputGateway;

import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class GetTasklistsUseCase implements GetTasklistsInputGateway {

    private final GetTasklistsOutputGateway getTasklistsOutputGateway;

    @Override
    public List<Tasklist> findAllByUserId(String userId) {
        return getTasklistsOutputGateway.findAllByUserId(java.util.UUID.fromString(userId));
    }
}
