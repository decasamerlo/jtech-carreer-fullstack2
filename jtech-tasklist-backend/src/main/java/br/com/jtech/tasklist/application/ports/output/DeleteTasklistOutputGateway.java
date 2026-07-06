package br.com.jtech.tasklist.application.ports.output;

import java.util.UUID;

public interface DeleteTasklistOutputGateway {
    void delete(String id, UUID userId);
}
