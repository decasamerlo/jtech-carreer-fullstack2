package br.com.jtech.tasklist.adapters.input.controllers;

import br.com.jtech.tasklist.adapters.input.protocols.AuthResponse;
import br.com.jtech.tasklist.adapters.input.protocols.LoginRequest;
import br.com.jtech.tasklist.adapters.input.protocols.RefreshTokenRequest;
import br.com.jtech.tasklist.adapters.input.protocols.RegisterRequest;
import br.com.jtech.tasklist.application.core.domains.User;
import br.com.jtech.tasklist.application.ports.input.LoginInputGateway;
import br.com.jtech.tasklist.application.ports.input.RefreshTokenInputGateway;
import br.com.jtech.tasklist.application.ports.input.RegisterUserInputGateway;
import br.com.jtech.tasklist.application.ports.output.PasswordHasherOutputGateway;
import br.com.jtech.tasklist.application.ports.output.RefreshTokenOutputGateway;
import br.com.jtech.tasklist.application.ports.output.TokenOutputGateway;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final RegisterUserInputGateway registerUserInputGateway;
    private final LoginInputGateway loginInputGateway;
    private final RefreshTokenInputGateway refreshTokenInputGateway;
    private final PasswordHasherOutputGateway passwordHasherOutputGateway;
    private final TokenOutputGateway tokenOutputGateway;
    private final RefreshTokenOutputGateway refreshTokenOutputGateway;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        User user = User.builder()
            .name(request.getName())
            .email(request.getEmail())
            .password(passwordHasherOutputGateway.encode(request.getPassword()))
            .build();

        User saved = registerUserInputGateway.register(user);

        String accessToken = tokenOutputGateway.generateAccessToken(saved);
        String refreshToken = refreshTokenOutputGateway.createRefreshToken(saved);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(AuthResponse.of(accessToken, refreshToken));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        var loginResult = loginInputGateway.login(request.getEmail(), request.getPassword());
        return ResponseEntity.ok(AuthResponse.of(loginResult.accessToken(), loginResult.refreshToken()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        var result = refreshTokenInputGateway.refresh(request.getRefreshToken());
        return ResponseEntity.ok(AuthResponse.of(result.accessToken(), result.refreshToken()));
    }
}
