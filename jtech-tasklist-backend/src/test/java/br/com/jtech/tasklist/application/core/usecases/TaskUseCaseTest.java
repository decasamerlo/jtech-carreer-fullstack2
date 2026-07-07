package br.com.jtech.tasklist.application.core.usecases;

import br.com.jtech.tasklist.application.core.domains.Task;
import br.com.jtech.tasklist.application.core.domains.Tasklist;
import br.com.jtech.tasklist.application.ports.output.CreateTaskOutputGateway;
import br.com.jtech.tasklist.application.ports.output.DeleteTaskOutputGateway;
import br.com.jtech.tasklist.application.ports.output.GetTasksOutputGateway;
import br.com.jtech.tasklist.application.ports.output.GetTasklistsOutputGateway;
import br.com.jtech.tasklist.application.ports.output.UpdateTaskOutputGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class TaskUseCaseTest {

    private CreateTaskOutputGateway createTaskOutputGateway;
    private GetTasksOutputGateway getTasksOutputGateway;
    private GetTasklistsOutputGateway getTasklistsOutputGateway;
    private UpdateTaskOutputGateway updateTaskOutputGateway;
    private DeleteTaskOutputGateway deleteTaskOutputGateway;

    private CreateTaskUseCase createTaskUseCase;
    private GetTasksUseCase getTasksUseCase;
    private UpdateTaskUseCase updateTaskUseCase;
    private DeleteTaskUseCase deleteTaskUseCase;

    private final UUID userId = UUID.randomUUID();
    private final UUID tasklistId = UUID.randomUUID();
    private final UUID taskId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        createTaskOutputGateway = mock(CreateTaskOutputGateway.class);
        getTasksOutputGateway = mock(GetTasksOutputGateway.class);
        getTasklistsOutputGateway = mock(GetTasklistsOutputGateway.class);
        updateTaskOutputGateway = mock(UpdateTaskOutputGateway.class);
        deleteTaskOutputGateway = mock(DeleteTaskOutputGateway.class);

        createTaskUseCase = new CreateTaskUseCase(createTaskOutputGateway, getTasklistsOutputGateway);
        getTasksUseCase = new GetTasksUseCase(getTasksOutputGateway);
        updateTaskUseCase = new UpdateTaskUseCase(updateTaskOutputGateway, getTasksOutputGateway);
        deleteTaskUseCase = new DeleteTaskUseCase(deleteTaskOutputGateway, getTasksOutputGateway);
    }

    @Test
    void create_ShouldValidateTasklistOwnership() {
        Task task = Task.builder()
                .title("Test Task")
                .tasklistId(tasklistId.toString())
                .userId(userId.toString())
                .build();

        when(getTasklistsOutputGateway.findByIdAndUserId(tasklistId, userId)).thenReturn(null);

        assertThatThrownBy(() -> createTaskUseCase.create(task))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Tasklist not found or access denied");

        verify(createTaskOutputGateway, never()).create(any());
    }

    @Test
    void create_ShouldCreateTask_WhenTasklistOwnedByUser() {
        Task task = Task.builder()
                .title("Test Task")
                .tasklistId(tasklistId.toString())
                .userId(userId.toString())
                .build();

        Tasklist tasklist = Tasklist.builder()
                .id(tasklistId.toString())
                .userId(userId.toString())
                .build();

        Task createdTask = Task.builder()
                .id(taskId.toString())
                .title("Test Task")
                .tasklistId(tasklistId.toString())
                .userId(userId.toString())
                .build();

        when(getTasklistsOutputGateway.findByIdAndUserId(tasklistId, userId)).thenReturn(tasklist);
        when(createTaskOutputGateway.create(task)).thenReturn(createdTask);

        Task result = createTaskUseCase.create(task);

        assertThat(result).isEqualTo(createdTask);
        verify(createTaskOutputGateway).create(task);
    }

    @Test
    void findByTasklistIdAndUserId_ShouldDelegateToOutputGateway() {
        Task task = Task.builder()
                .id(taskId.toString())
                .title("Test Task")
                .tasklistId(tasklistId.toString())
                .userId(userId.toString())
                .build();

        when(getTasksOutputGateway.findByTasklistIdAndUserId(tasklistId, userId))
                .thenReturn(List.of(task));

        List<Task> result = getTasksUseCase.findByTasklistIdAndUserId(
                tasklistId.toString(), userId.toString());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Test Task");
    }

    @Test
    void findByIdAndUserId_ShouldReturnNull_WhenNotFound() {
        when(getTasksOutputGateway.findByIdAndUserId(taskId, userId)).thenReturn(null);

        Task result = getTasksUseCase.findByIdAndUserId(taskId.toString(), userId.toString());

        assertThat(result).isNull();
    }

    @Test
    void update_ShouldThrow_WhenTaskNotFound() {
        Task task = Task.builder()
                .id(taskId.toString())
                .title("Updated")
                .userId(userId.toString())
                .build();

        when(getTasksOutputGateway.findByIdAndUserId(taskId, userId)).thenReturn(null);

        assertThatThrownBy(() -> updateTaskUseCase.update(task, userId.toString()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Task not found or access denied");

        verify(updateTaskOutputGateway, never()).update(any(), any());
    }

    @Test
    void update_ShouldUpdateTask_WhenOwnedByUser() {
        Task task = Task.builder()
                .id(taskId.toString())
                .title("Updated")
                .userId(userId.toString())
                .build();

        Task existing = Task.builder()
                .id(taskId.toString())
                .title("Original")
                .userId(userId.toString())
                .build();

        Task updated = Task.builder()
                .id(taskId.toString())
                .title("Updated")
                .userId(userId.toString())
                .build();

        when(getTasksOutputGateway.findByIdAndUserId(taskId, userId)).thenReturn(existing);
        when(updateTaskOutputGateway.update(task, userId)).thenReturn(updated);

        Task result = updateTaskUseCase.update(task, userId.toString());

        assertThat(result.getTitle()).isEqualTo("Updated");
        verify(updateTaskOutputGateway).update(task, userId);
    }

    @Test
    void delete_ShouldThrow_WhenTaskNotFound() {
        when(getTasksOutputGateway.findByIdAndUserId(taskId, userId)).thenReturn(null);

        assertThatThrownBy(() -> deleteTaskUseCase.delete(taskId.toString(), userId.toString()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Task not found or access denied");

        verify(deleteTaskOutputGateway, never()).delete(any(), any());
    }

    @Test
    void delete_ShouldDeleteTask_WhenOwnedByUser() {
        Task existing = Task.builder()
                .id(taskId.toString())
                .title("To Delete")
                .userId(userId.toString())
                .build();

        when(getTasksOutputGateway.findByIdAndUserId(taskId, userId)).thenReturn(existing);

        deleteTaskUseCase.delete(taskId.toString(), userId.toString());

        verify(deleteTaskOutputGateway).delete(taskId.toString(), userId);
    }
}
