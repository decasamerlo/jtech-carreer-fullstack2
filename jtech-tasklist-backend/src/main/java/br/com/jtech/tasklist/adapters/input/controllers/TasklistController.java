package br.com.jtech.tasklist.adapters.input.controllers;

import br.com.jtech.tasklist.adapters.input.protocols.TasklistRequest;
import br.com.jtech.tasklist.adapters.input.protocols.TasklistResponse;
import br.com.jtech.tasklist.application.core.domains.Tasklist;
import br.com.jtech.tasklist.application.ports.input.CreateTasklistInputGateway;
import br.com.jtech.tasklist.application.ports.input.DeleteTasklistInputGateway;
import br.com.jtech.tasklist.application.ports.input.GetTasklistsInputGateway;
import br.com.jtech.tasklist.application.ports.input.UpdateTasklistInputGateway;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;

/**
 * class TasklistController
 *
 * user angelo.vicente
 */
@RestController
@RequestMapping("/api/v1/tasklists")
@RequiredArgsConstructor
public class TasklistController {

    private final CreateTasklistInputGateway createTasklistInputGateway;
    private final GetTasklistsInputGateway getTasklistsInputGateway;
    private final UpdateTasklistInputGateway updateTasklistInputGateway;
    private final DeleteTasklistInputGateway deleteTasklistInputGateway;

    @GetMapping
    public ResponseEntity<List<TasklistResponse>> findAll() {
        String userId = getCurrentUserId();
        List<TasklistResponse> response = getTasklistsInputGateway.findAllByUserId(userId)
                .stream()
                .map(TasklistResponse::of)
                .toList();
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<TasklistResponse> create(@Valid @RequestBody TasklistRequest request) {
        String userId = getCurrentUserId();
        Tasklist tasklist = Tasklist.builder()
                .name(request.getName())
                .userId(userId)
                .build();
        Tasklist created = createTasklistInputGateway.create(tasklist);
        return ResponseEntity.status(201).body(TasklistResponse.of(created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TasklistResponse> update(@PathVariable String id,
                                                   @Valid @RequestBody TasklistRequest request) {
        String userId = getCurrentUserId();
        Tasklist tasklist = Tasklist.builder()
                .id(id)
                .name(request.getName())
                .userId(userId)
                .build();
        Tasklist updated = updateTasklistInputGateway.update(tasklist, userId);
        return ResponseEntity.ok(TasklistResponse.of(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        String userId = getCurrentUserId();
        deleteTasklistInputGateway.delete(id, userId);
        return ResponseEntity.noContent().build();
    }

    private String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }
}
