package br.com.jtech.tasklist.application.ports.output;

import java.util.UUID;

public interface DeleteTaskOutputGateway {
    void delete(String id, UUID userId);
}
