# Open Source Core Specification  
## Composable Business Application Platform (CBAP)

Version: 0.1  
Status: Draft (Engineering Baseline)

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

---

## 3. High-Level Architecture

```text
+------------------------------+
| Presentation Layer           |
| (Auto-generated UI + APIs)   |
+--------------▲---------------+
               |
+--------------┴---------------+
| Application Engine           |
| - Entity Runtime             |
| - Workflow Engine            |
| - Authorization Engine       |
| - Validation/Rule Engine     |
+--------------▲---------------+
               |
+--------------┴---------------+
| Metadata Store               |
| - Entity Definitions         |
| - Workflow Definitions       |
| - Measures (Declarative Fn)  |
| - Schema & Screen Versions   |
+--------------▲---------------+
               |
+--------------┴---------------+
| Data Store & Search Index    |
| - Schemaless Storage         |
| - Denormalized Index         |
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

## 12. Admin Capabilities (OSS)

- User management
- Role management
- Entity definition
- Workflow definition (basic grid)
- Measure definition (metadata CRUD)
- System configuration

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

---

## 16. Reference Implementation (Required)

The OSS core must be capable of implementing at least:
- A Procurement Request / Purchase Order entity
- Approval workflow
- Role-based access
- Searchable list
- Dashboard pinning
- A budget availability rule using a measure

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
