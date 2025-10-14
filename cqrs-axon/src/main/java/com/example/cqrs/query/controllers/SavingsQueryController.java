package com.example.cqrs.query.controllers;

import com.example.cqrs.domain.SavingsAccount;
import com.example.cqrs.domain.SavingsTransaction;
import com.example.cqrs.query.repositories.SavingsAccountRepository;
import com.example.cqrs.query.repositories.SavingsTransactionRepository;
import com.example.cqrs.command.repositories.CommandLogSourceRepository;
import com.example.cqrs.domain.CommandLogSource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/query/savings")
public class SavingsQueryController {

    private final SavingsAccountRepository accountRepository;
    private final SavingsTransactionRepository transactionRepository;
    private final CommandLogSourceRepository commandLogSourceRepository;

    public SavingsQueryController(SavingsAccountRepository accountRepository, 
                                  SavingsTransactionRepository transactionRepository,
                                  CommandLogSourceRepository commandLogSourceRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.commandLogSourceRepository = commandLogSourceRepository;
    }

    @GetMapping("/accounts/{accountNumber}")
    public Optional<SavingsAccount> getAccount(@PathVariable String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber);
    }

    @GetMapping("/accounts/{accountNumber}/transactions")
    public List<SavingsTransaction> getAccountTransactions(@PathVariable String accountNumber) {
        return transactionRepository.findByAccountNumberOrderByTransactionDateDesc(accountNumber);
    }

    @GetMapping("/accounts/{accountNumber}/transactions/{referenceNumber}")
    public List<SavingsTransaction> getTransactionsByReference(@PathVariable String accountNumber, 
                                                              @PathVariable String referenceNumber) {
        return transactionRepository.findByAccountNumberAndReferenceNumberOrderByTransactionDateDesc(
            accountNumber, referenceNumber);
    }

    @GetMapping("/request-id/{requestId}")
    public ResponseEntity<?> getAccountByRequestId(@PathVariable("requestId") String requestId) {
        return commandLogSourceRepository.findByRequestIdAndCommandName(requestId, "CreateAccountCommand")
                .flatMap(log -> accountRepository.findByAccountNumber(log.getAccountNumber()))
                .map(acc -> ResponseEntity.ok(new AccountDto(acc)))
                .orElse(ResponseEntity.notFound().build());
    }

    public record AccountDto(String accountNumber, String clientId, String status, java.math.BigDecimal balance, java.time.Instant activationDate) {
        public AccountDto(SavingsAccount acc) {
            this(acc.getAccountNumber(), acc.getClientId(), acc.getStatus().name(), acc.getBalance(), acc.getActivationDate());
        }
    }
}

