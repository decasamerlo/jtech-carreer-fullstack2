package br.com.jtech.tasklist.application.ports.input;

import br.com.jtech.tasklist.application.core.domains.Tasklist;

public interface CreateTasklistInputGateway {
    Tasklist create(Tasklist tasklist);
}