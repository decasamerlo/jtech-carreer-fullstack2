package br.com.jtech.tasklist.adapters.input.controllers;

import br.com.jtech.tasklist.adapters.input.protocols.TaskRequest;
import br.com.jtech.tasklist.adapters.input.protocols.TaskResponse;
import br.com.jtech.tasklist.application.core.domains.Task;
import br.com.jtech.tasklist.application.ports.input.CreateTaskInputGateway;
import br.com.jtech.tasklist.application.ports.input.DeleteTaskInputGateway;
import br.com.jtech.tasklist.application.ports.input.GetTasksInputGateway;
import br.com.jtech.tasklist.application.ports.input.UpdateTaskInputGateway;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final CreateTaskInputGateway createTaskInputGateway;
    private final GetTasksInputGateway getTasksInputGateway;
    private final UpdateTaskInputGateway updateTaskInputGateway;
    private final DeleteTaskInputGateway deleteTaskInputGateway;

    @GetMapping
    public ResponseEntity<List<TaskResponse>> findByTasklistId(
            @RequestParam String tasklistId) {
        String userId = getCurrentUserId();
        List<TaskResponse> response = getTasksInputGateway.findByTasklistIdAndUserId(tasklistId, userId)
                .stream()
                .map(TaskResponse::of)
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskResponse> findById(@PathVariable String id) {
        String userId = getCurrentUserId();
        Task task = getTasksInputGateway.findByIdAndUserId(id, userId);
        if (task == null) {
            throw new IllegalArgumentException("Task not found or access denied");
        }
        return ResponseEntity.ok(TaskResponse.of(task));
    }

    @PostMapping
    public ResponseEntity<TaskResponse> create(@Valid @RequestBody TaskRequest request,
                                               @RequestParam String tasklistId) {
        String userId = getCurrentUserId();
        Task task = Task.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .completed(request.getCompleted())
                .tasklistId(tasklistId)
                .userId(userId)
                .build();
        Task created = createTaskInputGateway.create(task);
        return ResponseEntity.status(201).body(TaskResponse.of(created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaskResponse> update(@PathVariable String id,
                                               @Valid @RequestBody TaskRequest request) {
        String userId = getCurrentUserId();
        Task task = Task.builder()
                .id(id)
                .title(request.getTitle())
                .description(request.getDescription())
                .completed(request.getCompleted())
                .userId(userId)
                .build();
        Task updated = updateTaskInputGateway.update(task, userId);
        return ResponseEntity.ok(TaskResponse.of(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        String userId = getCurrentUserId();
        deleteTaskInputGateway.delete(id, userId);
        return ResponseEntity.noContent().build();
    }

    private String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }
}
