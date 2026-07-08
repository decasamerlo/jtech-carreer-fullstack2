package br.com.jtech.tasklist.application.core.usecases;

import br.com.jtech.tasklist.application.core.domains.User;
import br.com.jtech.tasklist.application.core.exceptions.InvalidCredentialsException;
import br.com.jtech.tasklist.application.ports.output.RefreshTokenOutputGateway;
import br.com.jtech.tasklist.application.ports.output.TokenOutputGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class RefreshUseCaseTest {

    private RefreshTokenOutputGateway refreshTokenOutputGateway;
    private TokenOutputGateway tokenOutputGateway;

    private RefreshUseCase refreshUseCase;

    @BeforeEach
    void setUp() {
        refreshTokenOutputGateway = mock(RefreshTokenOutputGateway.class);
        tokenOutputGateway = mock(TokenOutputGateway.class);
        refreshUseCase = new RefreshUseCase(refreshTokenOutputGateway, tokenOutputGateway);
    }

    @Test
    void refresh_ShouldReturnNewTokens_WhenValidRefreshToken() {
        var user = User.builder()
                .id("user-id-123")
                .name("Test User")
                .build();

        when(refreshTokenOutputGateway.findValidUserByToken("valid-refresh-token"))
                .thenReturn(Optional.of(user));
        when(refreshTokenOutputGateway.rotateRefreshToken("valid-refresh-token", user))
                .thenReturn(Optional.of("new-refresh-token"));
        when(tokenOutputGateway.generateAccessToken(user))
                .thenReturn("new-access-token");

        var result = refreshUseCase.refresh("valid-refresh-token");

        assertThat(result).isNotNull();
        assertThat(result.accessToken()).isEqualTo("new-access-token");
        assertThat(result.refreshToken()).isEqualTo("new-refresh-token");

        verify(refreshTokenOutputGateway).findValidUserByToken("valid-refresh-token");
        verify(refreshTokenOutputGateway).rotateRefreshToken("valid-refresh-token", user);
        verify(tokenOutputGateway).generateAccessToken(user);
    }

    @Test
    void refresh_ShouldThrowInvalidCredentials_WhenTokenNotFound() {
        when(refreshTokenOutputGateway.findValidUserByToken("bad-token"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> refreshUseCase.refresh("bad-token"))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid refresh token");

        verify(refreshTokenOutputGateway).findValidUserByToken("bad-token");
        verify(refreshTokenOutputGateway, never()).rotateRefreshToken(any(), any());
        verify(tokenOutputGateway, never()).generateAccessToken(any());
    }

    @Test
    void refresh_ShouldThrowInvalidCredentials_WhenRotateFails() {
        var user = User.builder()
                .id("user-id-123")
                .name("Test User")
                .build();

        when(refreshTokenOutputGateway.findValidUserByToken("stale-token"))
                .thenReturn(Optional.of(user));
        when(refreshTokenOutputGateway.rotateRefreshToken("stale-token", user))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> refreshUseCase.refresh("stale-token"))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid refresh token");

        verify(refreshTokenOutputGateway).findValidUserByToken("stale-token");
        verify(refreshTokenOutputGateway).rotateRefreshToken("stale-token", user);
        verify(tokenOutputGateway, never()).generateAccessToken(any());
    }
}