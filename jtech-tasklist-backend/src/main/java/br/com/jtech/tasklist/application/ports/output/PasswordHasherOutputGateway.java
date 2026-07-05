package br.com.jtech.tasklist.application.ports.output;

public interface PasswordHasherOutputGateway {
    String encode(String rawPassword);
    boolean matches(String rawPassword, String encodedPassword);
}
