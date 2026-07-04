package br.com.jtech.tasklist.config.infra.audit;

import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component("auditorAware")
public class AuditorAwareImpl implements AuditorAware<UUID> {

    @Override
    public Optional<UUID> getCurrentAuditor() {
        // TODO: Replace with SecurityContextHolder extraction once backend-auth is implemented
        // For now returns a fixed system UUID placeholder
        return Optional.of(UUID.fromString("00000000-0000-0000-0000-000000000001"));
    }
}
