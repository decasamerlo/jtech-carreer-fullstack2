package br.com.jtech.tasklist.application.ports.output;

import br.com.jtech.tasklist.application.core.domains.User;

import java.util.Optional;

public interface RefreshTokenOutputGateway {
    String createRefreshToken(User user);
    Optional<String> rotateRefreshToken(String currentToken, User user);
    void revokeAllUserTokens(String userId);
    boolean validateRefreshToken(String token);
    Optional<String> getUserIdFromRefreshToken(String token);
    Optional<User> findValidUserByToken(String token);
}
