package br.com.jtech.tasklist.application.core.domains;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class User extends BaseDomain<String> {

    private String name;
    private String email;
    private String password;
    private UserRole role;
}
