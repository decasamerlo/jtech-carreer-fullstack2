package br.com.jtech.tasklist.application.ports.input;

public interface LoginInputGateway {
    LoginInputGateway.LoginResult login(String email, String password);

    record LoginResult(String accessToken, String refreshToken) {}
}
