package br.com.jtech.tasklist.application.core.usecases;

import br.com.jtech.tasklist.application.core.exceptions.InvalidCredentialsException;
import br.com.jtech.tasklist.application.ports.input.RefreshTokenInputGateway;
import br.com.jtech.tasklist.application.ports.output.RefreshTokenOutputGateway;
import br.com.jtech.tasklist.application.ports.output.TokenOutputGateway;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RefreshUseCase implements RefreshTokenInputGateway {

    private final RefreshTokenOutputGateway refreshTokenOutputGateway;
    private final TokenOutputGateway tokenOutputGateway;

    @Override
    public RefreshResult refresh(String refreshToken) {
        var user = refreshTokenOutputGateway.findValidUserByToken(refreshToken)
            .orElseThrow(() -> new InvalidCredentialsException("Invalid refresh token"));

        var newRefreshToken = refreshTokenOutputGateway.rotateRefreshToken(refreshToken, user)
            .orElseThrow(() -> new InvalidCredentialsException("Invalid refresh token"));

        String accessToken = tokenOutputGateway.generateAccessToken(user);
        return new RefreshResult(accessToken, newRefreshToken);
    }
}
