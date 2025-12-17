# Expression Language v0 Specification  
**CBAP Expression Language (CEL-v0)**

---

## 1. Purpose

The CBAP Expression Language (CEL-v0) provides a **safe, deterministic, declarative expression syntax** used across the platform for:

- Validations
- Business rules
- Measures (declarative functions)
- Workflow guards
- UI visibility and enablement

CEL-v0 is **not a general-purpose programming language**.

---

## 2. Design Goals

CEL-v0 MUST be:

- Deterministic
- Side-effect free
- Sandboxed
- Serializable
- Versionable
- Safe for offline evaluation
- Human-readable and auditable

CEL-v0 MUST NOT support:
- Loops or recursion
- Mutation or assignment
- External I/O
- Network calls
- File access
- Time-dependent behavior outside controlled functions

---

## 3. Expression Model

An expression evaluates to one of:
- Boolean
- Number (integer or decimal)
- String
- Date
- Null

Expressions are **pure functions** of their inputs.

---

## 4. Syntax Overview

CEL-v0 uses a **JavaScript-like infix syntax** with strict limitations.

### 4.1 Literals

```
true, false
null
123
45.67
"string value"
2024-01-01
```

---

### 4.2 Property Access

Properties are accessed relative to the current record context.

```
requestedAmount
department
createdAt
```

Nested access:
```
supplier.creditLimit
```

---

### 4.3 Operators

#### Comparison
```
==  !=  <  <=  >  >=
```

#### Logical
```
&&  ||  !
```

#### Arithmetic
```
+  -  *  /
```

#### Grouping
```
( expression )
```

---

## 5. Built-in Functions (v0)

### 5.1 Null & Existence

```
isNull(x)
isNotNull(x)
```

---

### 5.2 Collections (Limited)

```
contains(collection, value)
size(collection)
```

Collections are read-only and may only originate from entity properties or measures.

---

### 5.3 Date Functions

```
today()
now()
daysBetween(date1, date2)
```

Notes:
- `now()` and `today()` are platform-controlled and injected by the runtime
- Values are consistent within a single evaluation

---

## 6. Measures & Function Calls

CEL-v0 supports calling **registered Measures**.

Example:
```
requestedAmount <= budget.available(department)
```

Rules:
- Measures are read-only
- Measures MUST declare dependencies and dimensions
- Measures may be cached
- Measures MUST be deterministic for identical inputs

---

## 7. Context Variables

The runtime injects context variables depending on usage.

### 7.1 Record Context

```
this
previous
```

---

### 7.2 Workflow Context

```
currentState
targetState
```

---

### 7.3 User Context

```
currentUser.id
currentUser.roles
currentUser.orgUnitId
```

---

### 7.4 System Context

```
tenantId
facilityId
```

Context availability depends on evaluation location.

---

## 8. Validation Semantics

Validation expressions MUST return a boolean.

Example:
```
requestedAmount > 0 && requestedAmount <= budget.available(department)
```

Rules:
- `false` → validation failure
- `null` → treated as failure
- Errors → treated as failure with diagnostic message

---

## 9. Error Handling

Expression evaluation may produce:
- SUCCESS
- FALSE
- ERROR

Errors MUST:
- Be non-fatal to the platform
- Include expression ID and message
- Be auditable and observable via telemetry

---

## 10. Versioning & Compatibility

Expressions MUST declare a language version:

```
{ "language": "CEL", "version": "v0" }
```

Rules:
- v0 expressions MUST remain backward compatible
- New operators or functions MUST be introduced in v1+

---

## 11. Security Constraints

The evaluator MUST enforce:
- Execution time limits
- Maximum expression depth
- Maximum function call count
- No reflection or dynamic resolution

---

## 12. Explicitly Out of Scope (v0)

- User-defined functions
- Loops or recursion
- Variable assignment
- Exception handling
- String interpolation
- Regex
- Aggregation logic (handled via Measures)

---

## 13. Example Use Cases

### Validation
```
requestedAmount > 0
```

### Workflow Guard
```
requestedAmount <= approvalLimit
```

### Measure-Based Rule
```
requestedAmount <= budget.available(department)
```

### UI Visibility
```
contains(currentUser.roles, "FINANCE_APPROVER")
```

---

## 14. Guiding Principle

> **Expressions describe truth.  
> Measures compute facts.  
> Workflows decide outcomes.**
