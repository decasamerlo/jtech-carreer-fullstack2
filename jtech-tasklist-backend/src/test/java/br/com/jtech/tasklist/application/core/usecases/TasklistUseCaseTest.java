package br.com.jtech.tasklist.application.core.usecases;

import br.com.jtech.tasklist.application.core.domains.Tasklist;
import br.com.jtech.tasklist.application.ports.output.CreateTasklistOutputGateway;
import br.com.jtech.tasklist.application.ports.output.DeleteTasklistOutputGateway;
import br.com.jtech.tasklist.application.ports.output.GetTasklistsOutputGateway;
import br.com.jtech.tasklist.application.ports.output.UpdateTasklistOutputGateway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TasklistUseCaseTest {

    @Mock
    private GetTasklistsOutputGateway getTasklistsOutputGateway;

    @Mock
    private UpdateTasklistOutputGateway updateTasklistOutputGateway;

    @Mock
    private DeleteTasklistOutputGateway deleteTasklistOutputGateway;

    @Mock
    private CreateTasklistOutputGateway createTasklistOutputGateway;

    @InjectMocks
    private GetTasklistsUseCase getTasklistsUseCase;

    @InjectMocks
    private UpdateTasklistUseCase updateTasklistsUseCase;

    @InjectMocks
    private DeleteTasklistUseCase deleteTasklistsUseCase;

    @InjectMocks
    private CreateTasklistUseCase createTasklistUseCase;

    @Test
    void findAllByUserId_ShouldReturnOnlyUserLists() {
        UUID userId = UUID.randomUUID();
        Tasklist list1 = Tasklist.builder().id(UUID.randomUUID().toString()).name("List 1").userId(userId.toString()).build();
        Tasklist list2 = Tasklist.builder().id(UUID.randomUUID().toString()).name("List 2").userId(userId.toString()).build();
        when(getTasklistsOutputGateway.findAllByUserId(userId)).thenReturn(List.of(list1, list2));

        List<Tasklist> result = getTasklistsUseCase.findAllByUserId(userId.toString());

        assertThat(result).hasSize(2);
        assertThat(result).extracting(Tasklist::getName).containsExactly("List 1", "List 2");
        verify(getTasklistsOutputGateway).findAllByUserId(userId);
    }

    @Test
    void update_ShouldReturnUpdatedTasklist_WhenOwner() {
        UUID userId = UUID.randomUUID();
        UUID taskId = UUID.randomUUID();
        Tasklist existing = Tasklist.builder().id(taskId.toString()).name("Old").userId(userId.toString()).build();
        Tasklist incoming = Tasklist.builder().id(taskId.toString()).name("New").userId(userId.toString()).build();
        Tasklist updated = Tasklist.builder().id(taskId.toString()).name("New").userId(userId.toString()).build();

        when(getTasklistsOutputGateway.findByIdAndUserId(taskId, userId)).thenReturn(existing);
        when(updateTasklistOutputGateway.update(any(Tasklist.class), eq(userId))).thenReturn(updated);

        Tasklist result = updateTasklistsUseCase.update(incoming, userId.toString());

        assertThat(result.getName()).isEqualTo("New");
        verify(updateTasklistOutputGateway).update(incoming, userId);
    }

    @Test
    void update_ShouldThrow_WhenNotFound() {
        UUID userId = UUID.randomUUID();
        UUID taskId = UUID.randomUUID();
        Tasklist incoming = Tasklist.builder().id(taskId.toString()).name("New").userId(userId.toString()).build();

        when(getTasklistsOutputGateway.findByIdAndUserId(taskId, userId)).thenReturn(null);

        assertThatThrownBy(() -> updateTasklistsUseCase.update(incoming, userId.toString()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not found or access denied");
    }

    @Test
    void delete_ShouldCallOutputGateway_WhenOwner() {
        UUID userId = UUID.randomUUID();
        UUID taskId = UUID.randomUUID();
        Tasklist existing = Tasklist.builder().id(taskId.toString()).name("List").userId(userId.toString()).build();

        when(getTasklistsOutputGateway.findByIdAndUserId(taskId, userId)).thenReturn(existing);

        deleteTasklistsUseCase.delete(taskId.toString(), userId.toString());

        verify(deleteTasklistOutputGateway).delete(taskId.toString(), userId);
    }

    @Test
    void delete_ShouldThrow_WhenNotFound() {
        UUID userId = UUID.randomUUID();
        UUID taskId = UUID.randomUUID();

        when(getTasklistsOutputGateway.findByIdAndUserId(taskId, userId)).thenReturn(null);

        assertThatThrownBy(() -> deleteTasklistsUseCase.delete(taskId.toString(), userId.toString()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not found or access denied");
    }

    @Test
    void create_ShouldThrow_WhenDuplicateName() {
        UUID userId = UUID.randomUUID();
        Tasklist tasklist = Tasklist.builder()
                .name("my list")
                .userId(userId.toString())
                .build();

        when(getTasklistsOutputGateway.existsByUserIdAndName(userId, "my list")).thenReturn(true);

        assertThatThrownBy(() -> createTasklistUseCase.create(tasklist))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("A list with this name already exists");

        verify(createTasklistOutputGateway, never()).create(any());
    }

    @Test
    void create_ShouldPreserveOriginalCasing_WhileCheckingDuplicatesCaseInsensitively() {
        UUID userId = UUID.randomUUID();
        Tasklist tasklist = Tasklist.builder()
                .name("My List")
                .userId(userId.toString())
                .build();

        Tasklist created = Tasklist.builder()
                .id(UUID.randomUUID().toString())
                .name("My List")
                .userId(userId.toString())
                .build();

        when(getTasklistsOutputGateway.existsByUserIdAndName(userId, "My List")).thenReturn(false);
        when(createTasklistOutputGateway.create(any(Tasklist.class))).thenReturn(created);

        Tasklist result = createTasklistUseCase.create(tasklist);

        assertThat(result.getName()).isEqualTo("My List");
    }

    @Test
    void update_ShouldThrow_WhenDuplicateName() {
        UUID userId = UUID.randomUUID();
        UUID taskId = UUID.randomUUID();
        Tasklist existing = Tasklist.builder().id(taskId.toString()).name("old").userId(userId.toString()).build();
        Tasklist incoming = Tasklist.builder().id(taskId.toString()).name("duplicate").userId(userId.toString()).build();

        when(getTasklistsOutputGateway.findByIdAndUserId(taskId, userId)).thenReturn(existing);
        when(getTasklistsOutputGateway.existsByUserIdAndNameAndIdNot(userId, "duplicate", taskId)).thenReturn(true);

        assertThatThrownBy(() -> updateTasklistsUseCase.update(incoming, userId.toString()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("A list with this name already exists");

        verify(updateTasklistOutputGateway, never()).update(any(), any());
    }

    @Test
    void update_ShouldAllowSameName_WhenRenamingToOwnName() {
        UUID userId = UUID.randomUUID();
        UUID taskId = UUID.randomUUID();
        Tasklist existing = Tasklist.builder().id(taskId.toString()).name("My List").userId(userId.toString()).build();
        Tasklist incoming = Tasklist.builder().id(taskId.toString()).name("My List").userId(userId.toString()).build();
        Tasklist updated = Tasklist.builder().id(taskId.toString()).name("My List").userId(userId.toString()).build();

        when(getTasklistsOutputGateway.findByIdAndUserId(taskId, userId)).thenReturn(existing);
        when(getTasklistsOutputGateway.existsByUserIdAndNameAndIdNot(userId, "My List", taskId)).thenReturn(false);
        when(updateTasklistOutputGateway.update(any(Tasklist.class), eq(userId))).thenReturn(updated);

        Tasklist result = updateTasklistsUseCase.update(incoming, userId.toString());

        assertThat(result.getName()).isEqualTo("My List");
        verify(updateTasklistOutputGateway).update(incoming, userId);
    }
}
