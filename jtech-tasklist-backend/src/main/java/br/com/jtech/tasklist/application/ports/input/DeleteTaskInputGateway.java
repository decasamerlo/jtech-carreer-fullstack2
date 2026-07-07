package br.com.jtech.tasklist.application.ports.input;

public interface DeleteTaskInputGateway {
    void delete(String id, String currentUserId);
}
