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
| POST | `/api/auth/login` | Access + refresh tokens |
| POST | `/api/auth/register` | Register a user (same token pair) |
| POST | `/api/auth/refresh` | Rotate refresh token; issue new access token |
| POST | `/api/auth/logout` | Blacklist access `jti` + revoke refresh tokens |
| GET | `/api/auth/me` | Current user profile (JWT required) |

Defaults: access token **15 minutes** (`JWT_EXPIRATION_MS`), refresh token **7 days** (`JWT_REFRESH_EXPIRATION_MS`).

CRM no longer owns users (`crm_users` dropped in Flyway `V2__drop_crm_users.sql`).  
CRM validates JWTs from Auth Service claims only (signature + expiry). Auth-service additionally rejects blacklisted access tokens on `/me` and other protected auth routes.

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

Response `data` includes `accessToken`, `refreshToken`, `expiresInMs`, `refreshExpiresInMs`.

Use `Authorization: Bearer <accessToken>` on CRM routes (see `test.http`).

## End-to-end sales pipeline (happy path)

Verified chain (run sequentially via `test.http`):

| Step | Action | Endpoint | Expected status / result |
|------|--------|----------|--------------------------|
| 1 | Create lead | `POST /api/leads` | Lead `NEW` |
| 2 | Qualify | `PATCH /api/leads/{id}/status?status=QUALIFIED` | Lead `QUALIFIED` |
| 3 | Convert | `PATCH /api/leads/{id}/convert` | Lead `CONVERTED`; customer + opportunity (`stage=NEW`); response includes `convertedCustomerId` / `convertedOpportunityId` |
| 4 | Create quotation | `POST /api/quotations` | Quotation `DRAFT` |
| 5 | Send (optional) | `PATCH /api/quotations/{id}/status?status=SENT` | Quotation `SENT` |
| 6 | Accept | `PATCH /api/quotations/{id}/accept` | Quotation `ACCEPTED`; linked sales order `DRAFT` (response is the order) |
| 7 | Confirm order | `PATCH /api/sales-orders/{id}/confirm` | Order `APPROVED`; RabbitMQ `sales.order.confirmed` (SKU/qty) |
| 8 | Invoice | `POST /api/invoices` | Invoice `DRAFT` (`paidAmount=0`); order must be `APPROVED`+ |
| 9 | Pay (full) | `POST /api/payments` | Payment `COMPLETED`; invoice → `PAID` |

### Status cheat sheet

| Entity | Field | Happy-path values | Notes |
|--------|-------|-------------------|-------|
| Lead | `status` | `NEW` → `QUALIFIED` → `CONVERTED` | Convert only from `QUALIFIED` |
| Opportunity | `stage` | `NEW` (at convert) | Not required to close before quote |
| Quotation | `status` | `DRAFT` → (`SENT`) → `ACCEPTED` | Accept allowed from `DRAFT` or `SENT` |
| Sales order | `status` | `DRAFT` → `APPROVED` | Confirm publishes inventory event |
| Invoice | `status` | `DRAFT` → `PAID` | Partial pay moves `DRAFT`/`SENT` → `PENDING` |
| Payment | `status` | `COMPLETED` on create | Amount must be `> 0` and `≤` invoice balance |

Transitions are enforced by `DomainStatusGuard`. Full request bodies and comments live in `test.http`.

## Integrations & notifications

- **WebClient** clients: `AuthClient`, `HrmClient`, `MmsClient`, `FmsClient` (+ fallbacks when upstream is down)
- **Email**: SMTP via `spring.mail.*` — set host/user/password for real delivery; `crm.notification.email.allow-log-fallback=false` in production
- **SMS**: off by default (`crm.notification.sms.enabled=false`)
- Finance / inventory listeners call FMS / MMS clients on invoice, payment, and order-confirm events

## Gateway hardening

| Feature | Config |
|---------|--------|
| Optional JWT at edge | `GATEWAY_JWT_ENABLED=true` + shared `JWT_SECRET` |
| Rate limit | `GATEWAY_RATE_LIMIT_RPM` (default 120/IP/min) |
| Body size | `GATEWAY_MAX_CONTENT_LENGTH` (default 2MB) |
| CORS | `GATEWAY_CORS_ORIGINS` — comma-separated, not `*` |

## Observability

- Logs include `[correlationId]` (`X-Correlation-Id` header generated or accepted)
- Actuator health: DB + RabbitMQ (+ mail when SMTP configured)

## Security

Mutating CRM endpoints use `@PreAuthorize` (`ADMIN`, `SALES_OFFICER`, `FINANCE_OFFICER`, `EMPLOYEE`).  
CORS uses explicit origins (SecurityConfig / gateway / auth) — controllers no longer use `@CrossOrigin(*)`.

| Auth endpoint | Access |
|---------------|--------|
| `POST /api/auth/login` | Public |
| `POST /api/auth/register` | Public for `EMPLOYEE` / `SALES_OFFICER` / `FINANCE_OFFICER` only; elevated roles need ADMIN JWT |
| `POST /api/auth/refresh` | Public (valid refresh token required in body) |
| `POST /api/auth/logout` | Public (accepts Bearer access and/or refresh token body) |
| `GET /api/auth/me` | JWT required |

Gateway JWT defaults **on** (`GATEWAY_JWT_ENABLED=true`). Set `INTEGRATION_ALLOW_FALLBACK=false` in production so MMS/FMS do not fake success.

## RabbitMQ events

Exchange: `erp.events` (topic)

| Routing key | Trigger |
|-------------|---------|
| `sales.order.confirmed` | `PATCH /api/sales-orders/{id}/confirm` |
| `invoice.created` | Create invoice |
| `invoice.overdue.reminder` | Nightly/daily overdue invoice reminder scheduler |
| `payment.received` | Create payment |
| `lead.created` / `lead.qualified` / `lead.converted` | Lead lifecycle |

## Main API areas (via gateway :8080)

| Area | Base path |
|------|-----------|
| Auth | `/api/auth` |
| Customers | `/api/customers` (alias: `/api/crm/customers`) |
| Leads | `/api/leads` |
| Opportunities | `/api/opportunities` |
| Sales orders | `/api/sales-orders` |
| Invoices | `/api/invoices` |
| Payments | `/api/payments` |
| Quotations | `/api/quotations` |
| Pricing | `/api/pricing` |
| Territories | `/api/territories` |
| Campaigns | `/api/campaigns` |
| Interactions | `/api/interactions` |
| Segments | `/api/segments` |
| Audit | `/api/audit-trails` |
| Dashboard / Analytics / Reports | `/api/dashboard`, `/api/analytics`, `/api/reports` |

### Response envelope

All CRM endpoints return:

```json
{
  "success": true,
  "message": "optional human message",
  "data": { },
  "errors": null,
  "timestamp": "2026-08-07T..."
}
```

Frontend should read payload from `data` (including Spring `Page` objects for list endpoints). Errors from `CustomExceptionHandler` use the same shape with `success: false`.

### Pricing / territories / campaign metrics

- `POST /api/pricing/rules` — create discount/markup rules; `POST /api/pricing/apply` evaluates the best active rule
- `CRUD /api/territories` (+ `GET /api/territories/active`)
- `GET /api/campaigns/{id}/metrics` — live achievement rates + historical snapshots; auto-snapshot on complete

### Invoice reminders & KPIs

- `InvoiceReminderScheduler` marks past-due `SENT`/`PENDING` invoices as `OVERDUE` and publishes `invoice.overdue.reminder` (throttled ~daily)
- Dashboard / analytics / reports expose real `activeCustomers`, overdue counts, open invoice balance, weighted pipeline, and projected revenue (payments + open balances + probability-weighted opportunities)

### Campaign lifecycle

| Action | Endpoint | From → To |
|--------|----------|-----------|
| Activate | `PATCH /api/campaigns/{id}/activate` | `PLANNED` → `ACTIVE` |
| Complete | `PATCH /api/campaigns/{id}/complete` | `ACTIVE` → `COMPLETED` |
| Cancel | `PATCH /api/campaigns/{id}/cancel` | `PLANNED`/`ACTIVE` → `CANCELLED` |

`actualLeads` increments when a lead is created with `campaignId`.  
`actualRevenue` increments when a campaign-attributed opportunity reaches `CLOSED_WON`.  
`CampaignScheduler` auto-activates by `startDate` and auto-completes by `endDate` daily.

### Segment membership

| Action | Endpoint |
|--------|----------|
| List members | `GET /api/segments/{id}/members` |
| Add member | `POST /api/segments/{id}/members` `{ "customerId": N }` |
| Remove member | `DELETE /api/segments/{id}/members/{customerId}` |
| Evaluate criteria | `POST /api/segments/{id}/evaluate` |

### API docs (SpringDoc)

- Swagger UI: http://localhost:8082/swagger-ui.html  
- OpenAPI JSON: http://localhost:8082/v3/api-docs  

Also reachable via the gateway once routed. Use **Authorize** with a JWT from `/api/auth/login`.

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
