# Agent Quick Reference Card
## CBAP OSS - Common Tasks & Patterns

**Quick lookup guide for AI agents working on CBAP OSS**

---

## 🚨 Critical Rules (Never Violate)

1. **Metadata over Code** - Execute metadata, don't embed business rules
2. **Determinism** - All logic must be deterministic and side-effect free
3. **Execution Order** - Always: Auth → Validation → Workflow → Persist → Audit
4. **No Business Logic** - Never hardcode domain-specific logic
5. **Schema Versioning** - Always version schema changes

---

## 📁 Project Structure

```
backend/
├── cbap-core/       # Core runtime (no dependencies on other modules)
├── cbap-api/        # REST API
├── cbap-persistence/# Database
├── cbap-search/     # OpenSearch
├── cbap-security/   # Auth
├── cbap-bootstrap/  # Startup
└── cbap-app/        # Launcher

frontend/
├── app-shell/       # Navigation
├── metadata-ui/     # Auto-generated forms/lists
├── workflow-ui/     # Tasks/approvals
├── document-ui/     # Documents
├── dashboard-ui/    # Dashboards
└── shared/          # Utilities
```

---

## 🔧 Common Tasks

### Add a New Entity Type

1. **Backend**: Add to `cbap-core/metadata/`
   - Create `EntityDefinition` model
   - Add metadata service methods
   - Add validation rules support

2. **Frontend**: Add to `metadata-ui/`
   - Use metadata to render forms/lists
   - No hardcoded components

### Add a Validation Rule

1. Add rule definition to metadata
2. Implement in `cbap-core/rules/`
3. Must be deterministic and side-effect free
4. Test with various inputs

### Add a Workflow Transition

1. Define in metadata (not code)
2. Implement in `cbap-core/workflow/`
3. Follow execution order
4. Add authorization checks
5. Audit the transition

### Add an API Endpoint

1. Add controller in `cbap-api/`
2. Use service from `cbap-core/`
3. Follow REST conventions
4. Include correlation ID
5. Add proper error handling

---

## 💻 Code Patterns

### Backend Service Pattern

```java
@Service
public class MyService {
    // 1. Authorization
    authService.checkPermission(user, resource, Permission.ACTION);
    
    // 2. Validation
    validationService.validate(definition, data);
    
    // 3. Business logic (metadata-driven)
    result = process(metadata, data);
    
    // 4. Persistence
    result = repository.save(result);
    
    // 5. Audit
    auditService.record(result, user);
    
    return result;
}
```

### Frontend Component Pattern

```typescript
export function MyComponent({ definition }: Props) {
  const { data } = useQuery({
    queryKey: ['key'],
    queryFn: () => service.fetch(definition),
  });
  
  return renderFromMetadata(definition, data);
}
```

---

## 🧪 Testing Checklist

- [ ] Unit tests for services
- [ ] Integration tests for APIs
- [ ] Test authorization
- [ ] Test validation
- [ ] Test deterministic behavior
- [ ] Test error cases

---

## 🚫 What NOT to Do

❌ Hardcode business logic  
❌ Skip authorization  
❌ Bypass validation  
❌ Make non-deterministic rules  
❌ Add side effects to rules  
❌ Create customer-specific features  
❌ Break backward compatibility  

---

## 📝 Documentation Checklist

- [ ] Code comments explain "why"
- [ ] Update SPEC.md if adding features
- [ ] Update relevant guides
- [ ] Add examples if needed

---

## 🔍 Finding Code

**By Feature**:
- Entity → `cbap-core/metadata/`
- Workflow → `cbap-core/workflow/`
- API → `cbap-api/`
- UI → `frontend/src/{module}/`

**By Layer**:
- Controllers → `.../controller/`
- Services → `.../service/`
- Repositories → `.../repository/`

---

## 🎯 Reference App Test

Feature must support:
- Purchase Order
- Approval workflow
- Budget check (Measure)
- Scheduled task
- Controlled document

---

## 📚 Key Documents

1. `OSS_MENTAL_MODEL.md` - Core philosophy
2. `SPEC.md` - Requirements
3. `TECH_STACK_AND_PROJECT_STRUCTURE_GUIDE.md` - Stack & structure
4. `AGENT_GUIDE.md` - Full guide
5. `COLOR_GUIDE.md` - UI colors

---

## ⚡ Quick Commands

```bash
# Backend
mvn clean install
cd cbap-app
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Frontend
npm install
npm run dev
npm run build
```

---

**Remember**: Metadata defines behavior. Code executes metadata.
