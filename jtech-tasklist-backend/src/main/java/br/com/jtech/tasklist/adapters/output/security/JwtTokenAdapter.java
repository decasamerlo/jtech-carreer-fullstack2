package br.com.jtech.tasklist.adapters.output.security;

import br.com.jtech.tasklist.application.core.domains.User;
import br.com.jtech.tasklist.application.ports.output.TokenOutputGateway;
import br.com.jtech.tasklist.config.infra.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtTokenAdapter implements TokenOutputGateway {

    private final JwtService jwtService;

    @Override
    public String generateAccessToken(User user) {
        return jwtService.generateAccessToken(
            user.getId(), user.getEmail(), user.getRole().name());
    }

    @Override
    public String getUserIdFromToken(String token) {
        return jwtService.getUserIdFromToken(token);
    }

    @Override
    public boolean validateToken(String token) {
        return jwtService.validateToken(token);
    }
}
