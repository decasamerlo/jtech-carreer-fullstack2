package br.com.jtech.tasklist.application.ports.input;

public interface RegisterUserInputGateway {
    RegisterResult register(RegisterCommand command);

    record RegisterCommand(String name, String email, String password) {}
    record RegisterResult(String accessToken, String refreshToken) {}
}
