# CRM Backend (INSA)

Spring Boot CRM microservice for the ERP system, now fronted by an API Gateway with a shared Auth Service.

## Services in this repo

| Service | Port | Role |
|---------|------|------|
| `api-gateway/` | **8080** | Entry point — routes `/api/auth/**` → Auth, `/api/**` → CRM |
| `auth-service/` | **8081** | Issues JWT (login/register), owns `auth_users` |
| CRM (root) | **8082** | JWT resource server — CRM domain APIs only |

## Stack

- Java 21, Spring Boot 3.5.16
- Spring Cloud Gateway 2025.0
- PostgreSQL (Neon shared team database) + Flyway
- Spring Security + JWT (JJWT 0.12.6)
- RabbitMQ (async domain events)
- WebClient integrations (HRM / MMS / FMS / Auth)
- MapStruct, Lombok, Maven

## Prerequisites

- JDK 21+
- Maven Wrapper (`mvnw` / `mvnw.cmd`) at repo root
- Docker Desktop (for RabbitMQ)
- Neon PostgreSQL connection details from the team

## Setup

### 1. Local config

```bash
copy src\main\resources\application.properties.example src\main\resources\application.properties
copy auth-service\src\main\resources\application.properties.example auth-service\src\main\resources\application.properties
```

Fill Neon URL/user/password in **both** CRM and Auth property files.  
Use the **same** `JWT_SECRET` in Auth (`auth.security.jwt.secret`) and CRM (`crm.security.jwt.secret`).

`application.properties` files are gitignored — never commit secrets.  
Gateway config (`api-gateway/.../application.properties`) has no secrets and is committed.

### 2. Start RabbitMQ

```bash
docker compose up -d rabbitmq
```

### 3. Run all three services

```bash
# Terminal 1 — Auth
cd auth-service
..\mvnw.cmd spring-boot:run

# Terminal 2 — CRM
.\mvnw.cmd spring-boot:run

# Terminal 3 — Gateway
cd api-gateway
..\mvnw.cmd spring-boot:run
```

Clients should call the **gateway** at **http://localhost:8080**.

## Auth (JWT) — via Auth Service / Gateway

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/auth/login` | Get access token |
| POST | `/api/auth/register` | Register a user |

CRM no longer owns users (`crm_users` dropped in Flyway `V2__drop_crm_users.sql`).  
CRM validates JWTs from Auth Service claims only.

### Seeded users (Auth Service)

| Username | Password | Role |
|----------|----------|------|
| admin | admin123 | ADMIN |
| sales | sales123 | SALES_OFFICER |
| finance | finance123 | FINANCE_OFFICER |
| employee | employee123 | EMPLOYEE |

### Example login (through gateway)

```http
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "admin123"
}
```

Use `Authorization: Bearer <accessToken>` on CRM routes (see `test.http`).

## Security

Mutating and sensitive CRM endpoints use `@PreAuthorize` role checks (`ADMIN`, `SALES_OFFICER`, `FINANCE_OFFICER`, `EMPLOYEE`).

## Integrations & notifications

- **WebClient** clients: `AuthClient`, `HrmClient`, `MmsClient`, `FmsClient` (+ fallbacks when upstream is down)
- **Notification consumer**: RabbitMQ → `NotificationEventListener` → Email/SMS services (log transport if SMTP not configured)
- Finance / inventory listeners call FMS / MMS clients on invoice, payment, and order-confirm events

## RabbitMQ events

Exchange: `erp.events` (topic)

| Routing key | Trigger |
|-------------|---------|
| `sales.order.confirmed` | `PATCH /api/sales-orders/{id}/confirm` |
| `invoice.created` | Create invoice |
| `payment.received` | Create payment |
| `lead.created` / `lead.qualified` / `lead.converted` | Lead lifecycle |

## Main API areas (via gateway :8080)

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

## Architecture

```
Client → API Gateway :8080
            ├─ /api/auth/**  → Auth Service :8081  (issues JWT, auth_users)
            └─ /api/**       → CRM :8082           (validates JWT, domain CRUD)
                                  ├─ Neon Postgres (Flyway)
                                  └─ RabbitMQ erp.events → notification / finance / inventory
```

## Useful commands

```bash
.\mvnw.cmd clean package
.\mvnw.cmd test
.\mvnw.cmd spring-boot:run
cd auth-service && ..\mvnw.cmd spring-boot:run
cd api-gateway && ..\mvnw.cmd spring-boot:run
docker compose up -d rabbitmq
```
