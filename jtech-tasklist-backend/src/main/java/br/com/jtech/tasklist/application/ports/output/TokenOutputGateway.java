package br.com.jtech.tasklist.application.ports.output;

import br.com.jtech.tasklist.application.core.domains.User;

public interface TokenOutputGateway {
    String generateAccessToken(User user);
    String getUserIdFromToken(String token);
    boolean validateToken(String token);
}
