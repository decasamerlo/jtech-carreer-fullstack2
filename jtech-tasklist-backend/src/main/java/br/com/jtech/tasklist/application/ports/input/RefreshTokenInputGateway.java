package br.com.jtech.tasklist.application.ports.input;

public interface RefreshTokenInputGateway {
    RefreshTokenInputGateway.RefreshResult refresh(String refreshToken);

    record RefreshResult(String accessToken, String refreshToken) {}
}
