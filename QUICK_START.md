# Quick Start Guide

## Prerequisites
- Java 21
- Docker and Docker Compose
- Gradle (optional - wrapper included)

## Setup Steps

### 1. Download Gradle Wrapper JAR (if needed)
If `gradle/wrapper/gradle-wrapper.jar` is missing, download it:
```bash
curl -L https://raw.githubusercontent.com/gradle/gradle/v8.5.0/gradle/wrapper/gradle-wrapper.jar -o gradle/wrapper/gradle-wrapper.jar
```

Or use Gradle to generate it:
```bash
gradle wrapper --gradle-version 8.5
```

### 2. Start Infrastructure
```bash
make start
# or
docker-compose up -d
```

This starts:
- PostgreSQL on port 5432
- Redis on port 6379
- Prometheus on port 9090
- Grafana on port 3000

### 3. Build the Project
```bash
make build
# or
./gradlew build
```

### 4. Run the Application
```bash
cd ledger-core
../gradlew bootRun
# or run LedgerApplication.main() from your IDE
```

### 5. Access Services
- **API Docs**: http://localhost:8080/swagger-ui.html
- **Actuator Health**: http://localhost:8080/actuator/health
- **Prometheus**: http://localhost:9090
- **Grafana**: http://localhost:3000 (admin/admin)

## Configuration

Edit `ledger-core/src/main/resources/application.properties` to customize:
- Database connection
- Redis connection
- OAuth2 issuer URI
- Logging levels

## Testing

Run all tests:
```bash
./gradlew test
```

Run specific test:
```bash
./gradlew test --tests "com.fintech.ledger.core.domain.JournalEntryTest"
```

## Project Structure

```
fintech/
├── common-lib/          # Shared value objects and utilities
├── ledger-core/         # Main Spring Boot application
├── wallet-service/      # Wallet management (skeleton)
├── clearing-house/      # Saga coordinator
├── anti-fraud-engine/   # Fraud detection rules
└── docker-compose.yml   # Infrastructure services
```

## Next Steps

1. Configure OAuth2 issuer URI for production
2. Add REST controllers for API endpoints
3. Implement wallet-service and clearing-house clients
4. Add database migrations (Flyway/Liquibase)
5. Set up CI/CD pipeline

See `IMPROVEMENTS.md` for detailed recommendations.



