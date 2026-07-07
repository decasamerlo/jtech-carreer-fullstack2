package br.com.jtech.tasklist.adapters.output;

import br.com.jtech.tasklist.application.core.domains.Task;
import br.com.jtech.tasklist.application.ports.output.CreateTaskOutputGateway;
import br.com.jtech.tasklist.application.ports.output.DeleteTaskOutputGateway;
import br.com.jtech.tasklist.application.ports.output.GetTasksOutputGateway;
import br.com.jtech.tasklist.application.ports.output.UpdateTaskOutputGateway;
import br.com.jtech.tasklist.adapters.output.repositories.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TaskAdapter implements CreateTaskOutputGateway,
        GetTasksOutputGateway, UpdateTaskOutputGateway, DeleteTaskOutputGateway {

    private final TaskRepository repository;

    @Override
    public Task create(Task task) {
        var entity = TaskMapper.toEntity(task);
        var saved = repository.save(entity);
        return TaskMapper.toDomain(saved);
    }

    @Override
    public List<Task> findByTasklistIdAndUserId(UUID tasklistId, UUID userId) {
        return repository.findByTasklistIdAndUserId(tasklistId, userId).stream()
                .map(TaskMapper::toDomain)
                .toList();
    }

    @Override
    public Task findByIdAndUserId(UUID id, UUID userId) {
        return repository.findByIdAndUserId(id, userId)
                .map(TaskMapper::toDomain)
                .orElse(null);
    }

    @Override
    public boolean existsByTasklistIdAndTitle(UUID tasklistId, String title) {
        return repository.existsByTasklistIdAndTitle(tasklistId, title);
    }

    @Override
    public boolean existsByTasklistIdAndTitleAndIdNot(UUID tasklistId, String title, UUID excludeId) {
        return repository.existsByTasklistIdAndTitleAndIdNot(tasklistId, title, excludeId);
    }

    @Override
    public Task update(Task task, UUID userId) {
        var existing = repository.findByIdAndUserId(UUID.fromString(task.getId()), userId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found"));
        existing.setTitle(task.getTitle());
        if (task.getDescription() != null) {
            existing.setDescription(task.getDescription());
        }
        if (task.getCompleted() != null) {
            existing.setCompleted(task.getCompleted());
        }
        var saved = repository.save(existing);
        return TaskMapper.toDomain(saved);
    }

    @Override
    public void delete(String id, UUID userId) {
        var entity = repository.findByIdAndUserId(UUID.fromString(id), userId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found"));
        entity.markAsDeleted(userId);
        repository.save(entity);
    }
}
