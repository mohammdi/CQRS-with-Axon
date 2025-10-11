package com.example.cqrs.query.controllers;

import com.example.cqrs.domain.SavingsAccount;
import com.example.cqrs.domain.SavingsTransaction;
import com.example.cqrs.query.repositories.SavingsAccountRepository;
import com.example.cqrs.query.repositories.SavingsTransactionRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/query/savings")
public class SavingsQueryController {

    private final SavingsAccountRepository accountRepository;
    private final SavingsTransactionRepository transactionRepository;

    public SavingsQueryController(SavingsAccountRepository accountRepository, 
                               SavingsTransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
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
}

