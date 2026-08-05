# INSA CRM Backend — Technical Lead Presentation Brief

**Service:** `crm-backend`  
**Bounded context:** Customer Relationship Management (CRM)  
**Phase posture:** Extracted microservice foundation (ERP Phase 2) with Auth Service + API Gateway in-repo  
**Audience use:** Live talk track for Technical Leads / Instructor — read aloud, jump to paths when challenged  

---

## 1. Architecture Overview

### 1.1 Current runtime architecture

```
Client (React / Postman)
        │ HTTP  :8080
        ▼
┌───────────────────┐
│   API Gateway     │  api-gateway/
└─────────┬─────────┘
          ├─ /api/auth/** ──► Auth Service :8081  (issues JWT, auth_users)
          └─ /api/**      ──► CRM :8082           (validates JWT claims)
                                   │
                    ┌──────────────┼──────────────┐
                    │ JDBC         │ AMQP         │ WebClient
                    ▼              ▼              ▼
              Neon Postgres   RabbitMQ      HRM / MMS / FMS
              Flyway V1+V2    erp.events    (fallbacks if down)
```

**Communication legend:**
- **Solid / sync:** Client → Gateway → Auth|CRM → Neon  
- **Dashed / async:** CRM → RabbitMQ → notification (email/SMS) / finance (FMS) / inventory (MMS)

### 1.2 What is actually built vs stub / placeholder

| Area | State | What you can defend |
|------|--------|---------------------|
| Domain CRUD APIs | **Built** | Full CRM domain controllers with `@PreAuthorize` |
| Auth Service | **Built** | Separate `auth-service/` — login/register, seeds users, owns `auth_users` |
| CRM JWT resource server | **Built** | Validates shared-secret JWT from claims (no local User table) |
| API Gateway | **Built** | `api-gateway/` routes Auth + CRM |
| RabbitMQ events | **Built** | Order/invoice/payment/lead publishers + consumers |
| Notifications | **Built** | Email/SMS services (log transport if SMTP unset) |
| WebClient integrations | **Built** | Auth/HRM/MMS/FMS clients + fallbacks |
| Flyway | **Built** | V1 baseline + V2 drop `crm_users` |
| Actuator | **Built** | Health, info, Prometheus |
| Cache / schedulers / entity stubs | **Mostly stub** | Territory, PricingRule, etc. remain placeholders |

**One-sentence honesty line for leads:**  
*“CRM is a JWT resource server behind an API Gateway; Auth Service owns users; RabbitMQ plus WebClient wire notifications and MMS/FMS/HRM integrations.”*

---

## 2. Tech Stack Specification

Exact versions from `pom.xml` / Spring Boot BOM (`3.5.16`).

| Layer | Technology | Version / note |
|-------|------------|----------------|
| Language | Java | **21** (`<java.version>21</java.version>`) |
| Runtime framework | Spring Boot | **3.5.16** (parent POM) |
| Build | Apache Maven | Wrapper (`mvnw` / `mvnw.cmd`) |
| Web | `spring-boot-starter-web` | Boot-managed |
| Persistence | `spring-boot-starter-data-jpa` + Hibernate (via Boot) | Boot-managed |
| DB driver | PostgreSQL JDBC | Boot-managed runtime |
| Database (hosted) | Neon PostgreSQL | Shared team instance (`neondb`) |
| Schema migration | Flyway `flyway-core` + `flyway-database-postgresql` | Boot-managed |
| Security | `spring-boot-starter-security` | Boot-managed |
| JWT | JJWT `jjwt-api` / `jjwt-impl` / `jjwt-jackson` | **0.12.6** |
| Messaging | `spring-boot-starter-amqp` (RabbitMQ) | Boot-managed |
| Validation | `spring-boot-starter-validation` | Boot-managed |
| Mapping | MapStruct | **1.6.3** |
| Boilerplate | Lombok | **1.18.38** |
| Observability | `spring-boot-starter-actuator` | Boot-managed |
| Metrics | `micrometer-registry-prometheus` | Boot-managed |
| Test | `spring-boot-starter-test` | Boot-managed |
| Local broker | RabbitMQ Docker image | `rabbitmq:3.13-management` (`docker-compose.yml`) |

**Also in this repo:** Spring Cloud Gateway (`api-gateway/`), WebClient (`spring-boot-starter-webflux`), Mail starter.  
**Not used:** OpenFeign, Spring Cloud Config, OAuth2 Resource Server starter, Kafka, Redis.

---

## 3. Code Navigator (Where the Code Is)

Jump paths for live questions. Root package: `com.crm.crm_backend`.

### 3.1 Bootstrap & configuration

| Concern | Path |
|---------|------|
| Main application | `src/main/java/com/crm/crm_backend/CrmBackendApplication.java` |
| Runtime config (local, gitignored) | `src/main/resources/application.properties` |
| Team template (committed) | `src/main/resources/application.properties.example` |
| Flyway migrations | `src/main/resources/db/migration/` (V1 baseline, V2 drop crm_users) |
| RabbitMQ topology | `src/main/java/com/crm/crm_backend/config/CrmRabbitMQConfig.java` |
| Auth Service | `auth-service/` (port 8081) |
| API Gateway | `api-gateway/` (port 8080) |
| Docker RabbitMQ | `docker-compose.yml` |
| Project README | `README.md` |

### 3.2 Security & Auth

| Concern | Path |
|---------|------|
| Auth login/register | `auth-service/.../controller/AuthController.java` |
| Auth user seeding | `auth-service/.../config/AuthDataInitializer.java` |
| Auth JWT issue | `auth-service/.../security/JwtTokenService.java` |
| CRM security filter chain | `security/SecurityConfig/SecurityConfig.java` |
| CRM JWT validate only | `security/JwtService/JwtService.java` |
| CRM Bearer filter (claims → roles) | `security/JwtFilter/JwtAuthenticationFilter.java` |
| Auth health client | `integration/client/AuthClient.java` |

### 3.3 Domain API surface (controllers)

| Domain | Path |
|--------|------|
| Customers | `controller/CustomerController.java` → `/api/crm/customers` |
| Leads | `controller/LeadController.java` |
| Opportunities | `controller/OpportunityController.java` |
| Sales orders (+ confirm) | `controller/SalesOrderController.java` |
| Invoices | `controller/InvoiceController.java` |
| Payments | `controller/PaymentController.java` |
| Quotations | `controller/QuotationController.java` |
| Campaigns / Interactions / Segments | matching `controller/*Controller.java` |
| Audit | `controller/AuditTrailController.java` |
| Dashboard / Analytics / Reports | `DashboardController`, `AnalyticsController`, `ReportController` |

### 3.4 Services, persistence, events

| Concern | Path |
|---------|------|
| Core services | `service/core/*Service.java` (Customer, Lead, SalesOrder, Invoice, Payment, …) |
| Repositories | `repository/*Repository.java` |
| Entities | `model/entity/*.java` |
| Enums | `model/enums/*.java` |
| Routing keys | `event/CrmRoutingKeys.java` |
| Event envelope | `event/dto/DomainEventMessage.java` |
| Publishers (live) | `event/publisher/OrderEventPublisher.java`, `InvoiceEventPublisher.java`, `PaymentEventPublisher.java`, `LeadEventPublisher.java` |
| Consumers (live thin) | `event/listener/NotificationEventListener.java`, `FinanceEventListener.java`, `InventoryEventListener.java` |
| Audit aspect | `aspect/AuditLogAspect.java` |
| Exception API | `exception/CustomExceptionHandler.java` |

### 3.5 Integrations & notifications (implemented)

| Area | Path |
|------|------|
| WebClient config | `config/CrmWebClientConfig.java` |
| HRM/MMS/FMS/Auth clients | `integration/client/*Client.java` |
| Fallbacks | `integration/fallback/*` |
| Integration services | `service/integration/*` |
| Email / SMS / Notification | `notification/**` |
| Cache / async / scheduler (still thin) | `config/CrmCacheConfig.java`, `CrmAsyncConfig.java`, `CrmSchedulerConfig.java` |

### 3.6 Demo credentials (seeded by Auth Service)

| User | Password | Role |
|------|----------|------|
| `admin` | `admin123` | `ADMIN` |
| `sales` | `sales123` | `SALES_OFFICER` |
| `finance` | `finance123` | `FINANCE_OFFICER` |
| `employee` | `employee123` | `EMPLOYEE` |

HTTP Client samples: `test.http`

---

## 4. Instructor Defense Cheat Sheet

### Q1. Why is Auth inside CRM instead of a separate Auth Service like the ERP diagram?

**Say:** “For this phase we co-located JWT issue and validation inside CRM so the team could ship a complete security path quickly with shared Neon and role-based access. The design is extraction-ready: `AuthClient` is reserved, and CRM already validates Bearer tokens through a dedicated filter and `JwtService`, which becomes a pure resource-server concern once Auth is split out.”

---

### Q2. How does CRM securely validate JWT today — and how will it validate tokens from a future Auth Service?

**Say:** “Today every protected request hits `JwtAuthenticationFilter`, which extracts the Bearer token, verifies the HS256 signature and expiry with our configured secret, then loads authorities into the SecurityContext. When Auth is externalized, we keep the same filter contract and switch to a shared secret or JWKS from the Auth Service—CRM stops issuing tokens and only validates them.”

---

### Q3. Why RabbitMQ events for order/invoice/payment instead of synchronous Feign to FMS/MMS?

**Say:** “Confirmation, invoicing, and payment are cross-cutting side effects; publishing `sales.order.confirmed`, `invoice.created`, and `payment.received` decouples CRM from FMS/MMS availability and matches the ERP async bus. We still plan synchronous Feign/WebClient for request/response needs like stock checks, where the caller must block on an immediate answer.”

---

### Q4. Why sync Feign/WebClient later instead of making everything async over the bus?

**Say:** “Async messaging is ideal for fire-and-forget domain facts; it is a poor fit when the API must return a decisive result in the same HTTP request—for example ‘is stock available?’ or ‘is this employee active?’. Our target is hybrid: sync HTTP for queries that gate the transaction, async RabbitMQ for post-commit notifications and downstream ledger/inventory reactions.”

---

### Q5. How do you handle database schema changes and rollbacks with Flyway?

**Say:** “Flyway versions schema under `db/migration`; V1 is our baseline and Hibernate `ddl-auto` is `none`, so the database is migration-owned, not ORM-owned. Flyway does not auto-rollback applied versions in production—we roll forward with a new corrective migration (or restore from backup/branch in Neon), which is the standard safe practice for team databases.”

---

### Q6. What prevents Hibernate from drifting away from Flyway, and how do you observe production health?

**Say:** “With `spring.jpa.hibernate.ddl-auto=none` and Flyway enabled at startup, schema changes must enter as SQL migrations—developers cannot silently mutate Neon via entity edits. Operationally we expose `/actuator/health`, `/actuator/info`, and `/actuator/prometheus` for liveness and metrics scraping, which is our Phase-1 observability foothold toward Prometheus/Grafana.”

---

### Bonus rapid-fire (optional)

| Challenge | One-liner |
|-----------|-----------|
| “Where is the API Gateway?” | “Not deployed yet; CRM is the service behind the future gateway route `/api/crm/**`.” |
| “Is this a modular monolith?” | “No—this repo is a standalone CRM microservice; Phase 1 monolith was the ERP plan, we are implementing the CRM extract early.” |
| “Are integrations done?” | “Package structure and clients exist as stubs; RabbitMQ publishers/consumers are live for CRM domain events.” |
| “Secrets in git?” | “`application.properties` is gitignored; teammates copy `application.properties.example` and inject Neon/JWT/RabbitMQ values locally.” |

---

## 5. Live Demo Script (60–90 seconds)

1. Show architecture ASCII + say “sync REST + async RabbitMQ + Neon + Flyway + Actuator”.  
2. `POST /api/auth/login` → copy JWT.  
3. `GET /api/crm/customers` with Bearer token.  
4. `PATCH /api/sales-orders/{id}/confirm` → mention `sales.order.confirmed`.  
5. `GET /actuator/health` → `"status":"UP"`.  
6. Close with roadmap: Gateway → extract Auth → Feign to HRM/MMS/FMS → real Notification Service.

---

## 6. Next Improvements (if asked “what’s next?”)

1. **API Gateway** — single entry, routing, rate limiting, edge auth filter  
2. **Extract Auth Service** — CRM becomes JWT resource server only  
3. **OpenFeign / WebClient** — sync MMS stock, HRM employee, FMS posting  
4. **Hardened Flyway discipline** — V2+ additive migrations; Neon branch review for risky DDL  
5. **Notification Service** — consume `erp.events` for in-app/email  
6. **Security depth** — consistent `@PreAuthorize` on all mutating APIs; secret rotation  
7. **Ops** — Grafana dashboards on `/actuator/prometheus`  

---

*Document generated from the current `crm-backend` codebase for technical presentation use. Prefer truth over aspirational architecture when challenged.*
