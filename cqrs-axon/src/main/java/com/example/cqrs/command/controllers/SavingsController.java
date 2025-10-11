package com.example.cqrs.command.controllers;

import com.example.cqrs.api.commands.SavingsCommands.DepositCommand;
import com.example.cqrs.api.commands.SavingsCommands.WithdrawCommand;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.cqrs.api.commands.SavingsCommands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestHeader;
import com.example.cqrs.command.services.generatorService;

@RestController
@RequestMapping("/api/command/savings")
@Validated
public class SavingsController {

    private final CommandGateway commandGateway;
    private static final Logger logger = LoggerFactory.getLogger(SavingsController.class);


    public SavingsController(CommandGateway commandGateway) {
        this.commandGateway = commandGateway;
    }

    public record MoneyRequest(
            @NotBlank String accountNumber,
            @NotNull @DecimalMin(value = "0.01") BigDecimal amount,
            String requestNumber
    ) {}

    @PostMapping("/deposit")
    public ResponseEntity<String> deposit(
            @Valid @RequestBody MoneyRequest req,
            @RequestHeader(value = "x-request-id") String requestId) {
        logger.info("[COMMAND:Deposit] Received: {}", req);
        commandGateway.sendAndWait(new DepositCommand(req.accountNumber(), req.amount(), requestId));
        return ResponseEntity.accepted().body(requestId);
    }

    @PostMapping("/withdraw")
    public ResponseEntity<String> withdraw(
            @Valid @RequestBody MoneyRequest req,
            @RequestHeader(value = "x-request-id") String requestId) {
        logger.info("[COMMAND:Withdraw] Received: {}", req);

        commandGateway.sendAndWait(new WithdrawCommand(req.accountNumber(), req.amount(), requestId));
        return ResponseEntity.accepted().body(requestId);
    }

    @PostMapping("/create")
    public ResponseEntity<String> createAccount(
            @RequestBody CreateAccountRequest request,
            @RequestHeader(value = "x-request-id") String requestId) {
        logger.info("[COMMAND:CreateAccount] Received: {}", request);
        commandGateway.sendAndWait(new SavingsCommands.CreateAccountCommand(
            request.getClientId(),
            request.getInitialBalance(),
            request.getCreationDate(),
            request.getStatus(),
            requestId
        ));
        return ResponseEntity.accepted().body(requestId);
    }

    public static class CreateAccountRequest {
        private String accountNumber;
        private String clientId;
        private java.math.BigDecimal initialBalance;
        private java.time.LocalDate creationDate;
        private String status;
        public String getAccountNumber() { return accountNumber; }
        public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }
        public String getClientId() { return clientId; }
        public void setClientId(String clientId) { this.clientId = clientId; }
        public java.math.BigDecimal getInitialBalance() { return initialBalance; }
        public void setInitialBalance(java.math.BigDecimal initialBalance) { this.initialBalance = initialBalance; }
        public java.time.LocalDate getCreationDate() { return creationDate; }
        public void setCreationDate(java.time.LocalDate creationDate) { this.creationDate = creationDate; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }
}


