package br.com.jtech.tasklist.application.core.domains;

import br.com.jtech.tasklist.adapters.input.protocols.TasklistRequest;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@ToString(callSuper = true)
@NoArgsConstructor
public class Tasklist extends BaseDomain<String> {

    private String name;
    private String userId;

    public static Tasklist of(TasklistRequest request) {
        return Tasklist.builder()
            .id(request.getId())
            .name(request.getName())
            .build();
    }
}
