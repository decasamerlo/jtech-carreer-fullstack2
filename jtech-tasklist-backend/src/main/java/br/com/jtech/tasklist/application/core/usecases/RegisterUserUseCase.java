package br.com.jtech.tasklist.application.core.usecases;

import br.com.jtech.tasklist.application.core.domains.User;
import br.com.jtech.tasklist.application.core.domains.UserRole;
import br.com.jtech.tasklist.application.ports.input.RegisterUserInputGateway;
import br.com.jtech.tasklist.application.ports.output.UserOutputGateway;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RegisterUserUseCase implements RegisterUserInputGateway {

    private final UserOutputGateway userOutputGateway;

    @Override
    public User register(User user) {
        if (user.getRole() == null) {
            user.setRole(UserRole.ROLE_USER);
        }
        if (userOutputGateway.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Email already registered: " + user.getEmail());
        }
        return userOutputGateway.save(user);
    }
}
