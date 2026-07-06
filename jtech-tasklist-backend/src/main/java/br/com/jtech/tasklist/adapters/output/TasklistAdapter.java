package br.com.jtech.tasklist.adapters.output;

import br.com.jtech.tasklist.application.core.domains.Tasklist;
import br.com.jtech.tasklist.application.ports.output.CreateTasklistOutputGateway;
import br.com.jtech.tasklist.application.ports.output.DeleteTasklistOutputGateway;
import br.com.jtech.tasklist.application.ports.output.GetTasklistsOutputGateway;
import br.com.jtech.tasklist.application.ports.output.UpdateTasklistOutputGateway;
import br.com.jtech.tasklist.adapters.output.repositories.TasklistRepository;
import br.com.jtech.tasklist.adapters.output.repositories.entities.TasklistEntity;
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
        TasklistEntity entity = tasklist.toEntity();
        TasklistEntity saved = repository.save(entity);
        return Tasklist.of(saved);
    }

    @Override
    public List<Tasklist> findAllByUserId(UUID userId) {
        return repository.findAllByUserId(userId).stream()
                .map(Tasklist::of)
                .toList();
    }

    @Override
    public Tasklist findByIdAndUserId(UUID id, UUID userId) {
        return repository.findByIdAndUserId(id, userId)
                .map(Tasklist::of)
                .orElse(null);
    }

    @Override
    public Tasklist update(Tasklist tasklist) {
        TasklistEntity existing = repository.findById(UUID.fromString(tasklist.getId()))
                .orElseThrow(() -> new IllegalArgumentException("Tasklist not found"));
        existing.setName(tasklist.getName());
        if (tasklist.getUserId() != null) {
            existing.setUserId(UUID.fromString(tasklist.getUserId()));
        }
        TasklistEntity saved = repository.save(existing);
        return Tasklist.of(saved);
    }

    @Override
    public void delete(String id) {
        repository.deleteById(UUID.fromString(id));
    }
}
