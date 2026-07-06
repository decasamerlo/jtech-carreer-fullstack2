package br.com.jtech.tasklist.application.ports.output;

import br.com.jtech.tasklist.application.core.domains.Tasklist;

import java.util.List;
import java.util.UUID;

public interface GetTasklistsOutputGateway {
    List<Tasklist> findAllByUserId(UUID userId);
    Tasklist findByIdAndUserId(UUID id, UUID userId);
}
