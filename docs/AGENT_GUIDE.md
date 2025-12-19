# Agent Guide: Working on CBAP OSS
## A Guide for AI Agents and Contributors

**Version**: 1.0  
**Last Updated**: December 2025

This guide helps AI agents and contributors understand how to work effectively on the CBAP OSS codebase. Read this before making any changes.

---

## 🎯 Quick Start

1. **Read First**: `OSS_MENTAL_MODEL.md` - Understand what CBAP is
2. **Architecture**: `TECH_STACK_AND_PROJECT_STRUCTURE_GUIDE.md` - Know the stack
3. **Specification**: `SPEC.md` - Understand requirements
4. **This Guide**: How to work on the code

---

## 📚 Essential Documentation

Before making changes, familiarize yourself with:

| Document | Purpose | When to Read |
|----------|---------|--------------|
| `OSS_MENTAL_MODEL.md` | Core philosophy and principles | **Always first** |
| `SPEC.md` | Complete platform specification | Before implementing features |
| `TECH_STACK_AND_PROJECT_STRUCTURE_GUIDE.md` | Tech stack and project layout | Before coding |
| `EXPRESSION_LANGUAGE_V0.md` | Expression language for rules/measures | When working on rules engine |
| `COLOR_GUIDE.md` | UI color system | When working on frontend styling |
| `AGENT_GUIDE.md` | This document | You're reading it! |

---

## 🏗️ Project Structure

### Backend (Java/Spring Boot)

```
backend/
├── cbap-core/          # Core runtime - metadata, rules, workflow, auth
├── cbap-api/           # REST API layer
├── cbap-persistence/    # Database access (PostgreSQL, Flyway)
├── cbap-search/        # OpenSearch integration
├── cbap-security/      # Authentication/authorization
├── cbap-bootstrap/     # Startup and initialization
└── cbap-app/           # Spring Boot launcher
```

**Key Points**:
- Each module is a Maven submodule
- Dependencies flow: `cbap-app` → other modules → `cbap-core`
- `cbap-core` has no dependencies on other CBAP modules
- Business entities are stored as JSONB, not JPA entities

### Frontend (React/TypeScript)

```
frontend/
├── src/
│   ├── app-shell/      # Application shell and navigation
│   ├── metadata-ui/    # Auto-generated forms and lists
│   ├── workflow-ui/    # Tasks and approvals
│   ├── document-ui/    # Controlled documents
│   ├── dashboard-ui/   # Dashboards and widgets
│   └── shared/         # Common utilities, theme, API client
```

**Key Points**:
- UI is **metadata-driven** - no hardcoded business screens
- Use TypeScript strict mode
- Follow MUI 6 patterns
- Use CSS variables from `COLOR_GUIDE.md`

---

## 🧠 Core Principles (Non-Negotiable)

### 1. Metadata Over Code

**✅ DO**:
- Execute metadata, don't embed business rules
- Make behavior configurable via metadata
- Store business logic as declarative definitions

**❌ DON'T**:
- Hardcode business workflows
- Embed domain-specific logic
- Create customer-specific features

**Example**:
```java
// ✅ GOOD: Execute workflow from metadata
WorkflowEngine.executeTransition(entity, transitionId, metadata);

// ❌ BAD: Hardcode business logic
if (entity.getType().equals("PurchaseOrder") && amount > 10000) {
    // Approval logic here
}
```

### 2. Determinism & Safety

**✅ DO**:
- Make all logic deterministic
- Keep rules side-effect free
- Make operations replayable
- Version-aware execution

**❌ DON'T**:
- Call external systems from rules
- Perform I/O in rule evaluation
- Mutate data during rule execution
- Use non-deterministic functions (random, current time without context)

**Example**:
```java
// ✅ GOOD: Deterministic measure evaluation
public BigDecimal evaluateMeasure(MeasureDefinition measure, Map<String, Object> params) {
    // Read-only, deterministic calculation
    return calculate(measure.getExpression(), params);
}

// ❌ BAD: Side effects in rules
public boolean validateOrder(Order order) {
    sendEmail(order.getCustomer()); // NO! Side effect
    return order.getAmount() > 0;
}
```

### 3. Execution Order (Never Skip Steps)

Every operation MUST follow this order:

```
Authorization
 → Validation & Rules
 → Measure Evaluation (read-only)
 → Workflow Transition
 → Persistence (atomic)
 → Audit & Telemetry
 → Replication Events
```

**Never bypass any step!**

### 4. Schema Versioning

**✅ DO**:
- Version all schema changes
- Keep old data readable
- Support multiple schema versions simultaneously

**❌ DON'T**:
- Make destructive schema changes
- Break backward compatibility
- Delete fields without versioning

---

## 🎨 Coding Standards

### Backend (Java)

#### Package Structure
```
com.cbap.{module}.{concern}
```

Examples:
- `com.cbap.core.metadata.EntityService`
- `com.cbap.api.v1.EntityController`
- `com.cbap.persistence.repository.EntityRepository`

#### Naming Conventions
- **Services**: `*Service` (e.g., `EntityService`, `WorkflowService`)
- **Controllers**: `*Controller` (e.g., `EntityController`)
- **Repositories**: `*Repository` (e.g., `EntityRepository`)
- **Models/DTOs**: `*Model`, `*DTO` (e.g., `EntityModel`, `EntityDTO`)
- **Metadata**: `*Definition`, `*Metadata` (e.g., `EntityDefinition`)

#### Code Style
- Use Java 21 features (records, pattern matching, etc.)
- Prefer immutability
- Use Spring's dependency injection
- Follow Spring Boot conventions
- Write self-documenting code

#### Example Service
```java
@Service
@RequiredArgsConstructor
public class EntityService {
    private final EntityRepository repository;
    private final AuthorizationService authService;
    private final ValidationService validationService;
    
    @Transactional
    public EntityRecord create(EntityDefinition definition, Map<String, Object> data, UserContext user) {
        // 1. Authorization
        authService.checkPermission(user, definition, Permission.CREATE);
        
        // 2. Validation
        validationService.validate(definition, data);
        
        // 3. Create record
        EntityRecord record = EntityRecord.builder()
            .entityId(definition.getEntityId())
            .data(data)
            .schemaVersion(definition.getSchemaVersion())
            .build();
        
        // 4. Persist
        record = repository.save(record);
        
        // 5. Audit
        auditService.recordCreate(record, user);
        
        return record;
    }
}
```

### Frontend (TypeScript/React)

#### File Structure
```
{module}/
├── components/     # React components
├── hooks/          # Custom hooks
├── services/       # API services
├── types/          # TypeScript types
└── utils/          # Utility functions
```

#### Naming Conventions
- **Components**: PascalCase (e.g., `EntityList.tsx`)
- **Hooks**: camelCase with `use` prefix (e.g., `useEntityData.ts`)
- **Services**: camelCase (e.g., `entityService.ts`)
- **Types**: PascalCase (e.g., `Entity.ts`)

#### Code Style
- Use TypeScript strict mode
- Prefer functional components with hooks
- Use React Query for server state
- Use React Hook Form for forms
- Follow MUI component patterns

#### Example Component
```typescript
import { useQuery } from '@tanstack/react-query';
import { Box, Typography } from '@mui/material';
import { entityService } from '@shared/api/entityService';

interface EntityListProps {
  entityId: string;
}

export function EntityList({ entityId }: EntityListProps) {
  const { data, isLoading } = useQuery({
    queryKey: ['entities', entityId],
    queryFn: () => entityService.list(entityId),
  });

  if (isLoading) return <Typography>Loading...</Typography>;

  return (
    <Box>
      {/* Metadata-driven list rendering */}
    </Box>
  );
}
```

---

## 🔍 How to Navigate the Codebase

### Finding Code

1. **By Feature**:
   - Entity management → `cbap-core/metadata/`
   - Workflow → `cbap-core/workflow/`
   - API endpoints → `cbap-api/`
   - UI components → `frontend/src/{module}/`

2. **By Concern**:
   - Authorization → `cbap-core/auth/` or `cbap-security/`
   - Validation → `cbap-core/rules/`
   - Persistence → `cbap-persistence/`
   - Search → `cbap-search/`

3. **By Layer**:
   - Controllers → `cbap-api/.../controller/`
   - Services → `cbap-core/.../service/`
   - Repositories → `cbap-persistence/.../repository/`
   - Models → `cbap-core/.../model/`

### Understanding Dependencies

```
cbap-app (launcher)
  ├── cbap-api
  │     └── cbap-core
  ├── cbap-persistence
  │     └── cbap-core
  ├── cbap-search
  │     └── cbap-core
  ├── cbap-security
  │     └── cbap-core
  └── cbap-bootstrap
        └── cbap-core
```

**Rule**: `cbap-core` has no dependencies on other CBAP modules.

---

## ✅ Making Changes

### Before You Start

1. **Understand the Requirement**
   - Read relevant SPEC.md sections
   - Check if it's OSS scope (not paid feature)
   - Verify it follows metadata-driven principles

2. **Check Existing Code**
   - Search for similar functionality
   - Understand existing patterns
   - Check for related tests

3. **Plan Your Approach**
   - Identify affected modules
   - Consider backward compatibility
   - Plan for schema versioning if needed

### Implementation Checklist

- [ ] Follows metadata-driven principles
- [ ] No hardcoded business logic
- [ ] Deterministic and side-effect free
- [ ] Respects execution order (auth → validation → workflow → persist → audit)
- [ ] Includes proper error handling
- [ ] Has unit tests
- [ ] Updates documentation if needed
- [ ] Backward compatible (or properly versioned)

### Testing Requirements

**Backend**:
- Unit tests for all services
- Integration tests for API endpoints
- Test deterministic behavior
- Test authorization checks
- Test validation rules

**Frontend**:
- Component tests for UI components
- Integration tests for user flows
- Test metadata-driven rendering
- Test theme switching

**Example Test**:
```java
@Test
void testEntityCreationWithAuthorization() {
    // Given
    UserContext user = createUserWithPermission("CREATE");
    EntityDefinition definition = createEntityDefinition();
    Map<String, Object> data = createValidData();
    
    // When
    EntityRecord record = entityService.create(definition, data, user);
    
    // Then
    assertThat(record).isNotNull();
    assertThat(record.getEntityId()).isEqualTo(definition.getEntityId());
    verify(auditService).recordCreate(record, user);
}
```

---

## 🚫 What NOT to Do

### ❌ Don't Add Business Logic

```java
// ❌ BAD: Domain-specific logic
if (order.getDepartment().equals("Procurement") && order.getAmount() > 5000) {
    order.setRequiresApproval(true);
}

// ✅ GOOD: Metadata-driven
if (workflow.shouldRequireApproval(order, metadata)) {
    workflow.transition(order, "REQUIRE_APPROVAL");
}
```

### ❌ Don't Hardcode Workflows

```java
// ❌ BAD: Hardcoded workflow
if (status.equals("DRAFT")) {
    if (user.hasRole("APPROVER")) {
        status = "APPROVED";
    }
}

// ✅ GOOD: Metadata-driven workflow
workflowEngine.executeTransition(entity, "APPROVE", metadata, user);
```

### ❌ Don't Bypass Authorization

```java
// ❌ BAD: Skip authorization
public void deleteEntity(String id) {
    repository.deleteById(id);
}

// ✅ GOOD: Always check authorization
public void deleteEntity(String id, UserContext user) {
    authService.checkPermission(user, entity, Permission.DELETE);
    repository.deleteById(id);
    auditService.recordDelete(id, user);
}
```

### ❌ Don't Create Customer-Specific Features

```java
// ❌ BAD: Customer-specific
if (customerId.equals("ACME_CORP")) {
    // Special logic for ACME
}

// ✅ GOOD: Generic, configurable
if (entity.getMetadata().getCustomRule("specialHandling").evaluate()) {
    // Generic rule execution
}
```

### ❌ Don't Make Non-Deterministic Rules

```java
// ❌ BAD: Non-deterministic
public boolean shouldApprove() {
    return Math.random() > 0.5; // NO!
}

// ✅ GOOD: Deterministic
public boolean shouldApprove(Order order, MeasureResult budget) {
    return order.getAmount() <= budget.getAvailable();
}
```

---

## 📝 Documentation Standards

### Code Comments

**DO**:
- Explain "why", not "what"
- Document complex algorithms
- Explain metadata structures
- Note any non-obvious constraints

**DON'T**:
- Comment obvious code
- Duplicate what code says
- Leave TODO comments without context

### Example Comments

```java
/**
 * Evaluates a measure expression in a sandboxed context.
 * 
 * Measures MUST be:
 * - Deterministic (same inputs = same outputs)
 * - Side-effect free (no I/O, no mutations)
 * - Version-aware (uses measure version from metadata)
 * 
 * @param measure The measure definition from metadata
 * @param params Parameter values resolved from entity context
 * @return The calculated result
 * @throws RuleEvaluationException if evaluation fails or violates constraints
 */
public MeasureResult evaluateMeasure(MeasureDefinition measure, Map<String, Object> params) {
    // Implementation
}
```

### Updating Documentation

When adding features:
- Update `SPEC.md` if adding new capabilities
- Update relevant guides if patterns change
- Add examples if introducing new concepts
- Update this guide if processes change

---

## 🔧 Common Patterns

### Entity Service Pattern

```java
@Service
public class EntityService {
    // 1. Authorization
    // 2. Validation
    // 3. Business logic (metadata-driven)
    // 4. Persistence
    // 5. Audit
    // 6. Return result
}
```

### Metadata-Driven Validation

```java
public ValidationResult validate(EntityDefinition definition, Map<String, Object> data) {
    List<ValidationRule> rules = definition.getValidationRules();
    return rules.stream()
        .map(rule -> ruleEngine.evaluate(rule, data))
        .collect(ValidationResult.collector());
}
```

### Workflow Transition

```java
public WorkflowResult transition(EntityRecord record, String transitionId, UserContext user) {
    // 1. Check authorization for transition
    // 2. Validate transition conditions
    // 3. Evaluate pre-transition rules
    // 4. Execute transition
    // 5. Persist state change
    // 6. Audit transition
    // 7. Create tasks if needed
}
```

### Frontend Metadata Rendering

```typescript
function renderForm(definition: EntityDefinition) {
  return definition.properties.map(prop => 
    renderField(prop, definition.getFieldConfig(prop))
  );
}
```

---

## 🧪 Testing Patterns

### Backend Testing

```java
@SpringBootTest
class EntityServiceTest {
    @MockBean
    private AuthorizationService authService;
    
    @MockBean
    private ValidationService validationService;
    
    @Autowired
    private EntityService entityService;
    
    @Test
    void shouldCreateEntityWhenAuthorized() {
        // Test implementation
    }
    
    @Test
    void shouldRejectWhenNotAuthorized() {
        // Test implementation
    }
}
```

### Frontend Testing

```typescript
import { render, screen } from '@testing-library/react';
import { EntityList } from './EntityList';

describe('EntityList', () => {
  it('renders entities from metadata', () => {
    const definition = createEntityDefinition();
    render(<EntityList definition={definition} />);
    // Assertions
  });
});
```

---

## 🐛 Debugging Tips

### Backend

1. **Check Execution Order**: Verify auth → validation → workflow → persist → audit
2. **Check Metadata**: Ensure metadata is loaded correctly
3. **Check Authorization**: Verify user has required permissions
4. **Check Versioning**: Ensure schema versions match
5. **Check Logs**: Look for correlation IDs in logs

### Frontend

1. **Check Metadata**: Verify entity definitions are loaded
2. **Check API Calls**: Use browser DevTools Network tab
3. **Check Theme**: Verify CSS variables are applied
4. **Check React Query**: Verify cache and state
5. **Check Console**: Look for errors or warnings

---

## 📦 Module-Specific Guidelines

### cbap-core

- **Purpose**: Core runtime engine
- **Dependencies**: None on other CBAP modules
- **Key Concerns**: Metadata, rules, workflow, auth
- **Pattern**: Stateless services, metadata-driven

### cbap-api

- **Purpose**: REST API layer
- **Dependencies**: cbap-core
- **Key Concerns**: Controllers, DTOs, request/response handling
- **Pattern**: RESTful endpoints, versioned APIs

### cbap-persistence

- **Purpose**: Database access
- **Dependencies**: cbap-core
- **Key Concerns**: Repositories, migrations, JSONB storage
- **Pattern**: Repository pattern, Flyway migrations

### cbap-search

- **Purpose**: Search indexing
- **Dependencies**: cbap-core
- **Key Concerns**: OpenSearch client, indexing, queries
- **Pattern**: Index on denormalized fields

### Frontend Modules

- **app-shell**: Navigation, layout, routing
- **metadata-ui**: Auto-generated forms/lists
- **workflow-ui**: Tasks, approvals, workflow actions
- **document-ui**: Document viewing, versioning
- **dashboard-ui**: Dashboards, widgets
- **shared**: Utilities, theme, API client

---

## 🎯 Reference App Test

Before considering a feature complete, verify it supports the reference app:

- ✅ Purchase Order entity
- ✅ Approval workflow
- ✅ Budget check (via Measure)
- ✅ Scheduled audit task
- ✅ Controlled SOP/Policy document

If the reference app needs custom code, the platform is incomplete.

---

## 🔄 Version Control

### Commit Messages

Follow conventional commits:
```
feat(core): add entity validation engine
fix(api): correct authorization check in entity controller
docs(guide): update agent guide with new patterns
refactor(persistence): simplify repository interface
```

### Branch Strategy

- `main`: Production-ready code
- `develop`: Integration branch
- `feature/*`: Feature branches
- `fix/*`: Bug fix branches

---

## 🚀 Deployment Considerations

### Backend

- Database migrations via Flyway
- Configuration via environment variables
- Health checks via Actuator
- Logging via SLF4J/Logback

### Frontend

- Build with Vite
- Environment variables for API URL
- Theme switching support
- i18n support

---

## 📞 Getting Help

1. **Check Documentation**: Start with `OSS_MENTAL_MODEL.md` and `SPEC.md`
2. **Review Examples**: Look at reference app (when available)
3. **Check Tests**: Tests show expected behavior
4. **Ask Questions**: If something is unclear, ask before implementing

---

## ✅ Final Checklist

Before submitting code:

- [ ] Follows metadata-driven principles
- [ ] No hardcoded business logic
- [ ] Deterministic and side-effect free
- [ ] Respects execution order
- [ ] Includes tests
- [ ] Updates documentation
- [ ] Backward compatible
- [ ] Follows coding standards
- [ ] No security issues
- [ ] Works with reference app

---

## 🎓 Learning Path

1. **Start Here**: Read `OSS_MENTAL_MODEL.md`
2. **Understand Stack**: Read `TECH_STACK_AND_PROJECT_STRUCTURE_GUIDE.md`
3. **Know Requirements**: Read relevant `SPEC.md` sections
4. **Learn Patterns**: Study existing code
5. **Practice**: Work on small features first
6. **Contribute**: Follow this guide

---

## 📚 Quick Reference

### Key Files to Know

- `backend/pom.xml` - Maven parent POM
- `frontend/package.json` - Frontend dependencies
- `backend/cbap-app/src/main/resources/application.yml` - Base config
- `frontend/src/shared/theme.ts` - Theme configuration
- `frontend/src/shared/styles/global.css` - Global styles

### Key Commands

```bash
# Backend
cd backend && mvn clean install
cd cbap-app && mvn spring-boot:run

# Frontend
cd frontend && npm install
cd frontend && npm run dev
cd frontend && npm run build
```

### Key Principles

1. **Metadata over Code**
2. **Determinism & Safety**
3. **Execution Order** (never skip steps)
4. **Schema Versioning**
5. **No Business Logic in OSS**

---

**Remember**: CBAP is a metadata-driven runtime. Code executes metadata. Never embed business rules in code.

---

**Last Updated**: December 2025  
**Maintainer**: CBAP OSS Team
