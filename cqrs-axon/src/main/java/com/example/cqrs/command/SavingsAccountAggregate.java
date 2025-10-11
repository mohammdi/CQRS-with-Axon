package com.example.cqrs.command;

import static org.axonframework.modelling.command.AggregateLifecycle.apply;

import com.example.cqrs.api.commands.SavingsCommands;
import com.example.cqrs.api.commands.SavingsCommands.DepositCommand;
import com.example.cqrs.api.commands.SavingsCommands.WithdrawCommand;
import com.example.cqrs.api.events.SavingsEvents.AccountCreatedEvent;
import com.example.cqrs.api.events.SavingsEvents.DepositedEvent;
import com.example.cqrs.api.events.SavingsEvents.WithdrawnEvent;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.spring.stereotype.Aggregate;

@Aggregate
public class SavingsAccountAggregate {

    @AggregateIdentifier
    private String accountNumber;
    private BigDecimal balance;
    private boolean active;
    private Instant activationTime;

    private static final Logger logger = LoggerFactory.getLogger(SavingsAccountAggregate.class);

    protected SavingsAccountAggregate() {}

    @CommandHandler
    public SavingsAccountAggregate(SavingsCommands.CreateAccountCommand cmd) {
        logger.info("[COMMAND:CreateAccount] Handling command: {}", cmd);
        if (cmd.getInitialBalance() == null || cmd.getInitialBalance().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Initial balance must be non-negative");
        }
        logger.info("[EVENT:AccountCreated] Publishing event: clientId={}, initialBalance={}, creationDate={}, status={},  requestNumber={}", cmd.getClientId(), cmd.getInitialBalance(), cmd.getCreationDate(), cmd.getStatus(), cmd.getRequestId());
        apply(new AccountCreatedEvent(
            cmd.getClientId(),
            cmd.getInitialBalance(),
            cmd.getCreationDate().atStartOfDay(ZoneId.systemDefault()).toInstant(),
            cmd.getStatus()
        ));
    }

    @CommandHandler
    public void handle(DepositCommand cmd) {
        logger.info("[COMMAND:Deposit] Handling command: {}", cmd);
        requireActive();
        if (cmd.amount == null || cmd.amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Deposit amount must be positive");
        }
        BigDecimal newBalance = balance.add(cmd.amount);
        logger.info("[EVENT:Deposited] Publishing event: amount={}, newBalance={}, referenceNumber={}, requestNumber={}", accountNumber, cmd.amount, newBalance, cmd.requestId);
        apply(new DepositedEvent(accountNumber, cmd.amount, cmd.requestId, newBalance, Instant.now()));
    }

    @CommandHandler
    public void handle(WithdrawCommand cmd) {
        logger.info("[COMMAND:Withdraw] Handling command: {}", cmd);
        requireActive();
        if (cmd.amount == null || cmd.amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Withdrawal amount must be positive");
        }
        if (balance.compareTo(cmd.amount) < 0) {
            throw new IllegalStateException("Insufficient balance");
        }
        BigDecimal newBalance = balance.subtract(cmd.amount);
        logger.info("[EVENT:Withdrawn] Publishing event: accountNumber={}, amount={}, newBalance={}, requestNumber={}", accountNumber, cmd.amount, newBalance, cmd.requestId);
        apply(new WithdrawnEvent(accountNumber, cmd.amount, cmd.requestId, newBalance, Instant.now()));
    }

/*    @EventSourcingHandler
    public void on(AccountCreatedEvent evt) {
        logger.info("[EVENT:AccountCreated] Sourced Event: {}", evt);
        this.accountNumber = evt.;
        this.balance = evt.initialBalance;
        this.active = "ACTIVE".equalsIgnoreCase(evt.status);
    }

    @EventSourcingHandler
    public void on(DepositedEvent evt) {
        logger.info("[EVENT:Deposited] Sourced Event: {}", evt);
        this.balance = evt.newBalance;
    }

    @EventSourcingHandler
    public void on(WithdrawnEvent evt) {
        logger.info("[EVENT:Withdrawn] Sourced Event: {}", evt);
        this.balance = evt.newBalance;
    }*/

    private void requireActive() {
        if (!active) throw new IllegalStateException("Account is not active");
    }
}


