package br.com.jtech.tasklist.adapters.input.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:application-test.properties")
class TasklistIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private String user1Token;
    private String user2Token;

    @BeforeEach
    void setUp() throws Exception {
        jdbcTemplate.execute("DELETE FROM tasklist");
        jdbcTemplate.execute("DELETE FROM refresh_tokens");
        jdbcTemplate.execute("DELETE FROM users");
        user1Token = registerAndLogin("user1@example.com", "User One");
        user2Token = registerAndLogin("user2@example.com", "User Two");
    }

    private String base() {
        return "http://localhost:" + port;
    }

    private String registerAndLogin(String email, String name) throws Exception {
        String registerBody = objectMapper.writeValueAsString(Map.of("name", name, "email", email, "password", "password123"));
        HttpRequest registerReq = HttpRequest.newBuilder()
                .uri(URI.create(base() + "/api/v1/auth/register"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(registerBody))
                .build();
        client.send(registerReq, HttpResponse.BodyHandlers.ofString());

        String loginBody = objectMapper.writeValueAsString(Map.of("email", email, "password", "password123"));
        HttpRequest loginReq = HttpRequest.newBuilder()
                .uri(URI.create(base() + "/api/v1/auth/login"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(loginBody))
                .build();
        HttpResponse<String> loginResp = client.send(loginReq, HttpResponse.BodyHandlers.ofString());
        Map<String, Object> body = objectMapper.readValue(loginResp.body(), new TypeReference<>() {});
        return (String) body.get("accessToken");
    }

    private HttpResponse<String> send(String method, String path, String json, String token) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(base() + path))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json");
        if (token != null) builder.header("Authorization", "Bearer " + token);
        switch (method) {
            case "POST" -> builder.POST(json != null ? HttpRequest.BodyPublishers.ofString(json) : HttpRequest.BodyPublishers.noBody());
            case "GET" -> builder.GET();
            case "PUT" -> builder.PUT(json != null ? HttpRequest.BodyPublishers.ofString(json) : HttpRequest.BodyPublishers.noBody());
            case "DELETE" -> builder.DELETE();
        }
        return client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }

    private String createList(String name, String token) throws Exception {
        String body = objectMapper.writeValueAsString(Map.of("name", name));
        HttpResponse<String> createResp = send("POST", "/api/v1/tasklists", body, token);

        assertThat(createResp.statusCode()).isEqualTo(201);
        Map<String, Object> created = objectMapper.readValue(createResp.body(), new TypeReference<>() {});
        return (String) created.get("id");
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> getLists(String token) throws Exception {
        HttpResponse<String> resp = send("GET", "/api/v1/tasklists", null, token);
        return objectMapper.readValue(resp.body(), new TypeReference<>() {});
    }

    @Test
    void create_ShouldReturn201_WhenValidRequest() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of("name", "My Tasklist"));
        HttpResponse<String> response = send("POST", "/api/v1/tasklists", body, user1Token);
        assertThat(response.statusCode()).isEqualTo(201);
        Map<String, Object> responseBody = objectMapper.readValue(response.body(), new TypeReference<>() {});
        assertThat(responseBody.get("name")).isEqualTo("My Tasklist");
        assertThat(responseBody.get("id")).isNotNull();
    }

    @Test
    void findAll_ShouldReturnOnlyOwnLists() throws Exception {
        createList("User1 List", user1Token);
        createList("User2 List", user2Token);

        List<Map<String, Object>> user1Lists = getLists(user1Token);
        assertThat(user1Lists).hasSize(1);
        assertThat(user1Lists.get(0).get("name")).isEqualTo("User1 List");

        List<Map<String, Object>> user2Lists = getLists(user2Token);
        assertThat(user2Lists).hasSize(1);
        assertThat(user2Lists.get(0).get("name")).isEqualTo("User2 List");
    }

    @Test
    void update_ShouldReturn200_WhenOwner() throws Exception {
        String id = createList("Original", user1Token);

        HttpResponse<String> response = send("PUT", "/api/v1/tasklists/" + id,
                objectMapper.writeValueAsString(Map.of("name", "Updated")), user1Token);
        assertThat(response.statusCode()).isEqualTo(200);
        Map<String, Object> responseBody = objectMapper.readValue(response.body(), new TypeReference<>() {});
        assertThat(responseBody.get("name")).isEqualTo("Updated");
    }

    @Test
    void delete_ShouldReturn204_WhenOwner() throws Exception {
        String id = createList("To Delete", user1Token);

        HttpResponse<String> response = send("DELETE", "/api/v1/tasklists/" + id, null, user1Token);
        assertThat(response.statusCode()).isEqualTo(204);

        List<Map<String, Object>> remaining = getLists(user1Token);
        assertThat(remaining).isEmpty();
    }

    @Test
    void update_ShouldReturn4xx_WhenNotOwner() throws Exception {
        String id = createList("User1 List", user1Token);

        HttpResponse<String> response = send("PUT", "/api/v1/tasklists/" + id,
                objectMapper.writeValueAsString(Map.of("name", "Hijacked")), user2Token);
        assertThat(response.statusCode()).isGreaterThanOrEqualTo(400).isLessThan(500);
    }

    @Test
    void delete_ShouldSoftDelete_WhenTasklistHasTasks() throws Exception {
        String listId = createList("List With Tasks", user1Token);
        String body = objectMapper.writeValueAsString(Map.of("title", "My Task", "completed", false));
        send("POST", "/api/v1/tasks?tasklistId=" + listId, body, user1Token);

        HttpResponse<String> response = send("DELETE", "/api/v1/tasklists/" + listId, null, user1Token);
        assertThat(response.statusCode()).isEqualTo(204);

        List<Map<String, Object>> remaining = getLists(user1Token);
        assertThat(remaining).isEmpty();

        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM tasklist WHERE id = ? AND deleted_at IS NOT NULL",
                Integer.class, java.util.UUID.fromString(listId));
        assertThat(count).isEqualTo(1);
    }

    @Test
    void getTasks_ShouldReturn400_WhenTasklistIsDeleted() throws Exception {
        String listId = createList("List With Tasks", user1Token);
        String body = objectMapper.writeValueAsString(Map.of("title", "My Task", "completed", false));
        send("POST", "/api/v1/tasks?tasklistId=" + listId, body, user1Token);

        // Delete the list
        send("DELETE", "/api/v1/tasklists/" + listId, null, user1Token);

        // Try to fetch tasks for the deleted list
        HttpResponse<String> tasksResp = send("GET", "/api/v1/tasks?tasklistId=" + listId, null, user1Token);
        assertThat(tasksResp.statusCode()).isEqualTo(400);
    }

    @Test
    void delete_ShouldReturn4xx_WhenNotOwner() throws Exception {
        String id = createList("User1 List", user1Token);

        HttpResponse<String> response = send("DELETE", "/api/v1/tasklists/" + id, null, user2Token);
        assertThat(response.statusCode()).isGreaterThanOrEqualTo(400).isLessThan(500);
    }
}
