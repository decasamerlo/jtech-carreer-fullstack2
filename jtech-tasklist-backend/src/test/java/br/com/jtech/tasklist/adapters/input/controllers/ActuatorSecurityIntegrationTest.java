package br.com.jtech.tasklist.adapters.input.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.client.RestClient;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:application-test.properties")
class ActuatorSecurityIntegrationTest {

    @LocalServerPort
    private int port;

    private RestClient client;

    @BeforeEach
    void setUp() {
        client = RestClient.builder()
                .defaultStatusHandler(status -> true, (request, response) -> {})
                .build();
    }

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    @SuppressWarnings("unchecked")
    private ResponseEntity<Map> get(String path) {
        return client.method(HttpMethod.GET)
                .uri(url(path))
                .retrieve()
                .toEntity(new ParameterizedTypeReference<Map>() {});
    }

    @Test
    void health_ShouldBeAccessibleWithoutAuth() {
        ResponseEntity<Map> response = get("/actuator/health");
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void nonHealthActuatorEndpoint_ShouldNotBeAccessibleWithoutAuth() {
        ResponseEntity<Map> response = get("/actuator/env");
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }
}
