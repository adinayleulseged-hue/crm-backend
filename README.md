# CRM Backend - INSA (Information Network Security Administration)

Spring Boot CRM microservice for the INSA ERP system. Auth follows the **ERP Keycloak standard**
(OAuth2 resource server). An optional API Gateway and legacy auth-service remain in-repo.

---

## 🎓 Developer Information

**Developer:** Adanech Leulseged  
**Institution:** INSA (Information Network Security Administration)  
**Project Status:** ✅ **COMPLETED**



---

## ✅ Project Completion Status

**Status:** Production Ready  
**Completion Date:** September 3, 2026  
**Final Version:** v3.0 - Phase 3A Complete

### Implementation Highlights

✅ **Full Microservices Architecture**
- API Gateway (Port 8090) with JWT validation
- Auth Service (Port 8081) - Legacy & Keycloak integration
- CRM Service (Port 8082) - Core business logic
- PostgreSQL Database (150 connection pool) - Neon hosted
- RabbitMQ Event Bus - Async domain events

✅ **Complete Sales Pipeline**
- Lead Management (Create → Qualify → Convert)
- Opportunity Tracking with weighted pipeline
- Quotation System with approval workflow
- Sales Order Management with inventory integration
- Invoice & Payment Processing
- Customer Relationship Management

✅ **Phase 3A Advanced Features**
- 📊 **Deal Pipeline** - Kanban board with drag-and-drop
- 📞 **Activity Logging** - Complete interaction tracking
- 🎯 **Quota Tracker** - Real-time sales performance monitoring
- 📈 **Analytics Dashboard** - Revenue forecasting & KPI tracking
- 🔔 **Smart Notifications** - Email/SMS alerts via RabbitMQ

✅ **Multi-Portal Frontend (Next.js 14)**
- Customer Portal - Self-service interface
- Sales Officer Portal - Deal management & activity tracking
- Sales Representative Portal - Lead & opportunity management
- Sales Manager Portal - Team oversight & analytics
- Finance Officer Portal - Invoice & payment management

✅ **Enterprise Security**
- Keycloak OAuth2/OIDC integration
- Role-based access control (RBAC)
- JWT token validation at API Gateway
- Secure password hashing (BCrypt)
- CORS & CSRF protection

✅ **Database & Performance**
- PostgreSQL with Flyway migrations
- HikariCP connection pooling (150 max connections)
- Optimized queries with JPA/Hibernate
- Database indexes on frequently queried fields

✅ **Integration & Events**
- RabbitMQ event-driven architecture
- WebClient integrations (HRM, MMS, FMS)
- Email notifications (SMTP)
- SMS notifications (configurable)
- Real-time data synchronization

✅ **Testing & Documentation**
- Comprehensive REST API testing
- Swagger/OpenAPI documentation
- End-to-end workflow verification
- Integration test suite
- Activity & sequence diagrams

### Production Configuration

**Backend Ports:**
- API Gateway: `:8090`
- Auth Service: `:8081`
- CRM Service: `:8082`
- Keycloak: `:8080`

**Frontend:**
- Next.js App: `:3000`

**Database:**
- PostgreSQL: `localhost:5433` (Neon hosted)
- Max Connections: 150 pool size, 10 min idle

**Message Queue:**
- RabbitMQ: `localhost:5672`
- User: `crm` / Password: `crm123`

### Test Credentials

**Sales Officer:**
- Username: `abem`
- Password: `123456`
- Portal: `/dashboard/sales-officer`

**Admin (Keycloak):**
- Username: `erp-admin`
- Roles: `admin`, `crm_user`, `fms_user`

---

## Services in this repo

| Service | Port | Role |
|---------|------|------|
| Keycloak (ERP-INSA) | **8080** | Identity provider — realm `erp` |
| `api-gateway/` | **8090** | Optional edge entry — routes `/api/**` → CRM (validates Keycloak JWT) |
| `auth-service/` | **8081** | **Legacy** — HS256 login/register (only if `CRM_AUTH_MODE=legacy`) |
| CRM (root) | **8082** | Keycloak resource server — CRM domain APIs |

## Stack

- Java 21, Spring Boot 3.5.16
- Spring Security OAuth2 Resource Server (Keycloak JWKS)
- Spring Cloud Gateway 2025.0 (optional)
- PostgreSQL (Neon shared team database) + Flyway
- RabbitMQ (async domain events)
- WebClient integrations (HRM / MMS / FMS)
- MapStruct, Lombok, Maven

## Prerequisites

- JDK 21+
- Maven Wrapper (`mvnw` / `mvnw.cmd`) at repo root
- Docker Desktop (RabbitMQ; Keycloak via ERP-INSA `docker compose`)
- Neon PostgreSQL connection details from the team
- Keycloak running with realm `erp` (see ERP-INSA `docs/architecture/keycloak-auth.md`)

## Setup

### 1. Local config

```bash
copy src\main\resources\application.properties.example src\main\resources\application.properties
```

Fill Neon URL/user/password. Set:

```properties
crm.security.auth-mode=keycloak
spring.security.oauth2.resourceserver.jwt.issuer-uri=http://localhost:8080/realms/erp
```

`application.properties` is gitignored — never commit secrets.

### 2. Start dependencies

```bash
# From ERP-INSA repo — Keycloak on :8080
docker compose up -d

# From this repo — RabbitMQ
docker compose up -d rabbitmq
```

### 3. Run CRM (+ optional gateway)

```bash
# Terminal 1 — CRM
.\mvnw.cmd spring-boot:run

# Terminal 2 — Gateway (optional)
cd api-gateway
..\mvnw.cmd spring-boot:run
```

Frontend / clients:

- Prefer calling CRM at **http://localhost:8082** with `Authorization: Bearer <Keycloak access token>`
- Or via gateway at **http://localhost:8090**

## Auth (ERP standard — Keycloak)

Login is owned by **Keycloak** (and the Next.js app in ERP-INSA). CRM does **not** issue tokens.

| Claim / role | Meaning |
|--------------|---------|
| `realm_access.roles` | Mapped to Spring authorities verbatim |
| `admin` | Full access |
| `crm_user` | CRM module access |
| `fms_user` | Allowed on CRM finance-ish endpoints (invoices/payments/audit) |
| `preferred_username` | Audit `performedBy` |

Use `@PreAuthorize("hasAuthority('crm_user')")` / `hasAnyAuthority('admin', 'crm_user')` — **not** `hasRole` (no `ROLE_` prefix).

### Get a token (local curl)

Enable Direct Access Grants on client `erp-frontend` in Keycloak, then:

```bash
TOKEN=$(curl -s -X POST http://localhost:8080/realms/erp/protocol/openid-connect/token \
  -d grant_type=password -d client_id=erp-frontend \
  -d client_secret=<secret> \
  -d username=erp-admin -d password=<password> | jq -r .access_token)

curl http://localhost:8082/api/leads -H "Authorization: Bearer $TOKEN"
```

Seeded Keycloak user (ERP-INSA realm export): `erp-admin` (reset password on first login) with all module roles.

### Legacy auth-service (optional)

Set `CRM_AUTH_MODE=legacy` and run `auth-service` if you need the old HS256 `/api/auth/login` flow. Default is Keycloak.

---

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
| Optional JWT at edge | `GATEWAY_JWT_ENABLED=true` + `KEYCLOAK_ISSUER_URI` |
| Rate limit | `GATEWAY_RATE_LIMIT_RPM` (default 120/IP/min) |
| Body size | `GATEWAY_MAX_CONTENT_LENGTH` (default 2MB) |
| CORS | `GATEWAY_CORS_ORIGINS` — comma-separated, not `*` |

## Observability

- Logs include `[correlationId]` (`X-Correlation-Id` header generated or accepted)
- Actuator health: DB + RabbitMQ (+ mail when SMTP configured)

## Security

CRM endpoints use `@PreAuthorize` with Keycloak realm authorities (`admin`, `crm_user`, `fms_user`).  
CORS uses explicit origins (SecurityConfig / gateway) — not `@CrossOrigin(*)`.

| Token source | Access |
|--------------|--------|
| Keycloak access token with `crm_user` or `admin` | CRM domain APIs |
| Keycloak access token with `fms_user` | CRM invoice/payment/audit endpoints (shared) |
| No / invalid token | `401 Unauthorized` |

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

## Main API areas (CRM :8082 or gateway :8090)

| Area | Base path |
|------|-----------|
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

### List filters, territories & CSV exports

Optional query filters on list endpoints (all combine with Spring `Pageable`):

| Resource | Filters |
|----------|---------|
| `GET /api/leads` | `q`, `status`, `assignedTo`, `territoryId`, `campaignId`, `createdFrom`, `createdTo` |
| `GET /api/customers` | `q`, `status`, `customerType`, `territoryId`, `city`, `country`, `createdFrom`, `createdTo` |
| `GET /api/quotations` | `q`, `status`, `customerId`, `opportunityId`, `issueFrom`, `issueTo`, `expiryFrom`, `expiryTo` |
| `GET /api/invoices` | `q`, `status`, `customerId`, `salesOrderId`, `dueFrom`, `dueTo`, `invoiceFrom`, `invoiceTo` |

`territoryId` on leads/customers links CRM records to `/api/territories`. Assign territory managers via `/api/territories` (`managerUsername` should match Keycloak `preferred_username`).

Lead auto-assignment (`LeadAssignmentService`): prefers the territory `managerUsername`, otherwise round-robins usernames from `crm.lead.assignment.officers` (comma-separated, default `sales`). Disable with `crm.lead.assignment.enabled=false`.

CSV exports (`text/csv`):

| Path | Content |
|------|---------|
| `GET /api/quotations/export.csv` | Quotation rows (same filters as list) |
| `GET /api/invoices/export.csv` | Invoice rows (same filters as list) |
| `GET /api/reports/export.csv` | Pipeline / revenue KPI summary (`generateReport` + forecasting fields) |

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

Also reachable via the gateway once routed. Use **Authorize** with a Keycloak access token.

## Architecture

```
Client (Next.js) → Keycloak :8080  (login, issues JWT)
                 → CRM :8082       (validates JWT via JWKS, domain CRUD)
                    optional:
                 → Gateway :8090 → CRM :8082
                                  ├─ Neon Postgres (Flyway)
                                  └─ RabbitMQ erp.events → notification / finance / inventory
```

## Useful commands

```bash
.\mvnw.cmd clean package
.\mvnw.cmd test
.\mvnw.cmd spring-boot:run
cd api-gateway && ..\mvnw.cmd spring-boot:run
docker compose up -d rabbitmq
```
