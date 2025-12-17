# CBAP OSS – Technology Stack & Project Structure Guide

This document defines the **finalized technology choices** and **project structure** for the CBAP OSS platform.
Its goal is to eliminate ambiguity for contributors and ensure long-term maintainability, extensibility, and enterprise readiness.

This is a **living guide**, but changes to core stack choices should be rare and deliberate.

---

## 1. Guiding Principles for Technology Choices

All technology decisions are driven by the following principles:

1. **Enterprise-grade & boring by design**
   - Mature ecosystems
   - Long-term support
   - Predictable behavior

2. **On‑prem + Cloud parity**
   - Must run inside customer firewalls
   - Must support offline and constrained networks

3. **OSS-first friendliness**
   - Strong open-source tooling
   - Avoid vendor lock-in

4. **Composable & testable**
   - Clear module boundaries
   - Deterministic runtime behavior

5. **Future-proof for paid extensions**
   - Mobile, analytics, AI, identity, scale

---

## 2. Backend Technology Stack (Final)

### 2.1 Language & Runtime

- **Java 21+**
- **Spring Boot (latest LTS)**

Why:
- Strong enterprise adoption
- Excellent OSS ecosystem
- First-class support for security, observability, and modularization
- Long lifecycle suitable for regulated customers

---

### 2.2 Core Backend Frameworks

| Concern | Technology |
|------|-----------|
| Web / API | Spring Web (REST) |
| Dependency Injection | Spring Core |
| Configuration | Spring Boot Config |
| Validation | Jakarta Validation (where applicable) |
| Security | Spring Security (RBAC baseline) |
| Scheduling | Spring Scheduler |
| Async / Events | Spring Events / Executor |
| Serialization | Jackson |
| Build | Maven |

---

### 2.3 Persistence & Data

| Area | Choice |
|----|------|
| Primary DB | PostgreSQL |
| ORM | JPA (Hibernate) – limited, controlled usage |
| Migrations | Flyway |
| JSON Storage | PostgreSQL JSONB |
| Search Index | OpenSearch (or Elasticsearch-compatible OSS) |

Notes:
- CBAP is **metadata-driven**, not ORM-centric
- JPA is used for infrastructure entities, not business entities
- Business records are stored schemaless (JSONB + metadata)

---

### 2.4 Observability & Ops (OSS)

| Area | Choice |
|----|------|
| Logging | SLF4J + Logback |
| Metrics | Micrometer |
| Tracing | OpenTelemetry (foundation) |
| Health | Spring Actuator |
| Packaging | Docker (optional) |

---

## 3. Frontend Technology Stack (Final)

### 3.1 Core Choices

- **React (latest LTS)**
- **TypeScript (strict mode)**

Why:
- Large ecosystem
- Strong hiring pool
- Mature tooling for complex UI

---

### 3.2 Frontend Libraries

| Concern | Library |
|------|--------|
| UI Components | MUI (Material UI) |
| Forms | React Hook Form |
| State (Server) | React Query |
| Routing | React Router |
| Charts (OSS) | Recharts / Chart.js |
| Tables | MUI DataGrid |
| i18n | i18next |
| Build Tool | Vite |

Notes:
- UI is **metadata-driven**
- No hardcoded business screens
- Screens are generated from entity definitions

---

### 3.3 Mobile Strategy

OSS:
- Responsive web UI
- Optional PWA support

Paid:
- Native iOS / Android apps
- Offline-first sync
- Push notifications

---

## 4. API Design Principles

- REST-first (no GraphQL in OSS)
- Versioned APIs (`/api/v1`)
- Stateless requests
- Correlation ID propagation (required)
- Explicit error models

API contracts are **stable and backward compatible**.

---

## 5. Project Structure (Monorepo)

The project follows a **clear modular monorepo structure**.

```
cbap-oss/
│
├── backend/
│   ├── cbap-core/                 # Core runtime engine
│   │   ├── metadata/              # Entity, property, workflow models
│   │   ├── rules/                 # Validation & rule engine
│   │   ├── measures/              # Declarative function engine
│   │   ├── workflow/              # Workflow runtime
│   │   ├── auth/                  # RBAC & authorization
│   │   ├── replication/           # Outbox / Inbox framework
│   │   ├── telemetry/             # Telemetry & observability
│   │   ├── licensing/             # License & entitlement enforcement
│   │   └── scheduler/             # Task & schedule engine
│   │
│   ├── cbap-api/                  # REST API layer
│   ├── cbap-persistence/          # DB access & repositories
│   ├── cbap-search/               # Search indexing & queries
│   ├── cbap-security/             # AuthN/AuthZ integration
│   ├── cbap-bootstrap/            # Startup, node registration
│   └── cbap-app/                  # Spring Boot launcher
│
├── frontend/
│   ├── app-shell/                 # Application shell & navigation
│   ├── metadata-ui/               # Generated forms & lists
│   ├── workflow-ui/               # Tasks & approvals
│   ├── document-ui/               # Controlled documents
│   ├── dashboard-ui/              # Dashboards & widgets
│   └── shared/                    # Common components & utilities
│
├── docs/
│   ├── SPEC.md
│   ├── OSS_MENTAL_MODEL.md
│   ├── EXPRESSION_LANGUAGE_V0.md
│   └── TECH_STACK_AND_PROJECT_STRUCTURE_GUIDE.md
│
├── infra/
│   ├── docker/
│   ├── compose/
│   └── scripts/
│
└── examples/
    └── reference-app/             # Procurement + SOP demo
```

---

## 6. What Does NOT Belong in OSS Code

Contributors should NOT introduce:
- Customer-specific logic
- Hardcoded workflows or approvals
- UI assumptions about business domains
- External system integrations
- Non-deterministic logic

These belong in:
- Reference apps
- Paid editions
- Consulting solutions

---

## 7. Contribution Expectations

When contributing code:
- Respect module boundaries
- Prefer metadata over logic
- Add tests for deterministic behavior
- Do not bypass authorization, validation, or workflow
- Update documentation when adding new core capabilities

---

## 8. Final Note

This stack is intentionally **conservative**.
The value of CBAP is not novelty—it is **composability, safety, and trust**.

If a proposed change increases cleverness but reduces predictability, it will likely be rejected.
