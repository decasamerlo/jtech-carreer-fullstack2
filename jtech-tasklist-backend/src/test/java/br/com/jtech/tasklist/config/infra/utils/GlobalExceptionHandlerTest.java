package br.com.jtech.tasklist.config.infra.handlers;

import br.com.jtech.tasklist.config.infra.exceptions.ApiError;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler handler;

    @Test
    void handleDataIntegrityViolation_ShouldReturnTasklistMessage_WhenTasklistConstraintViolated() {
        DataIntegrityViolationException ex = new DataIntegrityViolationException(
                "could not execute statement; SQL [n/a]; constraint [uk_tasklist_user_name]");

        ResponseEntity<ApiError> response = handler.handleDataIntegrityViolation(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getMessage()).isEqualTo("A list with this name already exists");
    }

    @Test
    void handleDataIntegrityViolation_ShouldReturnTasklistMessage_WhenConstraintNameUppercase() {
        DataIntegrityViolationException ex = new DataIntegrityViolationException(
                "Unique index or primary key violation: UK_TASKLIST_USER_NAME");

        ResponseEntity<ApiError> response = handler.handleDataIntegrityViolation(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getMessage()).isEqualTo("A list with this name already exists");
    }

    @Test
    void handleDataIntegrityViolation_ShouldReturnEmailMessage_WhenEmailConstraintViolated() {
        DataIntegrityViolationException ex = new DataIntegrityViolationException(
                "could not execute statement; SQL [n/a]; constraint [uk_users_email]");

        ResponseEntity<ApiError> response = handler.handleDataIntegrityViolation(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getMessage()).isEqualTo("Email already registered");
    }

    @Test
    void handleDataIntegrityViolation_ShouldReturnTaskTitleMessage_WhenTaskTitleConstraintViolated() {
        DataIntegrityViolationException ex = new DataIntegrityViolationException(
                "could not execute statement; SQL [n/a]; constraint [uk_task_tasklist_title]");

        ResponseEntity<ApiError> response = handler.handleDataIntegrityViolation(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getMessage()).isEqualTo("A task with this title already exists in this list");
    }

    @Test
    void handleDataIntegrityViolation_ShouldReturnGenericMessage_WhenUnknownConstraintViolated() {
        DataIntegrityViolationException ex = new DataIntegrityViolationException(
                "could not execute statement; SQL [n/a]; constraint [some_other_constraint]");

        ResponseEntity<ApiError> response = handler.handleDataIntegrityViolation(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getMessage()).isEqualTo("Data integrity violation");
    }

    @Test
    void handleDataIntegrityViolation_ShouldReturnGenericMessage_WhenMessageIsNull() {
        DataIntegrityViolationException ex = new DataIntegrityViolationException((String) null);

        ResponseEntity<ApiError> response = handler.handleDataIntegrityViolation(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getMessage()).isEqualTo("Data integrity violation");
    }
}
