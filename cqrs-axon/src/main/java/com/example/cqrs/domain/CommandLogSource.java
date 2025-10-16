package com.example.cqrs.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "command_log_source", uniqueConstraints = @UniqueConstraint(columnNames = "request_id"))
public class CommandLogSource {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "command_name", nullable = false, length = 100)
    private String commandName;

    @Column(name = "account_number", length = 64)
    private String accountNumber;

    @Column(name = "request_id", nullable = false, length = 100)
    private String requestId;

    @Column(name = "reference_number", length = 100)
    private String referenceNumber;

    public CommandLogSource() {}
    public CommandLogSource(String commandName, String accountNumber, String requestId, String referenceNumber) {
        this.commandName = commandName;
        this.accountNumber = accountNumber;
        this.requestId = requestId;
        this.referenceNumber = referenceNumber;
    }
    public Long getId() { return id; }
    public String getCommandName() { return commandName; }
    public String getAccountNumber() { return accountNumber; }
    public String getRequestId() { return requestId; }
    public String getReferenceNumber() { return referenceNumber; }
    public void setId(Long id) { this.id = id; }
    public void setCommandName(String commandName) { this.commandName = commandName; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }
    public void setRequestId(String requestId) { this.requestId = requestId; }
    public void setReferenceNumber(String referenceNumber) { this.referenceNumber = referenceNumber; }
}
