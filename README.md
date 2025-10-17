# CQRS-with-Axon

## Overview
This project demonstrates a production-grade CQRS and Event Sourcing application using Spring Boot 3, Axon Framework, and H2 (in-memory) as event store/read database. It models a Savings Account domain and implements clear separation of command (write) and query (read) sides, enriched with centralized idempotency, structured logging, and OpenAPI documentation.

## Architecture
- **CQRS**: Commands mutate state on the write side (Aggregates); Queries read from denormalized read models (Projections/Repositories).
- **Event Sourcing**: Aggregates apply and source events to rebuild state. Events are the single source of truth.
- **Axon Framework**: Handles command routing, aggregate lifecycle, event store, and projections via processors.
- **Spring Boot 3 (Java 21)**: REST controllers, auto-configuration, DI, and actuator.
- **Database**: H2 in-memory for both event store and read models (dev). Replace with your RDBMS for persistence.

## Key Components
- `api/commands`: Command definitions (`CreateAccountCommand`, `DepositCommand`, `WithdrawCommand`).
- `api/events`: Event definitions (`AccountCreatedEvent`, `DepositedEvent`, `WithdrawnEvent`).
- `command/controllers`: REST endpoints for command side (create account, deposit, withdraw).
- `command` (aggregate): `SavingsAccountAggregate` (must exist) handles commands and applies events.
- `query/projections`: `SavingsProjections` builds/updates read models on events.
- `query/repositories`: Spring Data JPA repositories for read models.
- `config`: Cross-cutting filters (idempotency, request/response logging) and OpenAPI config.
- `domain`: JPA entities for read side (`SavingsAccount`, `SavingsTransaction`) and `CommandLogSource` (idempotency log).

## Command Flow (Write Side)
1. Client calls a command endpoint with header `x-request-id`.
2. `IdempotencyFilter` enforces presence of `x-request-id` and rejects duplicates via `CommandLogSource`.
3. Controller builds the command and sends it via `CommandGateway`.
4. Axon routes the command to `SavingsAccountAggregate` using `@TargetAggregateIdentifier`.
5. Aggregate validates and applies domain events.
6. After success, `IdempotencyFilter` logs the command in `CommandLogSource`.

## Query Flow (Read Side)
1. Axon event processors deliver events to `SavingsProjections`.
2. Projections update read models: `SavingsAccount`, `SavingsTransaction`.
3. Query controllers read from repositories and return denormalized data.

## Idempotency (Centralized)
- Implemented via `IdempotencyFilter` + `CommandIdempotencyService`.
- Requires `x-request-id` header; duplicates (same requestId) return HTTP 409.
- `CommandLogSource` stores: `command_name`, `account_number`, `request_id`, `reference_number`.
- Controllers remain clean; no per-endpoint duplication logic.

## Logging
- `ApiLoggingInterceptor` logs method/URI for GET, and logs request/response bodies for non-GET methods.
- Log4j2 is used (Logback excluded). Config at `src/main/resources/log4j2.xml`.

## REST Endpoints
- Command side (`/api/command/savings`):
  - `POST /create` (Create account)
    - Header: `x-request-id: <uuid-from-client>`
    - Body: `{ "clientId": "...", "initialBalance": 0, "creationDate": "YYYY-MM-DD", "status": "ACTIVE" }`
    - Returns: `{ "accountNumber": "...", "requestId": "..." }` (server-generated accountNumber)
  - `POST /deposit`
    - Header: `x-request-id`
    - Body: `{ "accountNumber": "...", "amount": 100.00 }`
  - `POST /withdraw`
    - Header: `x-request-id`
    - Body: `{ "accountNumber": "...", "amount": 50.00 }`
- Query side (`/api/query/savings`):
  - `GET /request-id/{requestId}` → resolves `requestId` via `CommandLogSource` and returns account info.

## OpenAPI (Swagger UI)
- Dependency: `springdoc-openapi-starter-webmvc-ui` (2.x). 
- UI: `http://localhost:8080/swagger-ui/index.html`

## How to Run
```bash
cd cqrs-axon
mvn clean install
mvn spring-boot:run
```
- H2 Console: `http://localhost:8080/h2-console` (JDBC: `jdbc:h2:mem:cqrsdb`, user `SA`, empty password)

## Testing Quickstart
- Create account:
```bash
curl -X POST http://localhost:8080/api/command/savings/create \
  -H 'Content-Type: application/json' \
  -H 'x-request-id: <uuid-from-client>' \
  -d '{"clientId":"c1","initialBalance":100.00,"creationDate":"2025-01-01","status":"ACTIVE"}'
```
- Query by request id:
```bash
curl http://localhost:8080/api/query/savings/request-id/<uuid-from-client>
```
- Deposit:
```bash
curl -X POST http://localhost:8080/api/command/savings/deposit \
  -H 'Content-Type: application/json' -H 'x-request-id: <uuid>' \
  -d '{"accountNumber":"<acct>","amount":25.00}'
```

## Troubleshooting
- "AggregateNotFoundException" on create command:
  - Ensure `SavingsAccountAggregate` exists, annotated with `@Aggregate`.
  - Ensure `CreateAccountCommand` has `@TargetAggregateIdentifier accountNumber`.
  - Ensure the aggregate has a constructor `@CommandHandler(CreateAccountCommand)` that sets `this.accountNumber = cmd.getAccountNumber();` and applies `AccountCreatedEvent`.
- Port 8080 in use: change `server.port` in `application.yml` or stop the conflicting process.
- Swagger 404: ensure `springdoc-openapi-starter-webmvc-ui` (2.x) is present and app is running.

## Notes
- This repo uses H2 in-memory for demo; switch to Postgres/MySQL for persistence.
- Redis starter present; no Redis repositories are enabled by default.
- Idempotency requires clients to always send a unique `x-request-id`.
