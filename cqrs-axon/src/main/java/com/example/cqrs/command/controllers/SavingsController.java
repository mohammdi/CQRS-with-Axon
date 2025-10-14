package com.example.cqrs.command.controllers;

import com.example.cqrs.api.commands.SavingsCommands.DepositCommand;
import com.example.cqrs.api.commands.SavingsCommands.WithdrawCommand;
import com.example.cqrs.command.services.GeneratorService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.boot.json.GsonJsonParser;
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

@RestController
@RequestMapping("/api/command/savings")
@Validated
public class SavingsController {

    private final CommandGateway commandGateway;
    private final GeneratorService generatorService;
    private static final Logger logger = LoggerFactory.getLogger(SavingsController.class);


    public SavingsController(CommandGateway commandGateway, GeneratorService generatorService) {
        this.commandGateway = commandGateway;
        this.generatorService = generatorService;
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
            @RequestHeader(value = "x-request-id") String requestId) throws JsonProcessingException {
        logger.info("[COMMAND:CreateAccount] Received: {}", request);
        String generatedAccountNumber = generatorService.generateAccountNumber();
        Object result = commandGateway.sendAndWait(new SavingsCommands.CreateAccountCommand(
                request.getClientId(),
                generatedAccountNumber,
                request.getInitialBalance(),
                request.getCreationDate(),
                request.getStatus(),
                requestId
        ));
        Map<String , String> response = Map.of("requestId", requestId, "accountNumber", generatedAccountNumber);
        return ResponseEntity.accepted().body(new ObjectMapper().writeValueAsString(response));
    }

    public static class CreateAccountRequest {
        private String clientId;
        private java.math.BigDecimal initialBalance;
        private java.time.LocalDate creationDate;
        private String status;
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


