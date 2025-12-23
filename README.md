# Distributed Ledger System (Modular Monolith)

A high-performance, audit-ready Distributed Ledger System built with **Java 21**, **Gradle**, **Spring Boot**, and **Project Reactor**.

## 🏗️ Architecture

```mermaid
graph TD
    User[User/Client] -->|HTTPS| APIGateway[Rate Limiter & API Gateway]
    APIGateway -->|OAuth2| LedgerCore[Ledger Core]
    APIGateway --> WalletService[Wallet Service]
    
    subgraph "Modular Monolith"
        LedgerCore -->|Reads/Writes| DB[(PostgreSQL)]
        WalletService -->|Saga Reserve| ClearingHouse[Clearing House]
        ClearingHouse -->|Reactor Pipe| AntiFraud[Anti-Fraud Engine]
        AntiFraud -->|Rules| Redis[(Redis Cache)]
        ClearingHouse -->|Commit| LedgerCore
    end

    subgraph "Audit & Monitoring"
        LedgerCore -->|@Auditable| AuditDB[(Audit Log)]
        Prometheus -->|Scrape| LedgerCore
        Grafana -->|Query| Prometheus
    end
```

## 🚀 Quick Start

Ensure you have **Java 21** and **Docker** installed.

1.  **Build Code**:
    ```bash
    make build
    ```

2.  **Start Infrastructure**:
    ```bash
    make start
    ```

3.  **Access API Docs**:
    Open [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html) (Once app is running)

## 📦 Modules

-   **`ledger-core`**: Immutable double-entry bookkeeping (Source of Truth).
-   **`wallet-service`**: User balances and sub-wallets.
-   **`clearing-house`**: Saga Coordinator for distributed transactions.
-   **`anti-fraud-engine`**: High-performance (<50ms) reactive fraud detection ("Antigravity").
-   **`common-lib`**: Shared Value Objects (`Money`) and Utilities (`ApiResponse`, Audit).

## 🔌 API Endpoints

### Accounts
- `POST /api/v1/accounts` - Create account
- `GET /api/v1/accounts/{id}` - Get account
- `GET /api/v1/accounts` - List accounts (paginated)

### Transactions
- `POST /api/v1/transactions` - Post transaction (with idempotency)

### Cross-Border
- `POST /api/v1/cross-border/transfer` - Execute FX transfer

### Reconciliation
- `POST /api/v1/reconciliation/run` - Manual reconciliation (Admin only)
- `GET /api/v1/reconciliation/status` - Check system status

### Monitoring
- `GET /actuator/health` - Health check
- `GET /actuator/metrics` - Metrics
- `GET /actuator/prometheus` - Prometheus metrics
- `GET /swagger-ui.html` - API documentation

## 🛡️ Audit Shield

-   **Global Audit**: Every `@Auditable` action is logged with Principal/IP.
-   **Security**: OAuth2 / JWT Resource Server protected.
-   **Reconciliation**: Hourly job verifies `Sum(Lines) == Balance` and HALTS system on failure.
