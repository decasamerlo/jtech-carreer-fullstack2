package br.com.jtech.tasklist.application.ports.input;

import br.com.jtech.tasklist.application.core.domains.User;

public interface RegisterUserInputGateway {
    User register(User user);
}
