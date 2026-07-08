package br.com.jtech.tasklist.application.core.domains;

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
}
