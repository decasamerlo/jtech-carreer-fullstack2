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
        return new CreateTaskUseCase(taskAdapter, tasklistAdapter, taskAdapter);
    }

    @Bean
    public GetTasksUseCase getTasksUseCase(TaskAdapter taskAdapter, TasklistAdapter tasklistAdapter) {
        return new GetTasksUseCase(taskAdapter, tasklistAdapter);
    }

    @Bean
    public UpdateTaskUseCase updateTaskUseCase(TaskAdapter taskAdapter, TasklistAdapter tasklistAdapter) {
        return new UpdateTaskUseCase(taskAdapter, taskAdapter, tasklistAdapter);
    }

    @Bean
    public DeleteTaskUseCase deleteTaskUseCase(TaskAdapter taskAdapter, TasklistAdapter tasklistAdapter) {
        return new DeleteTaskUseCase(taskAdapter, taskAdapter, tasklistAdapter);
    }
}
