package br.com.jtech.tasklist.adapters.output;

import br.com.jtech.tasklist.adapters.output.repositories.RefreshTokenRepository;
import br.com.jtech.tasklist.adapters.output.repositories.UserRepository;
import br.com.jtech.tasklist.adapters.output.repositories.entities.RefreshTokenEntity;
import br.com.jtech.tasklist.application.core.domains.User;
import br.com.jtech.tasklist.application.ports.output.RefreshTokenOutputGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.ZoneId;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RefreshTokenAdapter implements RefreshTokenOutputGateway {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    @Value("${app.jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    @Override
    public String createRefreshToken(User user) {
        String token = UUID.randomUUID().toString().replace("-", "");
        UUID userId = UUID.fromString(user.getId());
        RefreshTokenEntity entity = RefreshTokenEntity.builder()
            .id(UUID.randomUUID())
            .token(token)
            .expiresAt(Instant.now().plusMillis(refreshTokenExpiration).atZone(ZoneId.systemDefault()).toLocalDateTime())
            .revoked(false)
            .build();
        entity.setUser(userRepository.getReferenceById(userId));
        refreshTokenRepository.save(entity);
        return token;
    }

    @Override
    public Optional<String> rotateRefreshToken(String currentToken, User user) {
        Optional<RefreshTokenEntity> existing = refreshTokenRepository.findByToken(currentToken);
        if (existing.isEmpty() || !existing.get().isValid()) {
            return Optional.empty();
        }
        existing.get().setRevoked(true);
        refreshTokenRepository.save(existing.get());
        return Optional.of(createRefreshToken(user));
    }

    @Override
    public void revokeAllUserTokens(String userId) {
        refreshTokenRepository.revokeAllByUserId(UUID.fromString(userId));
    }

    @Override
    public Optional<User> findValidUserByToken(String token) {
        return refreshTokenRepository.findByToken(token)
            .filter(RefreshTokenEntity::isValid)
            .map(entity -> UserMapper.toDomain(entity.getUser()));
    }

    @Override
    public boolean validateRefreshToken(String token) {
        return refreshTokenRepository.findByToken(token)
            .map(RefreshTokenEntity::isValid)
            .orElse(false);
    }

    @Override
    public Optional<String> getUserIdFromRefreshToken(String token) {
        return refreshTokenRepository.findByToken(token)
            .map(rt -> rt.getUser().getId().toString());
    }
}
