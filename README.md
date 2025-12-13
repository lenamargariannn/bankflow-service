# BankFlow Service

A simple Spring Boot service for customer and account operations with PostgreSQL.

## Quick Start

- Java 21, Maven
- PostgreSQL running locally
- Base URL: `http://localhost:8080/api`

### 1) Configure DB (local)
Update `src/main/resources/application.yml` if needed:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/bankflow
    username: postgres
    password: password
```
Create DB:

```sql
CREATE DATABASE bankflow;
```

### 2) Build & Run

```bash
mvn clean package -DskipTests
mvn spring-boot:run
```

Health:

```bash
curl http://localhost:8080/api/actuator/health
```

## API (v1)

### Authentication
- POST `/api/v1/auth/signup`
- POST `/api/v1/auth/login`

### Customers (username-based)
- GET `/api/v1/customers/{username}`
- PUT `/api/v1/customers/{username}`
- GET `/api/v1/customers/{username}/accounts`
- POST `/api/v1/customers/{username}/accounts` (optional `initialDeposit`)

### Accounts (accountNumber-based)
- GET `/api/v1/accounts/{accountNumber}`
- POST `/api/v1/accounts/{accountNumber}/deposit` `{ "amount": 100.00 }`
- POST `/api/v1/accounts/{accountNumber}/withdraw` `{ "amount": 50.00 }`
- POST `/api/v1/accounts/transfer` `{ "fromAccountNumber": "...", "toAccountNumber": "...", "amount": 150.00, "description": "..." }`
- GET `/api/v1/accounts/{accountNumber}/transactions`
- GET `/api/v1/accounts/{accountNumber}/transactions/{transactionId}`

Notes:
- `accountNumber` is digits-only, length 12–20.
- Update customer supports partial updates: null or empty values are ignored.

## Dev Profile & GCP

- Dev profile: `SPRING_PROFILES_ACTIVE=dev` (used in Docker)
- Local dev does not use GCP Secret Manager (disabled via autoconfigure exclusion).

## Docker (in `/docker`)

Build & run with dev profile:

```bash
# Build image (multi-stage)
DOCKER_BUILDKIT=1 docker build -t bankflow-service:dev ./docker

# Run container
docker run -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=dev \
  -e DB_URL=jdbc:postgresql://host.docker.internal:5432/bankflow \
  -e DB_USERNAME=postgres \
  -e DB_PASSWORD=password \
  bankflow-service:dev
```

## Project Structure

```
src/
  main/java/com/bankflow/...    # controllers, service, repository, model, util
  main/resources/application.yml # config
  test/java/...                  # unit & integration tests
```

## Troubleshooting
- Flyway errors: `mvn flyway:repair && mvn flyway:migrate`
- Port in use: stop other apps on 8080 or run with `--server.port=9090`
- DB connection: verify URL/credentials; ensure Postgres is running

## License
Part of the BankFlow system.
