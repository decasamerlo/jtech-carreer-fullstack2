package br.com.jtech.tasklist.config.usecases;

import br.com.jtech.tasklist.adapters.output.TaskAdapter;
import br.com.jtech.tasklist.adapters.output.TasklistAdapter;
import br.com.jtech.tasklist.application.core.usecases.CreateTaskUseCase;
import br.com.jtech.tasklist.application.core.usecases.DeleteTaskUseCase;
import br.com.jtech.tasklist.application.core.usecases.GetTasksUseCase;
import br.com.jtech.tasklist.application.core.usecases.UpdateTaskUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TaskUseCaseConfig {

    @Bean
    public CreateTaskUseCase createTaskUseCase(TaskAdapter taskAdapter, TasklistAdapter tasklistAdapter) {
        return new CreateTaskUseCase(taskAdapter, tasklistAdapter);
    }

    @Bean
    public GetTasksUseCase getTasksUseCase(TaskAdapter taskAdapter) {
        return new GetTasksUseCase(taskAdapter);
    }

    @Bean
    public UpdateTaskUseCase updateTaskUseCase(TaskAdapter taskAdapter) {
        return new UpdateTaskUseCase(taskAdapter, taskAdapter);
    }

    @Bean
    public DeleteTaskUseCase deleteTaskUseCase(TaskAdapter taskAdapter) {
        return new DeleteTaskUseCase(taskAdapter, taskAdapter);
    }
}
