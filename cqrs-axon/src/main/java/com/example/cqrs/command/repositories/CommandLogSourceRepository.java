package com.example.cqrs.command.repositories;

import com.example.cqrs.domain.CommandLogSource;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CommandLogSourceRepository extends JpaRepository<CommandLogSource, Long> {
    Optional<CommandLogSource> findByRequestId(String requestId);
    Optional<CommandLogSource> findByRequestIdAndCommandName(String requestId, String commandName);
}
