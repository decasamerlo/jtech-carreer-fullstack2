package br.com.jtech.tasklist.application.core.usecases;

import br.com.jtech.tasklist.application.core.domains.User;
import br.com.jtech.tasklist.application.core.domains.UserRole;
import br.com.jtech.tasklist.application.ports.input.RegisterUserInputGateway;
import br.com.jtech.tasklist.application.ports.output.PasswordHasherOutputGateway;
import br.com.jtech.tasklist.application.ports.output.RefreshTokenOutputGateway;
import br.com.jtech.tasklist.application.ports.output.TokenOutputGateway;
import br.com.jtech.tasklist.application.ports.output.UserOutputGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class RegisterUserUseCaseTest {

    private UserOutputGateway userOutputGateway;
    private PasswordHasherOutputGateway passwordHasherOutputGateway;
    private TokenOutputGateway tokenOutputGateway;
    private RefreshTokenOutputGateway refreshTokenOutputGateway;

    private RegisterUserUseCase useCase;

    @BeforeEach
    void setUp() {
        userOutputGateway = mock(UserOutputGateway.class);
        passwordHasherOutputGateway = mock(PasswordHasherOutputGateway.class);
        tokenOutputGateway = mock(TokenOutputGateway.class);
        refreshTokenOutputGateway = mock(RefreshTokenOutputGateway.class);
        useCase = new RegisterUserUseCase(userOutputGateway, passwordHasherOutputGateway, tokenOutputGateway, refreshTokenOutputGateway);
    }

    @Test
    void register_ShouldReturnTokens_WhenEmailIsUnique() {
        var command = new RegisterUserInputGateway.RegisterCommand("Test User", "test@example.com", "password123");

        when(userOutputGateway.existsByEmail(command.email())).thenReturn(false);
        when(passwordHasherOutputGateway.encode(command.password())).thenReturn("hashed-password");
        when(userOutputGateway.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            return User.builder()
                .id("user-id-123")
                .name(u.getName())
                .email(u.getEmail())
                .password(u.getPassword())
                .role(u.getRole())
                .build();
        });
        when(tokenOutputGateway.generateAccessToken(any(User.class))).thenReturn("access-token-jwt");
        when(refreshTokenOutputGateway.createRefreshToken(any(User.class))).thenReturn("refresh-token-uuid");

        var result = useCase.register(command);

        assertThat(result).isNotNull();
        assertThat(result.accessToken()).isEqualTo("access-token-jwt");
        assertThat(result.refreshToken()).isEqualTo("refresh-token-uuid");

        verify(userOutputGateway).existsByEmail(command.email());
        verify(passwordHasherOutputGateway).encode(command.password());
        verify(userOutputGateway).save(any(User.class));
        verify(tokenOutputGateway).generateAccessToken(any(User.class));
        verify(refreshTokenOutputGateway).createRefreshToken(any(User.class));
    }

    @Test
    void register_ShouldThrowException_WhenEmailAlreadyExists() {
        var command = new RegisterUserInputGateway.RegisterCommand("Test User", "existing@example.com", "password123");

        when(userOutputGateway.existsByEmail(command.email())).thenReturn(true);

        assertThatThrownBy(() -> useCase.register(command))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Email already registered: existing@example.com");

        verify(userOutputGateway).existsByEmail(command.email());
        verify(passwordHasherOutputGateway, never()).encode(any());
        verify(userOutputGateway, never()).save(any());
        verify(tokenOutputGateway, never()).generateAccessToken(any());
        verify(refreshTokenOutputGateway, never()).createRefreshToken(any());
    }

    @Test
    void register_ShouldAlwaysAssignUserRole() {
        var command = new RegisterUserInputGateway.RegisterCommand("Test User", "test@example.com", "password123");

        when(userOutputGateway.existsByEmail(command.email())).thenReturn(false);
        when(passwordHasherOutputGateway.encode(command.password())).thenReturn("hashed-password");
        when(userOutputGateway.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            return User.builder()
                .id("user-id-123")
                .name(u.getName())
                .email(u.getEmail())
                .password(u.getPassword())
                .role(u.getRole())
                .build();
        });
        when(tokenOutputGateway.generateAccessToken(any(User.class))).thenReturn("access-token-jwt");
        when(refreshTokenOutputGateway.createRefreshToken(any(User.class))).thenReturn("refresh-token-uuid");

        useCase.register(command);

        verify(userOutputGateway).save(argThat(user -> user.getRole() == UserRole.ROLE_USER));
        verify(tokenOutputGateway).generateAccessToken(argThat(u -> "user-id-123".equals(u.getId())));
        verify(refreshTokenOutputGateway).createRefreshToken(argThat(u -> "user-id-123".equals(u.getId())));
    }

    @Test
    void register_ShouldEncodePassword_WithPasswordHasher() {
        var command = new RegisterUserInputGateway.RegisterCommand("Test User", "test@example.com", "password123");

        when(userOutputGateway.existsByEmail(command.email())).thenReturn(false);
        when(passwordHasherOutputGateway.encode(command.password())).thenReturn("hashed-password");
        when(userOutputGateway.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            return User.builder()
                .id("user-id-123")
                .name(u.getName())
                .email(u.getEmail())
                .password(u.getPassword())
                .role(u.getRole())
                .build();
        });
        when(tokenOutputGateway.generateAccessToken(any(User.class))).thenReturn("access-token-jwt");
        when(refreshTokenOutputGateway.createRefreshToken(any(User.class))).thenReturn("refresh-token-uuid");

        useCase.register(command);

        verify(passwordHasherOutputGateway).encode("password123");
        verify(userOutputGateway).save(argThat(user -> "hashed-password".equals(user.getPassword())));
        verify(tokenOutputGateway).generateAccessToken(argThat(u -> "user-id-123".equals(u.getId())));
        verify(refreshTokenOutputGateway).createRefreshToken(argThat(u -> "user-id-123".equals(u.getId())));
    }
}
