# Open Source Core Specification  
## Composable Business Application Platform (CBAP)

Version: 0.1  
Status: Draft (Engineering Baseline)
> New contributors should read `README_OSS_MENTAL_MODEL.md` before diving into implementation.
---

## 1. Purpose

This document defines the **Open Source Core** of the Composable Business Application Platform (CBAP).

The OSS core provides a **metadata-driven runtime** for building business applications where:
- Data models are schema-flexible
- Workflows are user-defined
- Authorization is fine-grained
- UX is auto-generated
- Systems can evolve safely over time

This repository **must NOT** include enterprise-only or monetized features.

---

## 2. Core Design Principles

1. **Metadata over Code**
   - Business behavior is defined declaratively
   - Code executes metadata, never embeds business rules

2. **Schema is Versioned**
   - No destructive schema changes
   - Old data must remain readable

3. **Workflow is First-Class**
   - Entity state determines allowed actions

4. **Authorization is Data-Aware**
   - Permissions can exist at entity and property level

5. **Composable, Not Opinionated**
   - No domain assumptions (HR, Finance, etc.)

6. **Global-ready (i18n)**
   - Labels and UI strings support localization via keys and locale dictionaries

7. **Mobile-ready**
   - The platform supports mobile workflows via responsive UX and API-first design, with optional native mobile capabilities in paid editions

---

## 3. High-Level Architecture

```text
+------------------------------+
| Presentation Layer           |
| (Web UX + APIs)              |
| (Optional Mobile Clients)    |
+--------------▲---------------+
               |
+--------------┴---------------+
| Application Engine           |
| - Entity Runtime             |
| - Workflow Engine            |
| - Authorization Engine       |
| - Validation/Rule Engine     |
| - Task & Scheduler Engine    |
| - Audit/History Engine       |
| - Replication Agent (base)   |
| - Telemetry/Observability    |
| - Licensing/Entitlements     |
+--------------▲---------------+
               |
+--------------┴---------------+
| Metadata Store               |
| - Entity Definitions         |
| - Workflow Definitions       |
| - Measures (Declarative Fn)  |
| - Document Definitions      |
| - Schema & Screen Versions   |
+--------------▲---------------+
               |
+--------------┴---------------+
| Data Store & Search Index    |
| - Schemaless Storage         |
| - Denormalized Index         |
| - Outbox/Inbox Logs          |
| - Telemetry Store            |
+------------------------------+
```

---

## 4. Core Concepts

### 4.1 Entity

An **Entity** represents a business object defined entirely via metadata.

#### Entity Attributes
- `entityId`
- `name`
- `description`
- `schemaVersion`
- `screenVersion`
- `workflowId`
- `authorizationModel`

---

### 4.2 Property

A **Property** is a field belonging to an entity.

#### Supported Property Types
- string
- number
- date
- boolean
- singleSelect
- multiSelect
- reference (to another entity)
- calculated (read-only)

#### Property Metadata
- `propertyId`
- `name`
- `label`
- `type`
- `required`
- `readOnly`
- `referenceEntityId` (if applicable)
- `calculationExpression` (if calculated)
- `denormalize` (boolean)
- `authorizationRules`
- `labelKey` (i18n key) and `labels` (optional per-locale overrides)

---

### 4.3 Calculated Fields

- Read-only
- Derived via expression language
- Recomputed on entity change
- May be indexed if denormalized

Example:

totalAmount = quantity * unitPrice

---

### 4.4 Validation & Rule Engine (Core)

The platform must support **declarative validations and business rules** defined as part of entity and workflow metadata.

Validations are evaluated:
- At UX/form level (pre-submit)
- At API/service level (authoritative)
- During workflow transitions

Rules must be **metadata-driven**, not hardcoded.

#### 4.4.1 Validation Types

**Field-Level Validations**
- Required
- Min / Max (number, date)
- Length (string)
- Regex / pattern
- Allowed value set
- Custom expression

**Entity-Level Validations**
- Cross-field validation
- Conditional validation (if/then)
- Aggregate validation within the entity

**Cross-Entity Validations**
- Reference existence
- Lookup-based constraints
- Read-only cross-entity checks

#### 4.4.2 Validation Metadata (Example)

```yaml
validationId: "budgetCheck"
scope: "workflow-transition"
trigger:
  entity: "PurchaseOrder"
  transition: "Approve"
ruleType: "expression"
expression: "requestedAmount <= measure('budget.available', { department: department, period: 'current' })"
errorMessage: "Insufficient budget for approval"
```

#### 4.4.3 Execution Guarantees
- Validations MUST execute on the server
- UX may pre-validate but cannot bypass rules
- Validation failures must block persistence or transition

---

### 4.5 Measures / Declarative Functions (Core)

A **Measure** is a named, declarative, read-only function defined via metadata.
Measures encapsulate reusable business calculations that can be referenced by:
- Validations
- Workflow rules
- Search filters
- Dashboards (basic usage in OSS)

Measures provide a safe alternative to user-defined code.

#### 4.5.1 Measure Characteristics

Measures MUST be:
- Read-only
- Deterministic
- Side-effect free
- Executed within the rule sandbox
- Defined using expressions and aggregates only

Measures MUST NOT:
- Modify data
- Call external systems
- Perform network or file I/O

#### 4.5.2 Measure Metadata Schema (Canonical Example)

```yaml
measureId: "budget.available"
name: "Available Budget"
description: "Remaining available budget for a department and period"
version: 1
parameters:
  - name: "department"
    type: "reference:Department"
  - name: "period"
    type: "string"
    default: "current"
returnType: "number"

dependsOn:
  - entity: "Budget"
    fields: ["allocatedAmount", "department", "period"]
    dimensions: ["department", "period"]
  - entity: "PurchaseOrder"
    fields: ["totalAmount", "department", "state"]
    dimensions: ["department", "period"]

definition:
  type: "expression"
  expression: |
    sum("Budget", "allocatedAmount",
        { department: $department, period: resolvePeriod($period) })
    -
    sum("PurchaseOrder", "totalAmount",
        { department: $department, stateIn: ["Approved","Issued"] })
```

#### 4.5.3 Measure Versioning Rules
- Measures are versioned independently
- Validations/rules SHOULD reference a specific measure version
- New versions MUST NOT break existing signatures (parameters/returnType)
- Old versions remain executable for historical data

#### 4.5.4 Measure Evaluation Lifecycle
Measures are evaluated:
- Lazily (on demand)
- Within the rule evaluation context
- With parameters resolved from entity fields or constants

Evaluation failures:
- Must return structured errors
- Must NOT cause partial state changes

#### 4.5.5 Caching Expectations

The platform MAY cache measure results if:
- Inputs are identical (measureId, version, parameter values)
- Underlying dependent data has not changed

Caching rules:
- Cache scope: request / transaction (minimum)
- Optional short-lived cache (configurable) may be added
- Cache invalidation triggered by:
  - Changes to dependent entities
  - Measure definition version change

Caching MUST NOT:
- Return stale data across workflow transitions
- Bypass authorization rules

#### 4.5.5.1 Dependency-Based Invalidation

Measures SHOULD declare dependencies using `dependsOn` to enable safe caching and invalidation.

Rules:
- A dependency lists the entities and fields whose changes may affect the measure result
- A dependency MAY declare `dimensions` (e.g., `department`, `period`) that define the measure's parameter-driven partitioning
- If `dimensions` are declared, invalidation SHOULD be selective:
  - Only cached results whose parameter values match the changed record's dimension values should be invalidated
- If `dimensions` are not declared, invalidation MUST be conservative (invalidate all cached results for that measure/version)
- When a measure version changes, all cached results for prior versions MUST be treated as invalid
- Dependencies MUST be treated as advisory for optimization; correctness MUST NOT rely solely on them

#### 4.5.5.2 Dimension Matching Rules

Selective invalidation relies on matching changed records to cached measure parameter sets.

Guidelines:
- Dimension names MUST correspond to measure parameter names or resolvable parameter aliases (e.g., `period` derived via `resolvePeriod()`)
- For reference dimensions (e.g., `department`), matching is performed on stable identifiers (not display labels)
- If a dimension value cannot be determined for a changed record, the engine MUST fall back to conservative invalidation
- Implementations SHOULD maintain a dependency index to map (measureId, version, dimensionValues) -> cache keys

#### 4.5.6 Built-in Aggregate Functions (OSS)

Measures and expressions may use the following built-in functions:
- `sum(entity, field, filter)`
- `count(entity, filter)`
- `exists(entity, filter)`
- `in(value, list)`
- `coalesce(value, default)`
- `now()`, `today()`
- `resolvePeriod(periodSpecifier)`
- `measure(measureId, params)` (invocation)

#### 4.5.7 Usage Examples

**A. Budget Availability (Approval Rule)**

```text
requestedAmount <= measure("budget.available", {
  department: department,
  period: "current"
})
```

**B. Credit Limit Check (Customer Order)**

```text
orderAmount <= measure("customer.credit.available", { customer: customer })
```

**C. Stock Availability (Inventory Issue)**

```text
requestedQty <= measure("stock.available", { item: item, location: location })
```

---

### 4.6 Audit & Record History (Core)

Beyond workflow transition audit trails, the platform MUST maintain a full, queryable history of changes for entity instances.

Required capabilities:
- Record creation, update, delete events are audited
- Field-level diff capture (from -> to), including null handling
- Actor identity (userId), timestamp, and source (UI/API)
- Workflow transitions link to the same audit stream
- Audit history is viewable per record and queryable by admins

Minimum audit event schema:
- `eventId`, `entityId`, `recordId`
- `eventType` (CREATE/UPDATE/DELETE/TRANSITION)
- `timestamp`, `actorUserId`
- `changes[]` (fieldName, oldValue, newValue)
- `schemaVersion`, `screenVersion`, `workflowState`

Audit requirements:
- Audit events MUST be immutable
- Audit storage MUST be append-only
- Authorization applies when viewing audit events (at least entity read permission)

---

### 4.7 Attachments (Core)

Entity instances MUST support attachments for common business scenarios (invoices, quotations, inspection photos, etc.).

Required capabilities:
- Attachment upload/download linked to an entity record
- Metadata: filename, contentType, size, uploadedAt, uploadedBy
- Authorization inherits from parent record (read/write)
- Attachments are included in audit stream (upload/remove events)

OSS scope:
- Storage backend may be local or pluggable
- Virus scanning and advanced DLP are out of scope

---

### 4.8 Record Lifecycle: Soft Delete, Archive & Retention (Core)

The platform MUST provide record lifecycle controls independent of workflow state.

Required capabilities:
- Soft delete (recoverable) as default delete behavior
- Hard delete (irreversible) restricted to admins
- Archive flag/state that removes record from default listings
- Basic retention support: ability to mark records with retention period metadata

Rules:
- Soft-deleted records MUST be excluded from default search/list results
- Audit history MUST remain accessible for deleted/archived records (subject to authorization)
- Hard delete MUST be audited (admin-only) and should optionally require confirmation reason/comment

---

### 4.9 Import/Export (Core)

The platform MUST provide basic data portability for onboarding and adoption.

Required capabilities:
- CSV import into an entity with field mapping
- CSV export of entity list/search results (respecting authorization)
- Export entity metadata (schema/workflow/measures) as JSON/YAML for backup and migration

OSS scope:
- Advanced ETL, transformations, and scheduled imports are out of scope

---

### 4.10 Concurrency & Transaction Semantics (Core)

The platform MUST define consistent concurrency and atomicity rules for edits and workflow transitions.

Required capabilities:
- Optimistic concurrency control (record version/ETag) to prevent lost updates
- Atomic workflow transition: validate -> evaluate measures -> persist -> audit (all-or-nothing)
- Idempotency for workflow transition requests (same transition request should not double-apply)

Rules:
- If optimistic concurrency fails, the API MUST return a clear conflict error
- No partial state transitions are permitted on validation failure

---

### 4.11 Organization Topology & Facility Partitioning (Core)

The platform MUST support organizational topology required for real-world deployments:
- HQ (root)
- Regional Office(s) (optional intermediate level)
- Facility/Site(s) (leaf nodes where operational data is produced)

#### 4.11.1 Topology Entities

The platform MUST include a minimal set of core entities (system-managed):
- `OrgUnit`
  - `orgUnitId` (global unique ID)
  - `tenantId`
  - `type` (HQ | REGION | FACILITY)
  - `name`
  - `parentOrgUnitId` (null for HQ)
  - `timezone`
  - `status` (ACTIVE | INACTIVE)
- `Facility`
  - `facilityId` (alias of OrgUnit where type=FACILITY)
  - `regionOrgUnitId` (optional parent linkage)

Rules:
- Exactly one HQ OrgUnit exists per tenant
- Regions may have one or more Facilities
- Facilities belong to exactly one parent OrgUnit (HQ or Region)

#### 4.11.2 Record Partitioning Fields

All records in the system MUST include:
- `tenantId`
- `facilityId` (nullable ONLY for HQ-owned/global records)

Additionally, entities MUST declare a `scope` policy:
- `LOCAL`  : data is owned and written at a Facility; replicated upward
- `GLOBAL` : data is owned and written at HQ; replicated downward
- `SHARED` : tenant-wide reference/master data; ownership must be configured (default HQ)

Defaults:
- Operational logs (e.g., inspections, tank logs) are LOCAL
- Policies, limits, budgets are GLOBAL (HQ-owned)
- Master/reference data (vendors, items) are SHARED (default HQ)

---

### 4.12 Replication Foundations (Core)

The platform MUST include a base replication mechanism designed for intermittent connectivity and firewall constraints.

#### 4.12.1 Replication Model (Base)

- Facilities are authoritative for `LOCAL` data
- HQ is authoritative for `GLOBAL` data
- Replication is asynchronous and resilient to offline periods
- The system uses an Outbox/Inbox pattern with idempotent processing

#### 4.12.2 Outbox/Inbox Logs

Each node (HQ/Region/Facility) MUST maintain:
- `OutboxEvent` (append-only)
- `InboxEvent` (append-only)

Minimum OutboxEvent fields:
- `eventId` (ULID/UUIDv7)
- `tenantId`, `sourceOrgUnitId`, `sourceFacilityId` (optional)
- `targetOrgUnitId` (nullable for broadcast to parent)
- `occurredAt`
- `eventType` (RECORD_UPSERT | RECORD_DELETE | TRANSITION | TASK_UPSERT | TASK_DECISION | COMMENT_ADD | ATTACHMENT_META)
- `entityId`, `recordId` (if applicable)
- `schemaVersion`
- `payload` (serialized, versioned)

Minimum InboxEvent fields:
- `eventId` (same as Outbox)
- `receivedAt`
- `status` (RECEIVED | APPLIED | FAILED)
- `error` (optional)

Rules:
- Events MUST be immutable and append-only
- Processing MUST be idempotent (same eventId can be safely re-applied)
- Applying an event MUST write to the audit stream where appropriate

#### 4.12.3 Connectivity & Firewall Constraints

The platform MUST support deployments where nodes are behind firewalls and cannot accept inbound connections.

Supported connectivity patterns:
- **Outbound-only from Facility to HQ/Region** (recommended): Facility polls/pushes events to a configured upstream URL when online
- **Store-and-forward**: Facility continues operating offline; events queue in Outbox until connectivity returns
- **Manual transfer (fallback)**: export/import of event bundles as files for air-gapped environments (optional but recommended)

Rules:
- Replication MUST NOT require inbound connections to Facilities
- All replication transport MUST be authenticated and encrypted (e.g., HTTPS); enterprise auth enhancements are out of scope

#### 4.12.4 Multi-Level Aggregation (HQ -> Region -> Facility)

The topology supports multiple aggregation tiers:
- Facility replicates to its parent (Region or HQ)
- Region may aggregate and forward to HQ

OSS scope:
- Base forwarding and idempotent apply
- Advanced conflict resolution UI, monitoring dashboards, and automated repair are out of scope


#### 4.12.5 Conflict Policy (Base)

To prevent rewrites later, the platform MUST define default conflict rules:
- LOCAL entities: writes are allowed only at the owning Facility; upstream nodes apply as read-only
- GLOBAL entities: writes allowed only at HQ; downstream nodes apply as read-only
- SHARED entities: default HQ-owned; configurable ownership is future scope

If an upstream receives a write for a record it does not own, it MUST:
- Reject (do not apply) and record a FAILED InboxEvent with reason

#### 4.12.6 Cross-Org Approvals (Base)

Real-world workflows may require approvals across OrgUnits (Facility -> Region -> HQ) while preserving record ownership.

Principles:
- The owning Facility remains the system of record for `LOCAL` operational entities (e.g., PurchaseOrder)
- Upstream OrgUnits (Region/HQ) receive a read-only projection for review
- Upstream actors do not edit the record; they issue decisions (approve/reject/request-changes)
- Decisions are replicated back to the owning Facility and applied as workflow transitions there

##### A) Routing & Tasks
- When a record enters an approval step that requires upstream review, the owning node MUST create a Task assigned to a user/role at the target OrgUnit
- The Task and a read-only record projection MUST be replicated to the target OrgUnit
- Approvers act on the Task, not by editing the record

##### B) Decision Event (TASK_DECISION)
A decision is represented as an outbound event from the approver OrgUnit to the owning OrgUnit.

Minimum decision payload:
- `taskId`, `entityId`, `recordId`
- `decision` (APPROVE | REJECT | REQUEST_CHANGES)
- `comment` (optional but recommended)
- `decidedByUserId`, `decidedAt`
- `recordVersionSeen` (the version the approver reviewed)

Rules:
- Decisions MUST be auditable and immutable
- The owning node MUST validate that the decision matches the current workflow step
- If `recordVersionSeen` is stale (record has changed since approval view), the decision MUST NOT be applied automatically; it MUST be marked as STALE and a re-review task SHOULD be generated
- REQUEST_CHANGES MUST transition the record to a Facility-editable state (e.g., ReworkRequested) and include the approver comment in the audit stream

---

### 4.13 Telemetry & Observability (Core)

The platform MUST provide built-in telemetry to support on-prem and cloud operations, support diagnostics, and usage insights.

Principles:
- Telemetry MUST be non-blocking and asynchronous (never fail business operations)
- Telemetry MUST be configurable and transparent (admins can view what is collected)
- Telemetry MUST be privacy-preserving by default (no business field values, no attachment content)
- Telemetry MUST be topology-aware (HQ/Region/Facility) and tolerate offline periods

#### 4.13.1 Telemetry Categories (OSS)

The platform MUST capture structured events in these categories:
- SYSTEM (startup/shutdown, health)
- API (request counts, latency buckets, error rates; no payload logging by default)
- WORKFLOW (transition failures, time-in-state metrics)
- RULES/MEASURES (evaluation errors, slow evaluations)
- REPLICATION (outbox backlog, last sync time, apply failures)
- SCHEDULER (missed runs, job failures)
- SECURITY (failed logins, permission denials)

#### 4.13.2 Telemetry Event Schema (Minimum)

Telemetry MUST be stored as structured events (append-only):

- `telemetryEventId` (ULID/UUIDv7)
- `tenantId`, `orgUnitId`, `facilityId` (optional)
- `timestamp`
- `category`, `severity` (INFO | WARN | ERROR)
- `component` (e.g., WorkflowEngine, Replicator, Scheduler)
- `operation` (e.g., Transition, ApplyEvent, RunSchedule)
- `correlationId` (propagated across API -> workflow -> rules -> replication/scheduler)
- `summary`
- `attributes` (key/value, size-limited, no business field values)

Rules:
- Telemetry events MUST NOT include business record field values by default
- Telemetry events MUST NOT include attachment content
- Telemetry events SHOULD include stable identifiers (entityId/recordId) only when necessary for diagnostics

#### 4.13.3 Local Telemetry Store & Retention (OSS)

- Telemetry MUST be stored locally on each node (PostgreSQL table or equivalent)
- Retention MUST be configurable (e.g., 7/30/90 days)
- Storage MUST be bounded (size limits, sampling/throttling when saturated)
- Admins MUST be able to view and export telemetry locally

#### 4.13.4 Support Bundle Export (OSS)

The platform SHOULD support exporting a diagnostics bundle for support:
- Selected telemetry range
- Replication status summary (queue depth, last sync)
- Application version/build info and node identity
- Optional logs (configurable), sanitized by default

Bundles MUST be exportable as files for air-gapped environments.

#### 4.13.5 Optional Upstream Telemetry Forwarding (OSS-safe)

The platform MAY support forwarding telemetry upstream (Facility -> Region/HQ) using an Outbox-style mechanism.

Rules:
- Forwarding MUST be opt-in and explicitly configured
- Forwarding MUST use outbound-only connectivity (no inbound requirement on facilities)
- Forwarding MUST apply filtering/sampling controls

Paid/Enterprise scope:
- Centralized fleet dashboards, anomaly detection, alerting, auto-triage

---

### 4.14 Licensing & Entitlements (Core)

The platform MUST support a modern licensing and entitlement model to enable trials, subscription enforcement, and feature gating across on-prem and cloud deployments.

Principles:
- Licensing MUST be node- and topology-aware (HQ/Region/Facility)
- Licensing MUST tolerate offline operation via signed licenses and grace periods
- Licensing MUST be enforceable via entitlements (feature flags + limits), not hardcoded checks
- Licensing MUST be auditable and observable (telemetry events on validation failures, expiry, grace)

#### 4.14.1 License Roles & Validation Node

- License validation is performed by the **Primary Node** (typically HQ for a tenant).
- Downstream nodes (Region/Facility) MUST consume the validated entitlement state replicated from the Primary Node.
- Facilities MUST NOT require inbound connectivity for licensing.

Definitions:
- **Primary Node**: OrgUnit responsible for license validation and entitlement distribution.
- **Entitlements**: Signed claims defining allowed features and limits.

#### 4.14.2 Trial / Bootstrap Behavior

- The platform MUST be installable without a license.
- On first run, the platform MUST operate in **Trial Mode** for a configurable period (default 15 days).
- During Trial Mode, the system SHOULD allow a reasonable default set of features and limits (configurable by distribution).
- Prior to trial expiry, the system MUST warn admins (UI + telemetry).

#### 4.14.3 Enrollment & License Application (Client-side)

The Primary Node MUST support enrolling with a License Server (external) by submitting:
- company name
- admin name
- email (required; must be verified by the license server)
- phone (optional)
- node/tenant identifiers

The license server implementation is out of scope for this spec; the platform must provide the client-side integration points.

#### 4.14.4 License Format & Verification (OSS-safe)

- Licenses MUST be verifiable offline using cryptographic signatures (e.g., signed JWT/JWS).
- The platform MUST embed the license server public key(s) (or allow admin import of public keys).
- The platform MUST cache the current license and entitlement set locally.

Minimum license claims:
- `licenseId`
- `tenantId`
- `issuedAt`, `expiresAt`
- `plan` (COMMUNITY | TRIAL | PRO | ENTERPRISE)
- `entitlements` (feature flags + limits)
- `primaryOrgUnitId`

#### 4.14.5 Entitlements (Feature Flags + Limits)

Entitlements MUST support:
- Feature toggles (enable/disable modules such as advanced auth, multi-site dashboards, etc.)
- Limit counters (e.g., maxFacilities, maxUsers, maxOrgUnits, maxRecordsPerEntity optional)

Required limit for future enforcement:
- `maxFacilities` (maximum number of FACILITY OrgUnits that may be created under the tenant)

Enforcement points (minimum):
- Org topology creation (creating a new Facility MUST check `maxFacilities`)
- Admin-only actions that enable paid features MUST be gated by entitlements

#### 4.14.6 Validation Frequency, Grace & Failure Behavior

- The Primary Node MUST periodically re-validate license status (configurable interval; e.g., daily/weekly).
- The platform MUST support a grace period when the license cannot be revalidated due to connectivity (configurable).
- On license expiry or grace exhaustion, the system MUST enter a restricted mode:
  - read-only access to existing data remains available (subject to authorization)
  - creation of new Facilities and other gated actions MUST be blocked
  - critical operational safety: existing records remain viewable/exportable

#### 4.14.7 Entitlement Distribution to Downstream Nodes

- The Primary Node MUST publish the current entitlement state to downstream nodes using the replication mechanism (e.g., as a GLOBAL record or event).
- Downstream nodes MUST enforce limits using the last known entitlement state.
- Entitlement changes MUST be auditable and replicated promptly.

#### 4.14.8 Telemetry Requirements

The platform MUST emit telemetry events for:
- trial started / trial expiring / trial expired
- license applied / license renewed / license expired
- revalidation failures and grace-period countdown
- blocked actions due to entitlement limits (e.g., maxFacilities exceeded)

#### 4.14.9 Node Identity & Registration (Bootstrap)

To support on-prem deployments behind firewalls, nodes MUST be able to register with the Primary Node using outbound-only connectivity.

Bootstrap flow:
- A newly installed node starts in **Unregistered Mode** with no tenant/org identity.
- An admin provides:
  - Primary Node URL (HQ/Region)
  - a one-time `registrationToken` issued by the Primary Node
- The node performs an outbound registration request and receives a **credential bundle**:
  - `tenantId`
  - `orgUnitId` and `orgUnitType` (FACILITY or REGION)
  - `facilityId` (if applicable)
  - replication upstream URL(s)
  - replication credentials (e.g., client certificate or signed token)
  - current entitlement snapshot (optional)

Rules:
- Registration MUST be auditable and emit telemetry events (registered, failed registration attempts)
- Registration credentials MUST be revocable by admins on the Primary Node
- If registration is not completed, the node MUST operate only in a limited local-admin mode and MUST NOT replicate data upstream
- Registration MUST NOT require inbound connectivity to the node

### 4.15 Document Control & Managed Content (Core)

The platform MUST provide a foundational document control capability to support process documentation, system documentation, and compliance use cases.

Document Control is distinct from Attachments:
- Attachments are unversioned evidence linked to records
- Controlled Documents are versioned, lifecycle-managed content governed by workflow

Controlled Documents MUST be implemented using the same metadata-driven principles as other entities.

#### 4.15.1 Controlled Document Entity (OSS)

The platform MUST provide a system entity for managed documents.

Minimum `ControlledDocument` attributes:
- `documentId` (stable, human-readable identifier)
- `title`
- `description`
- `documentType` (POLICY | SOP | MANUAL | WORK_INSTRUCTION | RECORD)
- `category` / `tags`
- `ownerOrgUnitId` (HQ / Region / Facility)
- `scope` (GLOBAL | LOCAL)
- `status` (DRAFT | IN_REVIEW | APPROVED | PUBLISHED | RETIRED)
- `currentDraftVersion`
- `currentPublishedVersion`
- `reviewInterval` (optional)
- `nextReviewDate` (optional)

Rules:
- Ownership determines who may edit and approve
- `GLOBAL` documents are owned by HQ; `LOCAL` documents by Facility
- Status transitions MUST be enforced via workflow

#### 4.15.2 Document Lifecycle & Workflow (OSS)

The OSS core MUST support a basic document lifecycle using workflow:
- Draft → Review → Approved → Published → Retired

Capabilities:
- Only Draft documents are editable
- Review and Approval are read-only with comment capability
- Published documents are immutable
- Retired documents remain viewable but are clearly marked inactive

Workflow transitions MUST be:
- Auditable
- Authorization-controlled
- Version-aware

#### 4.15.3 Document Versioning (OSS)

Controlled Documents MUST support versioning suitable for audits.

Rules:
- Draft edits increment a working version (e.g., 1.1-draft)
- Publishing creates an immutable `DocumentRevision`
- Published revisions MUST NOT be modified or deleted
- Multiple published revisions MAY exist; exactly one is active at a time
- Historical revisions MUST remain accessible (subject to authorization)

Minimum `DocumentRevision` attributes:
- `revisionId`
- `documentId`
- `revisionNumber` (e.g., 1.0, 2.0)
- `content` (HTML or Markdown)
- `publishedAt`, `publishedBy`
- `checksum` (optional, for integrity verification)

#### 4.15.4 Authoring & Editing (OSS)

OSS MUST provide a basic authoring experience:
- Simple WYSIWYG or Markdown editor
- Copy/paste support from common tools (e.g., Word)
- Sanitized content storage and rendering

OSS explicitly excludes:
- Real-time collaboration
- Track changes / redlining
- Inline threaded comments with resolution

#### 4.15.5 Linking Documents to Work (OSS Foundation)

The platform MUST support linking controlled documents to operational context.

Capabilities:
- Link a document to an Entity type or specific record
- Link a document to a Task (e.g., SOP attached to an approval task)
- Display linked documents as read-only references

Rules:
- Linking MUST NOT duplicate content
- Links MUST respect document authorization
- Links MAY reference the active published revision only

#### 4.15.6 Search & Discovery (OSS)

OSS MUST support basic discovery:
- Search by title, documentId, tags, category, status
- Filter by ownerOrgUnit and scope
- Optional full-text search in content if indexing is available

#### 4.15.7 Audit & Compliance (OSS)

The platform MUST audit:
- Document creation
- Content edits (draft)
- Workflow transitions
- Publication and retirement
- Link creation/removal

Audit events MUST:
- Be immutable
- Include actor, timestamp, version, and action
- Be queryable by admins

#### 4.15.8 Scheduler Integration (OSS)

Controlled Documents MAY integrate with the scheduler:
- Automatic review reminders based on `reviewInterval`
- Task creation for document review/approval

Scheduler-triggered actions MUST be auditable.

#### 4.15.9 Explicitly Out of Scope (Paid Features)

The following document control features are excluded from OSS and reserved for paid editions:
- Real-time collaborative editing
- Inline comments, redlining, and tracked changes
- Multi-reviewer quorum and electronic signatures
- Controlled distribution and read-acknowledgement tracking
- Contextual section-level links (deep anchors)
- Facility deviations, waivers, and local overrides of GLOBAL documents
- Advanced audit reports and compliance dashboards
- External document repository integrations (SharePoint, S3, etc.)

## 5. Schema & Versioning Model

### 5.1 Schema Version
- Every entity has a `schemaVersion`
- Schema changes create a new version
- Existing records retain original version reference

### 5.2 Screen Version
- Controls UI layout and presentation
- Independent of schema version
- Allows UX changes without data migration

---

## 6. Workflow Engine (OSS Core)

### 6.1 Workflow Definition

A workflow consists of:
- States
- Transitions
- Transition actions

#### Example States
- Draft
- Submitted
- Approved
- Rejected
- Closed
- Archived

#### Transition Metadata
- `fromState`
- `toState`
- `actionLabel`
- `conditions` (simple rules only in OSS)
- `allowedRoles`

Workflows MAY include cross-OrgUnit approval steps. In such cases, upstream approvers act via Tasks and emit decisions; the owning OrgUnit applies the resulting transition after validating the workflow step and record version.

---

### 6.2 Workflow Capabilities (OSS Scope)

Included:
- State transitions
- State-based permissions
- Audit trail of transitions

Excluded (Paid):
- Visual designer
- SLAs
- Escalations
- Notifications

---

### 6.3 Workflow-Driven Rules

Workflows may enforce validations and rules during transitions.

Supported capabilities in OSS:
- Pre-transition validation hooks
- Rule evaluation based on entity state
- Blocking transitions on rule failure
- Returning structured validation errors

Rules may:
- Read current entity data
- Read referenced entity data
- Perform read-only aggregate checks
- Invoke measures with parameters

Rules must NOT:
- Mutate data
- Perform external calls
- Trigger side effects (OSS scope)

---

### 6.4 Task & Assignment Model (Core)

The platform MUST support a basic task/assignment model to represent ownership and approvals.

Concepts:
- A Task is linked to an entity record and may be created manually or by workflow transitions
- Tasks have an assignee (user or role), status, due date, and optional priority
- Workflow transitions may generate tasks for approvers/owners
- Tasks may accept decisions (approve/reject/request-changes) which are emitted as TASK_DECISION events when the assignee is in a different OrgUnit than the record owner

Minimum Task fields:
- `taskId`, `entityId`, `recordId`
- `title`, `description`
- `assigneeType` (USER/ROLE), `assigneeId`
- `status` (OPEN/IN_PROGRESS/DONE/CANCELLED)
- `dueAt`, `createdAt`, `createdBy`
- `workflowState` (optional snapshot)
- `ownerOrgUnitId` (OrgUnit that owns the parent record), `assigneeOrgUnitId` (OrgUnit of assignee)

Rules:
- Task visibility MUST respect authorization to the parent record
- Tasks MUST be auditable (create/update/complete)
- When a Task is completed with a decision by a non-owning OrgUnit, the decision MUST be replicated to the owning OrgUnit as a TASK_DECISION event

---

## 7. Authorization Model (RBAC)

### 7.1 Roles
- Admin
- Designer
- User
- Approver
- Custom roles

### 7.2 Permission Scope
- Entity-level permissions (CRUD)
- Property-level permissions (Read / Write)
- Workflow transition permissions

Authorization must be evaluated:
- At API level
- At UI generation level

---

## 8. Data Storage Model

### 8.1 Schemaless Storage
- Entity records stored as key-value documents
- Schema validation occurs via metadata

### 8.2 Denormalization
- User selects properties to denormalize
- Denormalized fields feed search & reporting
- Must be explicitly declared

### 8.3 Rule Evaluation Context

All validations and rules are executed within a controlled context.

Context includes:
- Current entity instance
- Referenced entities (read-only)
- User role & identity
- Workflow state
- Schema version
- Measure definitions and versions

Context excludes:
- Network access
- File system access
- External services

---

## 9. Search & Indexing (OSS)

### 9.1 Entity Search
- Filterable lists per entity
- Filters based on denormalized fields
- Save search definitions

### 9.2 Global Search
- Cross-entity
- Role-aware
- Indexed fields only

---

## 10. Auto-Generated UX

### 10.1 Generated Screens
- Entity list view
- Entity detail view
- Create/Edit form
- Workflow action bar

### 10.2 UI Behavior
- Field visibility controlled by authorization
- Read-only fields enforced
- Workflow state drives available actions

UI customization is **minimal** in OSS.

### 10.4 Mobile Support (Foundation)

Mobile workflows are required for modern business operations. The OSS core MUST provide a foundation that enables mobile usage and future native mobile applications.

#### 10.4.1 OSS Baseline (Responsive Web)

OSS MUST support:
- Responsive layouts for generated screens (lists, forms, tasks, documents)
- Touch-friendly controls for common actions
- Mobile-safe authentication sessions/tokens (no reliance on desktop-only flows)
- File capture/upload support where feasible (attachments via browser)

OSS MAY support:
- Progressive Web App (PWA) packaging for installability (optional)

#### 10.4.2 Mobile API Readiness (Core)

To enable paid native mobile applications, the platform MUST:
- Expose stable APIs for Tasks, Workflows, Documents, Search, and Attachments
- Support deep links to records, tasks, and documents
- Preserve correlationId across mobile requests for telemetry and support bundles

#### 10.4.3 Paid / Enterprise Mobile Capabilities (Out of OSS)

The following mobile capabilities are reserved for paid editions:
- Native iOS/Android applications
- Offline-first mobile data sync (selective caching of tasks/records/documents)
- Background sync and conflict handling for mobile edits
- Push notifications (APNS/FCM) for tasks, approvals, and reminders
- Device management controls (device registration, revoke, policy enforcement)

---

## 11. Dashboard (OSS)

### Features
- User landing page
- Pin saved searches
- Pin entity lists
- Auto-layout

Excluded (Paid):
- Charts
- Cross-entity analytics
- Custom widgets

---

### 11.1 Scheduler & Calendar (OSS)

The platform MUST provide an OSS-level scheduler and lightweight calendar to support recurring compliance and operational activities.

Use cases:
- Create an audit task every 6 months
- Remind weekly tank level entry for fuel consumption
- Schedule periodic inspections and auto-create records/tasks

Required capabilities:
- Define schedules (one-time and recurring) using a rule such as iCal RRULE or equivalent metadata
- Scheduler triggers can:
  - Create a task linked to an entity record
  - Create a new entity record based on a template
  - Update a field (restricted: safe, deterministic updates only)

Schedule definition (example):
```yaml
scheduleId: "fuel.tank.level.weekly"
name: "Weekly Tank Level Entry"
rrule: "FREQ=WEEKLY;BYDAY=MO;BYHOUR=9;BYMINUTE=0"
timezone: "UTC"
action:
  type: "createTask"
  task:
    title: "Update tank level"
    entityId: "TankLog"
    assigneeType: "ROLE"
    assigneeId: "Operations"
    dueOffset: "P1D"
```

Rules:
- Scheduler actions MUST be auditable
- Scheduler MUST be tenant-safe (if multi-tenant is later enabled)
- Scheduler MUST NOT call external systems in OSS

---

## 12. Admin Capabilities (OSS)

- User management
- Role management
- Entity definition
- Workflow definition (basic grid)
- Measure definition (metadata CRUD)
- System configuration
- Org topology management (create HQ/Region/Facility nodes)
- Replication configuration (set upstream URL, credentials, and node identity)
- Licensing configuration (set Primary Node, apply license, view entitlement status)
- Trial/licensing status warnings and audit view
- Controlled document management (create, publish, retire, link)

---

## 13. Authentication (OSS)

- Username/password authentication
- Local identity store
- Session or token-based auth

Excluded (Paid):
- OAuth2
- SAML
- LDAP
- MFA

---

### 13.1 Account Lifecycle (OSS)

Required capabilities:
- Password reset (token-based)
- Admin disable/enable user
- Basic rate limiting / lockout for repeated failed logins (configurable)

---

## 14. Non-Functional Requirements

### Scalability
- Stateless application layer
- Horizontal scaling support

### Security
- Role-based access enforcement
- Audit logs for workflow transitions
- Rule execution is sandboxed

### Extensibility
- Event hooks (pre/post create, update, transition)
- Plugin points (OSS-safe)

### 14.1 Determinism & Safety

The rule engine must be:
- Deterministic
- Side-effect free
- Sandbox-executed
- Version-aware

Rule execution must be auditable and traceable.

---

## 15. Explicitly Out of Scope (Paid Features)

The following **must not** be implemented in OSS:
- Visual workflow designer
- Advanced dashboards & charts
- Multi-site / HQ consolidation
- Enterprise authentication
- AI assistant
- SLA & escalation logic
- External system rule execution
- User-written executable functions or scripts
- Scheduled imports/ETL pipelines
- External notification channels (SMS/Slack/Email gateways) beyond basic in-app
- Replication monitoring dashboards, auto-heal, and conflict resolution UI
- License server implementation and payment/billing workflows
- Native mobile apps, offline mobile sync, push notifications, and device management

---

## 16. Reference Implementation (Required)

The OSS core must be capable of implementing at least:
- A Procurement Request / Purchase Order entity
- Approval workflow
- Role-based access
- Searchable list
- Dashboard pinning
- A budget availability rule using a measure
- Attachments on Purchase Order (e.g., vendor quotation PDF)
- Scheduled recurring task (e.g., 6-month audit reminder)

This reference must be built **using the platform itself**.

---

## 17. Contribution Guidelines (High-Level)

- No domain-specific logic
- No hardcoded business rules
- Metadata-first implementations
- Backward compatibility is mandatory

---

## 18. Success Criteria (OSS)

The OSS core is successful if:
- A complete business app can be built with zero custom code
- Schema changes do not break existing data
- Workflow governs all state transitions
- Authorization is enforceable at property level
- Business validations can be enforced declaratively without custom code
- Reusable business calculations can be defined once and enforced consistently via measures

---
