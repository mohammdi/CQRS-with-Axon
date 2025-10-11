package com.example.cqrs.command.services;

import com.example.cqrs.domain.CommandLogSource;
import com.example.cqrs.command.repositories.CommandLogSourceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CommandIdempotencyService {
    private final CommandLogSourceRepository repo;

    public CommandIdempotencyService(CommandLogSourceRepository repo) {
        this.repo = repo;
    }

    @Transactional(readOnly = true)
    public void ensureIdempotencyOrThrow(String requestId) {
        repo.findByRequestId(requestId).ifPresent(log -> {
            throw new AlreadyHandledException("Request already performed", log.getAccountNumber(), log.getReferenceNumber());
        });
    }

    @Transactional
    public void logCommand(String commandName, String accountNumber, String requestId, String referenceNumber) {
        CommandLogSource log = new CommandLogSource(commandName, accountNumber, requestId, referenceNumber);
        repo.save(log);
    }

    public static class AlreadyHandledException extends RuntimeException {
        private final String accountNumber;
        private final String referenceNumber;
        public AlreadyHandledException(String message, String accountNumber, String referenceNumber) {
            super(message);
            this.accountNumber = accountNumber;
            this.referenceNumber = referenceNumber;
        }
        public String getAccountNumber() { return accountNumber; }
        public String getReferenceNumber() { return referenceNumber; }
    }
}
