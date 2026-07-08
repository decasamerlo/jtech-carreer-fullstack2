package br.com.jtech.tasklist.adapters.input.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
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
class TasklistIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private RestClient client;
    private String user1Token;
    private String user2Token;

    @BeforeEach
    void setUp() {
        client = RestClient.builder()
                .defaultStatusHandler(status -> true, (request, response) -> {})
                .build();
        jdbcTemplate.execute("DELETE FROM tasklist");
        jdbcTemplate.execute("DELETE FROM refresh_tokens");
        jdbcTemplate.execute("DELETE FROM users");
        user1Token = registerAndLogin("user1@example.com", "User One");
        user2Token = registerAndLogin("user2@example.com", "User Two");
    }

    private String base() {
        return "http://localhost:" + port;
    }

    private String registerAndLogin(String email, String name) {
        send("POST", "/api/v1/auth/register", toJson(Map.of("name", name, "email", email, "password", "password123")), null);
        ResponseEntity<String> loginResp = send("POST", "/api/v1/auth/login",
                toJson(Map.of("email", email, "password", "password123")), null);
        Map<String, Object> body = readValue(loginResp.getBody());
        return (String) body.get("accessToken");
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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

    private String createList(String name, String token) {
        ResponseEntity<String> createResp = send("POST", "/api/v1/tasklists",
                toJson(Map.of("name", name)), token);

        assertThat(createResp.getStatusCode().value()).isEqualTo(201);
        Map<String, Object> created = readValue(createResp.getBody());
        return (String) created.get("id");
    }

    private List<Map<String, Object>> getLists(String token) {
        ResponseEntity<String> resp = send("GET", "/api/v1/tasklists", null, token);
        return readList(resp.getBody());
    }

    @Test
    void create_ShouldReturn201_WhenValidRequest() {
        ResponseEntity<String> response = send("POST", "/api/v1/tasklists",
                toJson(Map.of("name", "My Tasklist")), user1Token);
        assertThat(response.getStatusCode().value()).isEqualTo(201);
        Map<String, Object> responseBody = readValue(response.getBody());
        assertThat(responseBody.get("name")).isEqualTo("My Tasklist");
        assertThat(responseBody.get("id")).isNotNull();
    }

    @Test
    void findAll_ShouldReturnOnlyOwnLists() {
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
    void update_ShouldReturn200_WhenOwner() {
        String id = createList("Original", user1Token);

        ResponseEntity<String> response = send("PUT", "/api/v1/tasklists/" + id,
                toJson(Map.of("name", "Updated")), user1Token);
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        Map<String, Object> responseBody = readValue(response.getBody());
        assertThat(responseBody.get("name")).isEqualTo("Updated");
    }

    @Test
    void delete_ShouldReturn204_WhenOwner() {
        String id = createList("To Delete", user1Token);

        ResponseEntity<String> response = send("DELETE", "/api/v1/tasklists/" + id, null, user1Token);
        assertThat(response.getStatusCode().value()).isEqualTo(204);

        List<Map<String, Object>> remaining = getLists(user1Token);
        assertThat(remaining).isEmpty();
    }

    @Test
    void update_ShouldReturn4xx_WhenNotOwner() {
        String id = createList("User1 List", user1Token);

        ResponseEntity<String> response = send("PUT", "/api/v1/tasklists/" + id,
                toJson(Map.of("name", "Hijacked")), user2Token);
        assertThat(response.getStatusCode().value()).isGreaterThanOrEqualTo(400).isLessThan(500);
    }

    @Test
    void delete_ShouldSoftDelete_WhenTasklistHasTasks() {
        String listId = createList("List With Tasks", user1Token);
        String taskBody = toJson(Map.of("title", "My Task", "completed", false));
        send("POST", "/api/v1/tasks?tasklistId=" + listId, taskBody, user1Token);

        ResponseEntity<String> response = send("DELETE", "/api/v1/tasklists/" + listId, null, user1Token);
        assertThat(response.getStatusCode().value()).isEqualTo(204);

        List<Map<String, Object>> remaining = getLists(user1Token);
        assertThat(remaining).isEmpty();

        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM tasklist WHERE id = ? AND deleted_at IS NOT NULL",
                Integer.class, java.util.UUID.fromString(listId));
        assertThat(count).isEqualTo(1);
    }

    @Test
    void getTasks_ShouldReturn400_WhenTasklistIsDeleted() {
        String listId = createList("List With Tasks", user1Token);
        String taskBody = toJson(Map.of("title", "My Task", "completed", false));
        send("POST", "/api/v1/tasks?tasklistId=" + listId, taskBody, user1Token);

        // Delete the list
        send("DELETE", "/api/v1/tasklists/" + listId, null, user1Token);

        // Try to fetch tasks for the deleted list
        ResponseEntity<String> tasksResp = send("GET", "/api/v1/tasks?tasklistId=" + listId, null, user1Token);
        assertThat(tasksResp.getStatusCode().value()).isEqualTo(400);
    }

    @Test
    void delete_ShouldReturn4xx_WhenNotOwner() {
        String id = createList("User1 List", user1Token);

        ResponseEntity<String> response = send("DELETE", "/api/v1/tasklists/" + id, null, user2Token);
        assertThat(response.getStatusCode().value()).isGreaterThanOrEqualTo(400).isLessThan(500);
    }

    @Test
    void create_ShouldReturn400_WhenDuplicateName() {
        createList("Unique Name", user1Token);

        ResponseEntity<String> response = send("POST", "/api/v1/tasklists",
                toJson(Map.of("name", "Unique Name")), user1Token);
        assertThat(response.getStatusCode().value()).isEqualTo(400);
    }

    @Test
    void create_ShouldReturn400_WhenCaseInsensitiveDuplicate() {
        createList("My List", user1Token);

        ResponseEntity<String> response = send("POST", "/api/v1/tasklists",
                toJson(Map.of("name", "my list")), user1Token);
        assertThat(response.getStatusCode().value()).isEqualTo(400);
    }

    @Disabled("H2 cannot express partial unique indexes (WHERE deleted_at IS NULL). " +
              "Reusing a name after soft-delete works in production (PostgreSQL via V008's partial index) " +
              "but H2's @UniqueConstraint from the entity enforces uniqueness over all rows including soft-deleted ones. " +
              "See plan Task 5 note for details.")
    @Test
    void create_ShouldAllowSameName_AfterSoftDelete() {
        String id = createList("Reusable Name", user1Token);
        send("DELETE", "/api/v1/tasklists/" + id, null, user1Token);

        ResponseEntity<String> response = send("POST", "/api/v1/tasklists",
                toJson(Map.of("name", "Reusable Name")), user1Token);
        assertThat(response.getStatusCode().value()).isEqualTo(201);
    }

    @Test
    void update_ShouldReturn400_WhenRenamingToExistingName() {
        createList("First List", user1Token);
        String id = createList("Second List", user1Token);

        ResponseEntity<String> response = send("PUT", "/api/v1/tasklists/" + id,
                toJson(Map.of("name", "First List")), user1Token);
        assertThat(response.getStatusCode().value()).isEqualTo(400);
    }
}
