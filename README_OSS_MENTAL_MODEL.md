# OSS Contributor Mental Model  
**Composable Business Application Platform (CBAP)**

> **TL;DR**  
> CBAP is a **metadata-driven runtime**, not an ERP and not a low-code app builder.  
> **Metadata defines behavior. Code executes metadata.**

---

## What You Are Building

You are building:
- A **business application kernel**
- A **safe runtime** for metadata-defined behavior
- A platform where **data, workflow, rules, UI, and authorization are declarative**

You are **not** building:
- Domain logic (HR, Finance, Procurement, etc.)
- Customer-specific features
- Hardcoded workflows, validations, or screens
- Visual designers or opinionated UX

If it looks like business logic, it probably does not belong in OSS.

---

## Core Execution Model

Every operation follows this order:

```
Authorization
 → Validation & Rules
 → Measure Evaluation (read-only)
 → Workflow Transition
 → Persistence (atomic)
 → Audit & Telemetry
 → Replication Events
```

No step is optional.  
No partial success is allowed.

---

## Determinism & Safety (Non‑Negotiable)

OSS logic MUST be:
- Deterministic
- Side‑effect free
- Version‑aware
- Replayable

Rules and measures:
- cannot modify data
- cannot call external systems
- cannot perform I/O
- run in a sandboxed execution model

---

## Metadata Is the Source of Truth

Everything important is metadata:
- Entities → data shape
- Properties → fields, labels, permissions
- Workflows → states & transitions
- Validations → constraints
- Measures → reusable calculations
- Navigation → discoverability
- Documents → governed knowledge

Code must **never** encode business assumptions.

---

## Ownership & Topology

- Facilities **own operational data**
- HQ **owns global / policy data**
- Upstream nodes **review, not edit**
- Decisions flow back to the owner

If ownership rules are violated, replication will break.

---

## UI & Navigation

- UI is **auto‑generated**, not hand‑crafted
- No hardcoded menus or screen hierarchies
- Navigation is metadata‑driven and configurable
- Global **Search** and **Create / New** are primary entry points

OSS UX exists to enforce rules and consistency, not to be pixel‑perfect.

---

## OSS vs Paid Boundary

OSS includes:
- Runtime engines
- Metadata execution
- Base UX
- Offline‑safe foundations
- Audit, telemetry, and replication primitives

OSS excludes:
- Visual designers
- Advanced dashboards and analytics
- Native mobile applications
- Enterprise authentication (SAML, OAuth, etc.)
- AI assistants
- External system integrations

If it primarily enables scale, governance, or monetization, it is likely **paid**.

---

## The Reference App Test

Every OSS capability must support building:
- A Purchase Order
- An Approval Workflow
- A Budget Check (via Measure)
- A Scheduled Audit Task
- A Controlled SOP / Policy Document

If the reference app needs custom code, the platform is incomplete.

---

## Golden Rule

> **OSS builds the engine.  
> Solutions, consulting, and business value live above it.**

If you follow this mental model, CBAP will remain:
- composable
- upgrade‑safe
- contributor‑friendly
- enterprise‑credible