package com.example.cqrs.query.projections;

import com.example.cqrs.api.events.SavingsEvents.AccountCreatedEvent;
import com.example.cqrs.api.events.SavingsEvents.DepositedEvent;
import com.example.cqrs.api.events.SavingsEvents.WithdrawnEvent;
import com.example.cqrs.command.services.GeneratorService;
import com.example.cqrs.domain.SavingsAccount;
import com.example.cqrs.domain.SavingsTransaction;
import com.example.cqrs.query.repositories.SavingsAccountRepository;
import com.example.cqrs.query.repositories.SavingsTransactionRepository;
import java.time.Instant;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class SavingsProjections {

    private final SavingsAccountRepository accountRepository;
    private final SavingsTransactionRepository txRepository;
    private final GeneratorService generatorService;

    public SavingsProjections(SavingsAccountRepository accountRepository,
                              SavingsTransactionRepository txRepository,
                              GeneratorService generatorService) {
        this.accountRepository = accountRepository;
        this.txRepository = txRepository;
        this.generatorService = generatorService;
    }

    @EventHandler
    public void on(AccountCreatedEvent evt) {
        SavingsAccount acc = new SavingsAccount();
        acc.setAccountNumber(generatorService.generateAccountNumber());
        acc.setClientId(evt.clientId);
        acc.setActivationDate(Objects.requireNonNullElseGet(evt.creationDate, Instant::now));
        acc.setStatus(SavingsAccount.Status.ACTIVE);
        acc.setBalance(evt.initialBalance);
        accountRepository.save(acc);
    }

    @EventHandler
    public void on(DepositedEvent evt) {
        SavingsAccount acc = accountRepository.findByAccountNumber(evt.accountNumber)
                .orElseThrow();
        acc.setBalance(evt.newBalance);
        accountRepository.save(acc);

        SavingsTransaction tx = new SavingsTransaction();
        tx.setAccountNumber(evt.accountNumber);
        tx.setAmount(evt.amount);
        tx.setTransactionType(SavingsTransaction.TransactionType.DEPOSIT);
        tx.setReferenceNumber(generatorService.generateReferenceNumber());
        tx.setRequestNumber(evt.requestNumber);
        tx.setRunningBalance(evt.newBalance);
        tx.setTransactionDate(evt.at);
        tx.setSavingId(acc.getId());
        txRepository.save(tx);
    }

    @EventHandler
    public void on(WithdrawnEvent evt) {
        SavingsAccount acc = accountRepository.findByAccountNumber(evt.accountNumber)
                .orElseThrow();
        acc.setBalance(evt.newBalance);
        accountRepository.save(acc);

        SavingsTransaction tx = new SavingsTransaction();
        tx.setAccountNumber(evt.accountNumber);
        tx.setAmount(evt.amount);
        tx.setTransactionType(SavingsTransaction.TransactionType.WITHDRAWAL);
        tx.setReferenceNumber(generatorService.generateReferenceNumber());
        tx.setRequestNumber(evt.requestNumber);
        tx.setRunningBalance(evt.newBalance);
        tx.setTransactionDate(evt.at);
        tx.setSavingId(acc.getId());
        txRepository.save(tx);
    }
}


