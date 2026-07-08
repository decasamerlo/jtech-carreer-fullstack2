package br.com.jtech.tasklist.adapters.input.protocols;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
* class TasklistRequest 
* 
* user angelo.vicente 
*/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TasklistRequest implements Serializable {
    private String id;
    @NotBlank
    @Size(max = 100)
    private String name;

    private List<TasklistRequest> requests;
}