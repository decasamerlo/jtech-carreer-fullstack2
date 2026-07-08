package br.com.jtech.tasklist.adapters.input.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.client.RestClient;

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

    private final ObjectMapper objectMapper = new ObjectMapper();
    private RestClient client;
    private String user1Token;
    private String user2Token;
    private String user1TasklistId;

    @BeforeEach
    void setUp() {
        client = RestClient.builder()
                .defaultStatusHandler(status -> true, (request, response) -> {})
                .build();
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

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> readValue(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> readList(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<List<Map<String, Object>>>() {});
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String registerAndLogin(String email, String name) {
        send("POST", "/api/v1/auth/register", toJson(Map.of("name", name, "email", email, "password", "password123")), null);
        ResponseEntity<String> loginResp = send("POST", "/api/v1/auth/login",
                toJson(Map.of("email", email, "password", "password123")), null);
        Map<String, Object> body = readValue(loginResp.getBody());
        return (String) body.get("accessToken");
    }

    private ResponseEntity<String> send(String method, String path, String json, String token) {
        var spec = client.method(HttpMethod.valueOf(method))
                .uri(base() + path)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json");
        if (token != null) spec = spec.header("Authorization", "Bearer " + token);
        var withBody = (json != null) ? spec.body(json) : spec;
        return withBody.retrieve().toEntity(String.class);
    }

    private String createTasklist(String name, String token) {
        ResponseEntity<String> createResp = send("POST", "/api/v1/tasklists", toJson(Map.of("name", name)), token);
        assertThat(createResp.getStatusCode().value()).isEqualTo(201);
        Map<String, Object> created = readValue(createResp.getBody());
        return (String) created.get("id");
    }

    private String createTask(String title, String tasklistId, String token) {
        ResponseEntity<String> createResp = send("POST", "/api/v1/tasks?tasklistId=" + tasklistId, toJson(Map.of("title", title)), token);
        assertThat(createResp.getStatusCode().value()).isEqualTo(201);
        Map<String, Object> created = readValue(createResp.getBody());
        return (String) created.get("id");
    }

    private List<Map<String, Object>> getTasks(String tasklistId, String token) {
        ResponseEntity<String> resp = send("GET", "/api/v1/tasks?tasklistId=" + tasklistId, null, token);
        assertThat(resp.getStatusCode().value()).isEqualTo(200);
        return readList(resp.getBody());
    }

    @Test
    void create_ShouldReturn201_WhenValidRequest() {
        ResponseEntity<String> response = send("POST", "/api/v1/tasks?tasklistId=" + user1TasklistId, toJson(Map.of("title", "Buy groceries")), user1Token);
        assertThat(response.getStatusCode().value()).isEqualTo(201);
        Map<String, Object> responseBody = readValue(response.getBody());
        assertThat(responseBody.get("title")).isEqualTo("Buy groceries");
        assertThat(responseBody.get("id")).isNotNull();
    }

    @Test
    void create_ShouldReturn400_WhenTitleBlank() {
        ResponseEntity<String> response = send("POST", "/api/v1/tasks?tasklistId=" + user1TasklistId, toJson(Map.of("title", "")), user1Token);
        assertThat(response.getStatusCode().value()).isEqualTo(400);
    }

    @Test
    void create_ShouldReturn4xx_WhenTasklistNotOwnedByUser() {
        String user2TasklistId = createTasklist("User2 Tasks", user2Token);
        ResponseEntity<String> response = send("POST", "/api/v1/tasks?tasklistId=" + user2TasklistId, toJson(Map.of("title", "Hijacked Task")), user1Token);
        assertThat(response.getStatusCode().value()).isGreaterThanOrEqualTo(400).isLessThan(500);
    }

    @Test
    void findByTasklistId_ShouldReturnOnlyOwnTasks() {
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
    void findById_ShouldReturnTask_WhenOwnedByUser() {
        String id = createTask("Find Me", user1TasklistId, user1Token);

        ResponseEntity<String> response = send("GET", "/api/v1/tasks/" + id, null, user1Token);
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        Map<String, Object> responseBody = readValue(response.getBody());
        assertThat(responseBody.get("title")).isEqualTo("Find Me");
    }

    @Test
    void findById_ShouldReturn4xx_WhenNotOwnedByUser() {
        String id = createTask("User1 Task", user1TasklistId, user1Token);

        ResponseEntity<String> response = send("GET", "/api/v1/tasks/" + id, null, user2Token);
        assertThat(response.getStatusCode().value()).isGreaterThanOrEqualTo(400).isLessThan(500);
    }

    @Test
    void update_ShouldReturn200_WhenOwner() {
        String id = createTask("Original", user1TasklistId, user1Token);

        ResponseEntity<String> response = send("PUT", "/api/v1/tasks/" + id, toJson(Map.of("title", "Updated")), user1Token);
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        Map<String, Object> responseBody = readValue(response.getBody());
        assertThat(responseBody.get("title")).isEqualTo("Updated");
    }

    @Test
    void update_ShouldPreserveCompleted_WhenTitleOnlyUpdate() {
        String id = createTask("Task", user1TasklistId, user1Token);

        ResponseEntity<String> markComplete = send("PUT", "/api/v1/tasks/" + id, toJson(Map.of("title", "Task", "completed", true)), user1Token);
        assertThat(markComplete.getStatusCode().value()).isEqualTo(200);

        ResponseEntity<String> response = send("PUT", "/api/v1/tasks/" + id, toJson(Map.of("title", "Renamed")), user1Token);
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        Map<String, Object> responseBody = readValue(response.getBody());
        assertThat(responseBody.get("title")).isEqualTo("Renamed");
        assertThat(responseBody.get("completed")).isEqualTo(true);
    }

    @Test
    void update_ShouldPreserveDescription_WhenTitleOnlyUpdate() {
        String createBody = toJson(Map.of("title", "Task", "description", "Original desc"));
        ResponseEntity<String> createResp = send("POST", "/api/v1/tasks?tasklistId=" + user1TasklistId, createBody, user1Token);
        assertThat(createResp.getStatusCode().value()).isEqualTo(201);
        Map<String, Object> created = readValue(createResp.getBody());
        String id = (String) created.get("id");

        ResponseEntity<String> response = send("PUT", "/api/v1/tasks/" + id, toJson(Map.of("title", "Renamed")), user1Token);
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        Map<String, Object> responseBody = readValue(response.getBody());
        assertThat(responseBody.get("title")).isEqualTo("Renamed");
        assertThat(responseBody.get("description")).isEqualTo("Original desc");
    }

    @Test
    void delete_ShouldReturn204_WhenOwner() {
        String id = createTask("To Delete", user1TasklistId, user1Token);

        ResponseEntity<String> response = send("DELETE", "/api/v1/tasks/" + id, null, user1Token);
        assertThat(response.getStatusCode().value()).isEqualTo(204);

        List<Map<String, Object>> remaining = getTasks(user1TasklistId, user1Token);
        assertThat(remaining).isEmpty();
    }

    @Test
    void update_ShouldReturn4xx_WhenNotOwner() {
        String id = createTask("User1 Task", user1TasklistId, user1Token);

        ResponseEntity<String> response = send("PUT", "/api/v1/tasks/" + id, toJson(Map.of("title", "Hijacked")), user2Token);
        assertThat(response.getStatusCode().value()).isGreaterThanOrEqualTo(400).isLessThan(500);
    }

    @Test
    void delete_ShouldReturn4xx_WhenNotOwner() {
        String id = createTask("User1 Task", user1TasklistId, user1Token);

        ResponseEntity<String> response = send("DELETE", "/api/v1/tasks/" + id, null, user2Token);
        assertThat(response.getStatusCode().value()).isGreaterThanOrEqualTo(400).isLessThan(500);
    }

    @Test
    void update_ShouldReturn400_WhenTasklistIsDeleted() {
        String listId = createTasklist("List With Tasks", user1Token);
        String taskId = createTask("My Task", listId, user1Token);

        send("DELETE", "/api/v1/tasklists/" + listId, null, user1Token);

        ResponseEntity<String> response = send("PUT", "/api/v1/tasks/" + taskId, toJson(Map.of("title", "Updated")), user1Token);
        assertThat(response.getStatusCode().value()).isEqualTo(400);
    }

    @Test
    void delete_ShouldReturn400_WhenTasklistIsDeleted() {
        String listId = createTasklist("List With Tasks", user1Token);
        String taskId = createTask("My Task", listId, user1Token);

        send("DELETE", "/api/v1/tasklists/" + listId, null, user1Token);

        ResponseEntity<String> response = send("DELETE", "/api/v1/tasks/" + taskId, null, user1Token);
        assertThat(response.getStatusCode().value()).isEqualTo(400);
    }

    @Test
    void create_ShouldReturn400_WhenDuplicateTitleExactCase() {
        createTask("Buy Milk", user1TasklistId, user1Token);

        ResponseEntity<String> response = send("POST", "/api/v1/tasks?tasklistId=" + user1TasklistId, toJson(Map.of("title", "Buy Milk")), user1Token);
        assertThat(response.getStatusCode().value()).isEqualTo(400);
    }

    @Test
    void create_ShouldReturn400_WhenDuplicateTitleDifferentCase() {
        createTask("Buy Milk", user1TasklistId, user1Token);

        ResponseEntity<String> response = send("POST", "/api/v1/tasks?tasklistId=" + user1TasklistId, toJson(Map.of("title", "buy milk")), user1Token);
        assertThat(response.getStatusCode().value()).isEqualTo(400);
    }

    @Test
    void update_ShouldReturn400_WhenDuplicateTitleDifferentCase() {
        createTask("Buy Milk", user1TasklistId, user1Token);
        String otherId = createTask("Walk Dog", user1TasklistId, user1Token);

        ResponseEntity<String> response = send("PUT", "/api/v1/tasks/" + otherId, toJson(Map.of("title", "buy milk")), user1Token);
        assertThat(response.getStatusCode().value()).isEqualTo(400);
    }

    @Test
    void create_ShouldSucceed_WhenSameTitleInDifferentTasklist() {
        createTask("Buy Milk", user1TasklistId, user1Token);

        String otherListId = createTasklist("Other List", user1Token);
        ResponseEntity<String> response = send("POST", "/api/v1/tasks?tasklistId=" + otherListId, toJson(Map.of("title", "Buy Milk")), user1Token);
        assertThat(response.getStatusCode().value()).isEqualTo(201);
    }

    @Test
    void create_ShouldSucceed_WhenTitleReusedAfterSoftDelete() {
        String taskId = createTask("Buy Milk", user1TasklistId, user1Token);
        send("DELETE", "/api/v1/tasks/" + taskId, null, user1Token);

        ResponseEntity<String> response = send("POST", "/api/v1/tasks?tasklistId=" + user1TasklistId, toJson(Map.of("title", "Buy Milk")), user1Token);
        assertThat(response.getStatusCode().value()).isEqualTo(201);
    }
}
