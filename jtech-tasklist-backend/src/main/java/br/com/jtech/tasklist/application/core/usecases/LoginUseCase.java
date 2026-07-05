package br.com.jtech.tasklist.application.core.usecases;

import br.com.jtech.tasklist.application.core.domains.User;
import br.com.jtech.tasklist.application.core.exceptions.InvalidCredentialsException;
import br.com.jtech.tasklist.application.ports.input.LoginInputGateway;
import br.com.jtech.tasklist.application.ports.output.PasswordHasherOutputGateway;
import br.com.jtech.tasklist.application.ports.output.RefreshTokenOutputGateway;
import br.com.jtech.tasklist.application.ports.output.TokenOutputGateway;
import br.com.jtech.tasklist.application.ports.output.UserOutputGateway;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class LoginUseCase implements LoginInputGateway {
    private final UserOutputGateway userOutputGateway;
    private final RefreshTokenOutputGateway refreshTokenOutputGateway;
    private final TokenOutputGateway tokenOutputGateway;
    private final PasswordHasherOutputGateway passwordHasherOutputGateway;

    @Override
    public LoginResult login(String email, String password) {
        User user = userOutputGateway.findByEmail(email)
            .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        if (!passwordHasherOutputGateway.matches(password, user.getPassword())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        String accessToken = tokenOutputGateway.generateAccessToken(user);
        String refreshToken = refreshTokenOutputGateway.createRefreshToken(user);

        return new LoginResult(accessToken, refreshToken);
    }
}
