package br.com.jtech.tasklist.adapters.output;

import br.com.jtech.tasklist.adapters.output.repositories.UserRepository;
import br.com.jtech.tasklist.application.core.domains.User;
import br.com.jtech.tasklist.application.ports.output.UserOutputGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UserAdapter implements UserOutputGateway {

    private final UserRepository userRepository;

    @Override
    public User save(User user) {
        var entity = UserMapper.toEntity(user);
        var saved = userRepository.save(entity);
        return UserMapper.toDomain(saved);
    }

    @Override
    public Optional<User> findById(String id) {
        return userRepository.findById(UUID.fromString(id)).map(UserMapper::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email).map(UserMapper::toDomain);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
}
