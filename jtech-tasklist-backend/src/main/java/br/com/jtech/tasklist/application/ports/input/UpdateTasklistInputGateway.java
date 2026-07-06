package br.com.jtech.tasklist.application.ports.input;

import br.com.jtech.tasklist.application.core.domains.Tasklist;

public interface UpdateTasklistInputGateway {
    Tasklist update(Tasklist tasklist, String currentUserId);
}
