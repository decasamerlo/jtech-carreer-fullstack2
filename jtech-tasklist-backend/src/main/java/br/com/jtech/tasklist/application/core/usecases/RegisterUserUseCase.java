package br.com.jtech.tasklist.application.core.usecases;

import br.com.jtech.tasklist.application.core.domains.User;
import br.com.jtech.tasklist.application.core.domains.UserRole;
import br.com.jtech.tasklist.application.ports.input.RegisterUserInputGateway;
import br.com.jtech.tasklist.application.ports.output.PasswordHasherOutputGateway;
import br.com.jtech.tasklist.application.ports.output.RefreshTokenOutputGateway;
import br.com.jtech.tasklist.application.ports.output.TokenOutputGateway;
import br.com.jtech.tasklist.application.ports.output.UserOutputGateway;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RegisterUserUseCase implements RegisterUserInputGateway {

    private final UserOutputGateway userOutputGateway;
    private final PasswordHasherOutputGateway passwordHasherOutputGateway;
    private final TokenOutputGateway tokenOutputGateway;
    private final RefreshTokenOutputGateway refreshTokenOutputGateway;

    @Override
    public RegisterResult register(RegisterCommand command) {
        if (userOutputGateway.existsByEmail(command.email())) {
            throw new IllegalArgumentException("Email already registered: " + command.email());
        }

        User user = User.builder()
            .name(command.name())
            .email(command.email())
            .password(passwordHasherOutputGateway.encode(command.password()))
            .role(UserRole.ROLE_USER)
            .build();

        User saved = userOutputGateway.save(user);

        String accessToken = tokenOutputGateway.generateAccessToken(saved);
        String refreshToken = refreshTokenOutputGateway.createRefreshToken(saved);

        return new RegisterResult(accessToken, refreshToken);
    }
}
