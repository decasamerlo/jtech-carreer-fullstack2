package br.com.jtech.tasklist.application.ports.input;

public interface DeleteTasklistInputGateway {
    void delete(String id, String currentUserId);
}
