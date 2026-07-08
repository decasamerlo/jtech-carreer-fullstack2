package br.com.jtech.tasklist.config.usecases;

import br.com.jtech.tasklist.application.core.usecases.LoginUseCase;
import br.com.jtech.tasklist.application.core.usecases.RefreshUseCase;
import br.com.jtech.tasklist.application.core.usecases.RegisterUserUseCase;
import br.com.jtech.tasklist.application.ports.output.PasswordHasherOutputGateway;
import br.com.jtech.tasklist.application.ports.output.RefreshTokenOutputGateway;
import br.com.jtech.tasklist.application.ports.output.TokenOutputGateway;
import br.com.jtech.tasklist.application.ports.output.UserOutputGateway;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AuthUseCaseConfig {

    @Bean
    public RegisterUserUseCase registerUserUseCase(UserOutputGateway userOutputGateway,
                                                    PasswordHasherOutputGateway passwordHasherOutputGateway,
                                                    TokenOutputGateway tokenOutputGateway,
                                                    RefreshTokenOutputGateway refreshTokenOutputGateway) {
        return new RegisterUserUseCase(userOutputGateway, passwordHasherOutputGateway, tokenOutputGateway, refreshTokenOutputGateway);
    }

    @Bean
    public LoginUseCase loginUseCase(UserOutputGateway userOutputGateway,
                                     RefreshTokenOutputGateway refreshTokenOutputGateway,
                                     PasswordHasherOutputGateway passwordHasherOutputGateway,
                                     TokenOutputGateway tokenOutputGateway) {
        return new LoginUseCase(userOutputGateway, refreshTokenOutputGateway, tokenOutputGateway, passwordHasherOutputGateway);
    }

    @Bean
    public RefreshUseCase refreshUseCase(RefreshTokenOutputGateway refreshTokenOutputGateway,
                                          TokenOutputGateway tokenOutputGateway) {
        return new RefreshUseCase(refreshTokenOutputGateway, tokenOutputGateway);
    }
}
