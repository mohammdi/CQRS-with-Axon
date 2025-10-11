package com.example.cqrs.api.events;

import java.math.BigDecimal;
import java.time.Instant;

public class SavingsEvents {

    public static class AccountCreatedEvent {
        public final String clientId;
        public final BigDecimal initialBalance;
        public final Instant creationDate;
        public final String status;

        public AccountCreatedEvent(String clientId, BigDecimal initialBalance, Instant creationDate, String status) {
            this.clientId = clientId;
            this.initialBalance = initialBalance;
            this.creationDate = creationDate;
            this.status = status;
        }

        @Override
        public String toString() {
            return "AccountCreatedEvent{" +
                    ", clientId='" + clientId + '\'' +
                    ", initialBalance=" + initialBalance +
                    ", creationDate=" + creationDate +
                    ", status='" + status + '\'' +
                    '}';
        }
    }

    public static class DepositedEvent {
        public final String accountNumber;
        public final BigDecimal amount;
        public final String requestNumber;
        public final BigDecimal newBalance;
        public final Instant at;

        public DepositedEvent(String accountNumber, BigDecimal amount, String requestNumber, BigDecimal newBalance, Instant at) {
            this.accountNumber = accountNumber;
            this.amount = amount;
            this.requestNumber = requestNumber;
            this.newBalance = newBalance;
            this.at = at;
        }

        @Override
        public String toString() {
            return "DepositedEvent{" +
                    "accountNumber='" + accountNumber + '\'' +
                    ", amount=" + amount +
                    ", requestNumber='" + requestNumber + '\'' +
                    ", newBalance=" + newBalance +
                    ", at=" + at +
                    '}';
        }
    }

    public static class WithdrawnEvent {
        public final String accountNumber;
        public final BigDecimal amount;
        public final String requestNumber;
        public final BigDecimal newBalance;
        public final Instant at;

        public WithdrawnEvent(String accountNumber, BigDecimal amount, String requestNumber, BigDecimal newBalance, Instant at) {
            this.accountNumber = accountNumber;
            this.amount = amount;
            this.requestNumber = requestNumber;
            this.newBalance = newBalance;
            this.at = at;
        }

        @Override
        public String toString() {
            return "WithdrawnEvent{" +
                    "accountNumber='" + accountNumber + '\'' +
                    ", amount=" + amount +
                    ", requestNumber='" + requestNumber + '\'' +
                    ", newBalance=" + newBalance +
                    ", at=" + at +
                    '}';
        }
    }
}


