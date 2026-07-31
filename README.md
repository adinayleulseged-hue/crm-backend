# CRM Backend (INSA)

Spring Boot microservice for the CRM module of the ERP system: customers, leads, opportunities, quotations, sales orders, invoices, payments, campaigns, segments, audit, and dashboard APIs.

## Stack

- Java 21, Spring Boot 3.5
- PostgreSQL (Neon shared team database)
- Spring Security + JWT
- RabbitMQ (async domain events)
- MapStruct, Lombok, Maven

## Prerequisites

- JDK 21+
- Maven Wrapper (`mvnw` / `mvnw.cmd`) included
- Docker Desktop (for RabbitMQ)
- Neon PostgreSQL connection details from the team

## Setup

### 1. Local config

```bash
copy src\main\resources\application.properties.example src\main\resources\application.properties
```

Fill in Neon URL, username, and password.  
`application.properties` is gitignored — never commit secrets.

Optional overrides via env vars: `DB_URL`, `DB_USER`, `DB_PASSWORD`, `JWT_SECRET`, `RABBITMQ_HOST`, `RABBITMQ_USER`, `RABBITMQ_PASSWORD`.

### 2. Start RabbitMQ

```bash
docker compose up -d rabbitmq
```

- AMQP: `localhost:5672` (user `crm` / `crm123`)
- Management UI: http://localhost:15672

### 3. Run the app

**IntelliJ:** open `CrmBackendApplication` → Run  

**CLI:**

```bash
.\mvnw.cmd spring-boot:run
```

App listens on **http://localhost:8080**.

## Auth (JWT)

Public endpoints:

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/auth/login` | Get access token |
| POST | `/api/auth/register` | Register a user |

All other `/api/**` routes require:

```http
Authorization: Bearer <accessToken>
```

### Seeded users

| Username | Password | Role |
|----------|----------|------|
| admin | admin123 | ADMIN |
| sales | sales123 | SALES_OFFICER |
| finance | finance123 | FINANCE_OFFICER |
| employee | employee123 | EMPLOYEE |

### Example login

```http
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "admin123"
}
```

Use the returned `accessToken` on protected calls (see `test.http`).

## RabbitMQ events

Exchange: `erp.events` (topic)

| Routing key | Trigger |
|-------------|---------|
| `sales.order.confirmed` | `PATCH /api/sales-orders/{id}/confirm` |
| `invoice.created` | Create invoice |
| `payment.received` | Create payment |
| `lead.created` / `lead.qualified` / `lead.converted` | Lead lifecycle |

Consumers (in-process for now): notification, finance, and inventory listeners.

## Main API areas

| Area | Base path |
|------|-----------|
| Auth | `/api/auth` |
| Customers | `/api/crm/customers` |
| Leads | `/api/leads` |
| Opportunities | `/api/opportunities` |
| Sales orders | `/api/sales-orders` |
| Invoices | `/api/invoices` |
| Payments | `/api/payments` |
| Quotations | `/api/quotations` |
| Campaigns | `/api/campaigns` |
| Interactions | `/api/interactions` |
| Segments | `/api/segments` |
| Audit | `/api/audit-trails` |
| Dashboard / Analytics / Reports | `/api/dashboard`, `/api/analytics`, `/api/reports` |

## Architecture notes

This service is the **CRM bounded context** toward Phase 2 of the ERP plan:

- Sync: HTTP/REST + JPA → Neon Postgres  
- Async: publish domain events to RabbitMQ  
- Auth: JWT issued here (extract to a dedicated Auth Service later)  
- Schema: **Flyway** migrations under `src/main/resources/db/migration`  
- Observability: Spring Boot **Actuator** (`/actuator/health`, `/info`, `/prometheus`)  
- Still pending for full diagram match: API Gateway, Feign/WebClient to HRM/MMS/FMS, external notification service

## Flyway & Actuator verification

```bash
# Start the app, then:
curl http://localhost:8080/actuator/health
curl http://localhost:8080/actuator/info
curl http://localhost:8080/actuator/prometheus

# Confirm Flyway history in Neon / psql:
# SELECT * FROM flyway_schema_history ORDER BY installed_rank;
# SELECT * FROM system_metadata;
```

## Useful commands

```bash
.\mvnw.cmd clean package
.\mvnw.cmd test
.\mvnw.cmd spring-boot:run
docker compose up -d rabbitmq
docker compose down
```
