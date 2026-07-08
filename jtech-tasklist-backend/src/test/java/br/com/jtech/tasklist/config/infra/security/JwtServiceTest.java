package br.com.jtech.tasklist.config.infra.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private static final String TEST_SECRET = "a1b2c3d4e5f6a7b8c9d0e1f2a3b4c5d6e7f8a9b0c1d2e3f4a5b6c7d8e9f0a1b2";
    private static final long EXPIRATION_MS = 900_000;

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(TEST_SECRET, EXPIRATION_MS);
    }

    @Test
    void generateAndValidate_ShouldRoundTrip() {
        String token = jwtService.generateAccessToken("user-123", "user@test.com", "ROLE_USER", "Test User");

        assertThat(jwtService.validateToken(token)).isTrue();
        assertThat(jwtService.getUserIdFromToken(token)).isEqualTo("user-123");
        assertThat(jwtService.getTokenType(token)).isEqualTo("access");
        assertThat(jwtService.getRoleFromToken(token)).isEqualTo("ROLE_USER");
    }

    @Test
    void validateToken_ShouldReturnFalse_ForTamperedToken() {
        String token = jwtService.generateAccessToken("user-123", "user@test.com", "ROLE_USER", "Test User");
        String tampered = token.substring(0, token.lastIndexOf('.') + 1) + "invalidsignature";

        assertThat(jwtService.validateToken(tampered)).isFalse();
    }

    @Test
    void validateToken_ShouldReturnFalse_ForGarbageToken() {
        assertThat(jwtService.validateToken("not.a.token")).isFalse();
    }

    @Test
    void validateToken_ShouldReturnFalse_ForExpiredToken() throws InterruptedException {
        var shortLived = new JwtService(TEST_SECRET, 1L);
        String token = shortLived.generateAccessToken("user-123", "user@test.com", "ROLE_USER", "Test User");
        Thread.sleep(10);
        assertThat(shortLived.validateToken(token)).isFalse();
    }
}