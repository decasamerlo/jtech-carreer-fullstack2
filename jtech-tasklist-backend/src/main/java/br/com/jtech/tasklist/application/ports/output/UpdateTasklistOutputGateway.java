package br.com.jtech.tasklist.application.ports.output;

import br.com.jtech.tasklist.application.core.domains.Tasklist;

public interface UpdateTasklistOutputGateway {
    Tasklist update(Tasklist tasklist);
}
