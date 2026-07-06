package br.com.jtech.tasklist.adapters.output;

import br.com.jtech.tasklist.application.core.domains.Tasklist;
import br.com.jtech.tasklist.application.ports.output.CreateTasklistOutputGateway;
import br.com.jtech.tasklist.application.ports.output.DeleteTasklistOutputGateway;
import br.com.jtech.tasklist.application.ports.output.GetTasklistsOutputGateway;
import br.com.jtech.tasklist.application.ports.output.UpdateTasklistOutputGateway;
import br.com.jtech.tasklist.adapters.output.repositories.TasklistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TasklistAdapter implements CreateTasklistOutputGateway,
        GetTasklistsOutputGateway, UpdateTasklistOutputGateway, DeleteTasklistOutputGateway {

    private final TasklistRepository repository;

    @Override
    public Tasklist create(Tasklist tasklist) {
        var entity = TasklistMapper.toEntity(tasklist);
        var saved = repository.save(entity);
        return TasklistMapper.toDomain(saved);
    }

    @Override
    public List<Tasklist> findAllByUserId(UUID userId) {
        return repository.findAllByUserId(userId).stream()
                .map(TasklistMapper::toDomain)
                .toList();
    }

    @Override
    public Tasklist findByIdAndUserId(UUID id, UUID userId) {
        return repository.findByIdAndUserId(id, userId)
                .map(TasklistMapper::toDomain)
                .orElse(null);
    }

    @Override
    public Tasklist update(Tasklist tasklist, UUID userId) {
        var existing = repository.findByIdAndUserId(UUID.fromString(tasklist.getId()), userId)
                .orElseThrow(() -> new IllegalArgumentException("Tasklist not found"));
        existing.setName(tasklist.getName());
        var saved = repository.save(existing);
        return TasklistMapper.toDomain(saved);
    }

    @Override
    public void delete(String id, UUID userId) {
        repository.deleteByIdAndUserId(UUID.fromString(id), userId);
    }
}
