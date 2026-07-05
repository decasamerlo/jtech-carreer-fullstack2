package br.com.jtech.tasklist.adapters.input.controllers;

import br.com.jtech.tasklist.adapters.input.protocols.LoginRequest;
import br.com.jtech.tasklist.adapters.input.protocols.RegisterRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:application-test.properties")
class AuthIntegrationTest {

    @LocalServerPort
    private int port;

    private RestTemplate restTemplate;

    @BeforeEach
    void setUp() {
        restTemplate = new RestTemplate();
        restTemplate.setErrorHandler(new ResponseErrorHandler() {
            public boolean hasError(ClientHttpResponse response) {
                return false;
            }
            public void handleError(ClientHttpResponse response) {
            }
        });
    }

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    @SuppressWarnings("unchecked")
    private ResponseEntity<Map> post(String path, Object request) {
        return restTemplate.postForEntity(url(path), request, Map.class);
    }

    @Test
    void register_ShouldReturn201AndTokens() {
        RegisterRequest request = RegisterRequest.builder()
            .name("Test User")
            .email("test@example.com")
            .password("password123")
            .build();

        ResponseEntity<Map> response = post("/api/v1/auth/register", request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("accessToken")).isNotNull();
        assertThat(response.getBody().get("refreshToken")).isNotNull();
        assertThat(response.getBody().get("tokenType")).isEqualTo("Bearer");
    }

    @Test
    void register_ShouldRejectDuplicateEmail() {
        RegisterRequest request = RegisterRequest.builder()
            .name("Test User")
            .email("duplicate@example.com")
            .password("password123")
            .build();

        ResponseEntity<Map> first = post("/api/v1/auth/register", request);
        assertThat(first.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        ResponseEntity<Map> second = post("/api/v1/auth/register", request);
        assertThat(second.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void register_ShouldRejectInvalidEmail() {
        RegisterRequest request = RegisterRequest.builder()
            .name("Test User")
            .email("not-an-email")
            .password("password123")
            .build();

        ResponseEntity<Map> response = post("/api/v1/auth/register", request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void register_ShouldRejectShortPassword() {
        RegisterRequest request = RegisterRequest.builder()
            .name("Test User")
            .email("test2@example.com")
            .password("12345")
            .build();

        ResponseEntity<Map> response = post("/api/v1/auth/register", request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void login_ShouldReturn200AndTokens() {
        RegisterRequest registerRequest = RegisterRequest.builder()
            .name("Login User")
            .email("login@example.com")
            .password("password123")
            .build();

        post("/api/v1/auth/register", registerRequest);

        LoginRequest loginRequest = LoginRequest.builder()
            .email("login@example.com")
            .password("password123")
            .build();

        ResponseEntity<Map> response = post("/api/v1/auth/login", loginRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("accessToken")).isNotNull();
        assertThat(response.getBody().get("refreshToken")).isNotNull();
        assertThat(response.getBody().get("tokenType")).isEqualTo("Bearer");
    }

    @Test
    void login_ShouldRejectWrongPassword() {
        RegisterRequest registerRequest = RegisterRequest.builder()
            .name("Wrong Password User")
            .email("wrongpw@example.com")
            .password("password123")
            .build();

        post("/api/v1/auth/register", registerRequest);

        LoginRequest loginRequest = LoginRequest.builder()
            .email("wrongpw@example.com")
            .password("wrongpassword")
            .build();

        ResponseEntity<Map> response = post("/api/v1/auth/login", loginRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void login_ShouldRejectNonExistentEmail() {
        LoginRequest loginRequest = LoginRequest.builder()
            .email("nobody@example.com")
            .password("password123")
            .build();

        ResponseEntity<Map> response = post("/api/v1/auth/login", loginRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
