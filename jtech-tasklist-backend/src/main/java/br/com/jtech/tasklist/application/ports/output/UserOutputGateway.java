package br.com.jtech.tasklist.application.ports.output;

import br.com.jtech.tasklist.application.core.domains.User;

import java.util.Optional;

public interface UserOutputGateway {
    User save(User user);
    Optional<User> findById(String id);
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
}
