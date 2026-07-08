package br.com.jtech.tasklist.config.usecases;

import br.com.jtech.tasklist.adapters.output.TasklistAdapter;
import br.com.jtech.tasklist.application.core.usecases.CreateTasklistUseCase;
import br.com.jtech.tasklist.application.core.usecases.DeleteTasklistUseCase;
import br.com.jtech.tasklist.application.core.usecases.GetTasklistsUseCase;
import br.com.jtech.tasklist.application.core.usecases.UpdateTasklistUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TasklistUseCaseConfig {

    @Bean
    public CreateTasklistUseCase createTasklistUseCase(TasklistAdapter tasklistAdapter) {
        return new CreateTasklistUseCase(tasklistAdapter, tasklistAdapter);
    }

    @Bean
    public GetTasklistsUseCase getTasklistsUseCase(TasklistAdapter tasklistAdapter) {
        return new GetTasklistsUseCase(tasklistAdapter);
    }

    @Bean
    public UpdateTasklistUseCase updateTasklistUseCase(TasklistAdapter tasklistAdapter) {
        return new UpdateTasklistUseCase(tasklistAdapter, tasklistAdapter);
    }

    @Bean
    public DeleteTasklistUseCase deleteTasklistUseCase(TasklistAdapter tasklistAdapter) {
        return new DeleteTasklistUseCase(tasklistAdapter, tasklistAdapter);
    }
}
