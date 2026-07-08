package br.com.jtech.tasklist.application.core.usecases;

import br.com.jtech.tasklist.application.core.domains.User;
import br.com.jtech.tasklist.application.core.exceptions.InvalidCredentialsException;
import br.com.jtech.tasklist.application.ports.output.PasswordHasherOutputGateway;
import br.com.jtech.tasklist.application.ports.output.RefreshTokenOutputGateway;
import br.com.jtech.tasklist.application.ports.output.TokenOutputGateway;
import br.com.jtech.tasklist.application.ports.output.UserOutputGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class LoginUseCaseTest {

    private UserOutputGateway userOutputGateway;
    private RefreshTokenOutputGateway refreshTokenOutputGateway;
    private TokenOutputGateway tokenOutputGateway;
    private PasswordHasherOutputGateway passwordHasherOutputGateway;

    private LoginUseCase loginUseCase;

    @BeforeEach
    void setUp() {
        userOutputGateway = mock(UserOutputGateway.class);
        refreshTokenOutputGateway = mock(RefreshTokenOutputGateway.class);
        tokenOutputGateway = mock(TokenOutputGateway.class);
        passwordHasherOutputGateway = mock(PasswordHasherOutputGateway.class);
        loginUseCase = new LoginUseCase(userOutputGateway, refreshTokenOutputGateway, tokenOutputGateway, passwordHasherOutputGateway);
    }

    @Test
    void login_ShouldReturnTokens_WhenCredentialsValid() {
        var user = User.builder()
                .id("user-id-123")
                .name("Test User")
                .email("test@example.com")
                .password("hashed-password")
                .build();

        when(userOutputGateway.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordHasherOutputGateway.matches("password123", "hashed-password")).thenReturn(true);
        when(tokenOutputGateway.generateAccessToken(user)).thenReturn("access-token-jwt");
        when(refreshTokenOutputGateway.createRefreshToken(user)).thenReturn("refresh-token-uuid");

        var result = loginUseCase.login("test@example.com", "password123");

        assertThat(result).isNotNull();
        assertThat(result.accessToken()).isEqualTo("access-token-jwt");
        assertThat(result.refreshToken()).isEqualTo("refresh-token-uuid");

        verify(userOutputGateway).findByEmail("test@example.com");
        verify(passwordHasherOutputGateway).matches("password123", "hashed-password");
        verify(tokenOutputGateway).generateAccessToken(user);
        verify(refreshTokenOutputGateway).createRefreshToken(user);
    }

    @Test
    void login_ShouldThrowInvalidCredentials_WhenEmailNotFound() {
        when(userOutputGateway.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> loginUseCase.login("unknown@example.com", "password123"))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid email or password");

        verify(userOutputGateway).findByEmail("unknown@example.com");
        verify(passwordHasherOutputGateway, never()).matches(any(), any());
        verify(tokenOutputGateway, never()).generateAccessToken(any());
        verify(refreshTokenOutputGateway, never()).createRefreshToken(any());
    }

    @Test
    void login_ShouldThrowInvalidCredentials_WhenPasswordMismatch() {
        var user = User.builder()
                .id("user-id-123")
                .email("test@example.com")
                .password("hashed-password")
                .build();

        when(userOutputGateway.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordHasherOutputGateway.matches("wrong-password", "hashed-password")).thenReturn(false);

        assertThatThrownBy(() -> loginUseCase.login("test@example.com", "wrong-password"))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid email or password");

        verify(userOutputGateway).findByEmail("test@example.com");
        verify(passwordHasherOutputGateway).matches("wrong-password", "hashed-password");
        verify(tokenOutputGateway, never()).generateAccessToken(any());
        verify(refreshTokenOutputGateway, never()).createRefreshToken(any());
    }
}