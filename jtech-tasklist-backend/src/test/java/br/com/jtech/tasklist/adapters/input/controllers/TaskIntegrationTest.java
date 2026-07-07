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
class TaskIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private String user1Token;
    private String user2Token;
    private String user1TasklistId;

    @BeforeEach
    void setUp() throws Exception {
        jdbcTemplate.execute("DELETE FROM task");
        jdbcTemplate.execute("DELETE FROM tasklist");
        jdbcTemplate.execute("DELETE FROM refresh_tokens");
        jdbcTemplate.execute("DELETE FROM users");
        user1Token = registerAndLogin("user1@example.com", "User One");
        user2Token = registerAndLogin("user2@example.com", "User Two");
        user1TasklistId = createTasklist("My Tasks", user1Token);
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

    private String createTasklist(String name, String token) throws Exception {
        String body = objectMapper.writeValueAsString(Map.of("name", name));
        HttpResponse<String> createResp = send("POST", "/api/v1/tasklists", body, token);
        assertThat(createResp.statusCode()).isEqualTo(201);
        Map<String, Object> created = objectMapper.readValue(createResp.body(), new TypeReference<>() {});
        return (String) created.get("id");
    }

    private String createTask(String title, String tasklistId, String token) throws Exception {
        String body = objectMapper.writeValueAsString(Map.of("title", title));
        HttpResponse<String> createResp = send("POST", "/api/v1/tasks?tasklistId=" + tasklistId, body, token);
        assertThat(createResp.statusCode()).isEqualTo(201);
        Map<String, Object> created = objectMapper.readValue(createResp.body(), new TypeReference<>() {});
        return (String) created.get("id");
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> getTasks(String tasklistId, String token) throws Exception {
        HttpResponse<String> resp = send("GET", "/api/v1/tasks?tasklistId=" + tasklistId, null, token);
        assertThat(resp.statusCode()).isEqualTo(200);
        return objectMapper.readValue(resp.body(), new TypeReference<>() {});
    }

    @Test
    void create_ShouldReturn201_WhenValidRequest() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of("title", "Buy groceries"));
        HttpResponse<String> response = send("POST", "/api/v1/tasks?tasklistId=" + user1TasklistId, body, user1Token);
        assertThat(response.statusCode()).isEqualTo(201);
        Map<String, Object> responseBody = objectMapper.readValue(response.body(), new TypeReference<>() {});
        assertThat(responseBody.get("title")).isEqualTo("Buy groceries");
        assertThat(responseBody.get("id")).isNotNull();
    }

    @Test
    void create_ShouldReturn400_WhenTitleBlank() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of("title", ""));
        HttpResponse<String> response = send("POST", "/api/v1/tasks?tasklistId=" + user1TasklistId, body, user1Token);
        assertThat(response.statusCode()).isEqualTo(400);
    }

    @Test
    void create_ShouldReturn4xx_WhenTasklistNotOwnedByUser() throws Exception {
        String user2TasklistId = createTasklist("User2 Tasks", user2Token);
        String body = objectMapper.writeValueAsString(Map.of("title", "Hijacked Task"));
        HttpResponse<String> response = send("POST", "/api/v1/tasks?tasklistId=" + user2TasklistId, body, user1Token);
        assertThat(response.statusCode()).isGreaterThanOrEqualTo(400).isLessThan(500);
    }

    @Test
    void findByTasklistId_ShouldReturnOnlyOwnTasks() throws Exception {
        createTask("User1 Task", user1TasklistId, user1Token);

        String user2TasklistId = createTasklist("User2 Tasks", user2Token);
        createTask("User2 Task", user2TasklistId, user2Token);

        List<Map<String, Object>> user1Tasks = getTasks(user1TasklistId, user1Token);
        assertThat(user1Tasks).hasSize(1);
        assertThat(user1Tasks.get(0).get("title")).isEqualTo("User1 Task");

        List<Map<String, Object>> user2Tasks = getTasks(user2TasklistId, user2Token);
        assertThat(user2Tasks).hasSize(1);
        assertThat(user2Tasks.get(0).get("title")).isEqualTo("User2 Task");
    }

    @Test
    void findById_ShouldReturnTask_WhenOwnedByUser() throws Exception {
        String id = createTask("Find Me", user1TasklistId, user1Token);

        HttpResponse<String> response = send("GET", "/api/v1/tasks/" + id, null, user1Token);
        assertThat(response.statusCode()).isEqualTo(200);
        Map<String, Object> responseBody = objectMapper.readValue(response.body(), new TypeReference<>() {});
        assertThat(responseBody.get("title")).isEqualTo("Find Me");
    }

    @Test
    void findById_ShouldReturn4xx_WhenNotOwnedByUser() throws Exception {
        String id = createTask("User1 Task", user1TasklistId, user1Token);

        HttpResponse<String> response = send("GET", "/api/v1/tasks/" + id, null, user2Token);
        assertThat(response.statusCode()).isGreaterThanOrEqualTo(400).isLessThan(500);
    }

    @Test
    void update_ShouldReturn200_WhenOwner() throws Exception {
        String id = createTask("Original", user1TasklistId, user1Token);

        HttpResponse<String> response = send("PUT", "/api/v1/tasks/" + id,
                objectMapper.writeValueAsString(Map.of("title", "Updated")), user1Token);
        assertThat(response.statusCode()).isEqualTo(200);
        Map<String, Object> responseBody = objectMapper.readValue(response.body(), new TypeReference<>() {});
        assertThat(responseBody.get("title")).isEqualTo("Updated");
    }

    @Test
    void update_ShouldPreserveCompleted_WhenTitleOnlyUpdate() throws Exception {
        String id = createTask("Task", user1TasklistId, user1Token);

        HttpResponse<String> markComplete = send("PUT", "/api/v1/tasks/" + id,
                objectMapper.writeValueAsString(Map.of("title", "Task", "completed", true)), user1Token);
        assertThat(markComplete.statusCode()).isEqualTo(200);

        HttpResponse<String> response = send("PUT", "/api/v1/tasks/" + id,
                objectMapper.writeValueAsString(Map.of("title", "Renamed")), user1Token);
        assertThat(response.statusCode()).isEqualTo(200);
        Map<String, Object> responseBody = objectMapper.readValue(response.body(), new TypeReference<>() {});
        assertThat(responseBody.get("title")).isEqualTo("Renamed");
        assertThat(responseBody.get("completed")).isEqualTo(true);
    }

    @Test
    void update_ShouldPreserveDescription_WhenTitleOnlyUpdate() throws Exception {
        String createBody = objectMapper.writeValueAsString(Map.of("title", "Task", "description", "Original desc"));
        HttpResponse<String> createResp = send("POST", "/api/v1/tasks?tasklistId=" + user1TasklistId, createBody, user1Token);
        assertThat(createResp.statusCode()).isEqualTo(201);
        Map<String, Object> created = objectMapper.readValue(createResp.body(), new TypeReference<>() {});
        String id = (String) created.get("id");

        HttpResponse<String> response = send("PUT", "/api/v1/tasks/" + id,
                objectMapper.writeValueAsString(Map.of("title", "Renamed")), user1Token);
        assertThat(response.statusCode()).isEqualTo(200);
        Map<String, Object> responseBody = objectMapper.readValue(response.body(), new TypeReference<>() {});
        assertThat(responseBody.get("title")).isEqualTo("Renamed");
        assertThat(responseBody.get("description")).isEqualTo("Original desc");
    }

    @Test
    void delete_ShouldReturn204_WhenOwner() throws Exception {
        String id = createTask("To Delete", user1TasklistId, user1Token);

        HttpResponse<String> response = send("DELETE", "/api/v1/tasks/" + id, null, user1Token);
        assertThat(response.statusCode()).isEqualTo(204);

        List<Map<String, Object>> remaining = getTasks(user1TasklistId, user1Token);
        assertThat(remaining).isEmpty();
    }

    @Test
    void update_ShouldReturn4xx_WhenNotOwner() throws Exception {
        String id = createTask("User1 Task", user1TasklistId, user1Token);

        HttpResponse<String> response = send("PUT", "/api/v1/tasks/" + id,
                objectMapper.writeValueAsString(Map.of("title", "Hijacked")), user2Token);
        assertThat(response.statusCode()).isGreaterThanOrEqualTo(400).isLessThan(500);
    }

    @Test
    void delete_ShouldReturn4xx_WhenNotOwner() throws Exception {
        String id = createTask("User1 Task", user1TasklistId, user1Token);

        HttpResponse<String> response = send("DELETE", "/api/v1/tasks/" + id, null, user2Token);
        assertThat(response.statusCode()).isGreaterThanOrEqualTo(400).isLessThan(500);
    }
}
