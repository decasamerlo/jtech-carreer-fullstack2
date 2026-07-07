package br.com.jtech.tasklist.application.core.domains;

import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@ToString(callSuper = true)
@NoArgsConstructor
public class Task extends BaseDomain<String> {

    private String title;
    private String description;
    private Boolean completed;
    private String tasklistId;
    private String userId;
}
