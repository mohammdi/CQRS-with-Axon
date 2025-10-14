package com.example.cqrs.api.commands;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.axonframework.modelling.command.TargetAggregateIdentifier;

public class SavingsCommands {

    public static class CreateAccountCommand {
        @TargetAggregateIdentifier
        private final String accountNumber;
        private final String clientId;
        private final java.math.BigDecimal initialBalance;
        private final java.time.LocalDate creationDate;
        private final String status;
        private final String requestId;
        public CreateAccountCommand(String clientId, String accountNumber, java.math.BigDecimal initialBalance, java.time.LocalDate creationDate, String status, String requestId) {
            this.clientId = clientId;
            this.accountNumber = accountNumber;
            this.initialBalance = initialBalance;
            this.creationDate = creationDate;
            this.status = status;
            this.requestId = requestId;
        }
        public String getClientId() { return clientId; }
        public java.math.BigDecimal getInitialBalance() { return initialBalance; }
        public java.time.LocalDate getCreationDate() { return creationDate; }
        public String getStatus() { return status; }
        public String getRequestId() { return requestId; }

        public String getAccountNumber() {
            return accountNumber;
        }


        @Override
        public String toString() {
            return "CreateAccountCommand{" +
                    ", clientId='" + clientId + '\'' +
                    ", initialBalance=" + initialBalance +
                    ", creationDate=" + creationDate +
                    ", status='" + status + '\'' +
                    ", requestId='" + requestId + '\'' +
                    '}';
        }
    }

    public static class DepositCommand {
        @TargetAggregateIdentifier
        public final String accountNumber;
        public final java.math.BigDecimal amount;
        public final String requestId;
        public DepositCommand(String accountNumber, java.math.BigDecimal amount, String requestId) {
            this.accountNumber = accountNumber;
            this.amount = amount;
            this.requestId = requestId;
        }
        @Override
        public String toString() {
            return "DepositCommand{" +
                    "accountNumber='" + accountNumber + '\'' +
                    ", amount=" + amount +
                    ", requestId='" + requestId + '\'' +
                    '}';
        }
    }

    public static class WithdrawCommand {
        @TargetAggregateIdentifier
        public final String accountNumber;
        public final java.math.BigDecimal amount;
        public final String requestId;
        public WithdrawCommand(String accountNumber, java.math.BigDecimal amount, String requestId) {
            this.accountNumber = accountNumber;
            this.amount = amount;
            this.requestId = requestId;
        }
        @Override
        public String toString() {
            return "WithdrawCommand{" +
                    "accountNumber='" + accountNumber + '\'' +
                    ", amount=" + amount +
                    ", requestId='" + requestId + '\'' +
                    '}';
        }
    }
}


