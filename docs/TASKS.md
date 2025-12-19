# Implementation Tasks
## CBAP OSS - UX-First Development Plan

**Version**: 1.0  
**Last Updated**: December 2025

This document organizes SPEC.md requirements into an ordered task list, prioritized to deliver visible UX progress incrementally. Each phase builds on the previous one, allowing you to see and interact with the application as it develops.

---

## 🎯 Development Philosophy

Tasks are organized to:
1. **Show progress early** - Visible UX from the start
2. **Build incrementally** - Each phase adds value
3. **Enable testing** - Can test features as they're built
4. **Follow user journey** - Login → Dashboard → Navigation → Features

---

## 📋 Task Status Legend

- ⬜ **Not Started** - Task not yet begun
- 🟡 **In Progress** - Currently being worked on
- ✅ **Complete** - Task finished and tested
- 🔄 **Blocked** - Waiting on dependencies

---

## Phase 1: Foundation & Authentication
*Goal: Users can log in and see a basic application*

### 1.1 Backend Foundation
- ✅ **Database Setup**
  - [x] Create Flyway migration for infrastructure tables (users, roles, permissions)
  - [x] Create base schema for metadata storage
  - [x] Set up PostgreSQL connection and test

- ✅ **Authentication Backend**
  - [x] User entity/model (username, password hash, roles, status)
  - [x] Role entity/model (roleId, name, permissions)
  - [x] Password hashing service (BCrypt)
  - [x] JWT token generation and validation
  - [x] Authentication service (login, logout, token refresh)
  - [x] Spring Security configuration (JWT-based)
  - [x] Basic rate limiting for login attempts
  - [x] API: `POST /api/v1/auth/login`
  - [x] API: `POST /api/v1/auth/logout`
  - [x] API: `GET /api/v1/auth/me` (current user)

- ✅ **User Management Backend**
  - [x] User repository and service
  - [x] Admin user creation endpoint
  - [x] Password reset token generation
  - [x] API: `POST /api/v1/users` (admin only)
  - [x] API: `GET /api/v1/users/{id}`
  - [x] API: `PUT /api/v1/users/{id}/password` (reset)

### 1.2 Frontend Foundation
- ✅ **Application Shell**
  - [x] Basic layout structure (header, main, footer)
  - [x] Theme provider setup (light/dark theme)
  - [x] CSS variables integration
  - [x] Responsive layout foundation

- ⬜ **Authentication UI**
  - [ ] Login page component
    - [ ] Username/password form
    - [ ] Error message display
    - [ ] Loading states
    - [ ] Remember me option
  - [ ] Login service/API integration
  - [ ] Token storage (localStorage)
  - [ ] Protected route wrapper
  - [ ] Redirect after login
  - [ ] Logout functionality

- ⬜ **Initial Landing Page**
  - [ ] Welcome message
  - [ ] Basic user info display
  - [ ] Placeholder for dashboard content

**Phase 1 Deliverable**: Users can log in and see a basic landing page

---

## Phase 2: Navigation & Dashboard
*Goal: Users can navigate and see a personalized dashboard*

### 2.1 Navigation System
- ⬜ **Backend: Navigation Metadata**
  - [ ] Navigation metadata model
  - [ ] Navigation service (load user's accessible navigation)
  - [ ] API: `GET /api/v1/navigation` (role-aware)

- ⬜ **Frontend: Navigation UI**
  - [ ] Navigation sidebar/drawer component
  - [ ] Navigation menu from metadata
  - [ ] Active route highlighting
  - [ ] Collapsible menu sections
  - [ ] Mobile-responsive navigation
  - [ ] User profile menu (logout, settings)

### 2.2 Dashboard Foundation
- ⬜ **Backend: Dashboard Metadata**
  - [ ] Dashboard metadata model
  - [ ] Dashboard service
  - [ ] API: `GET /api/v1/dashboard`
  - [ ] API: `POST /api/v1/dashboard/pins` (pin searches/lists)

- ⬜ **Frontend: Dashboard UI**
  - [ ] Dashboard page component
  - [ ] Dashboard layout (grid system)
  - [ ] Pinned searches widget
  - [ ] Pinned entity lists widget
  - [ ] Recent activity widget (placeholder)
  - [ ] Tasks summary widget (placeholder)
  - [ ] Auto-layout for widgets

**Phase 2 Deliverable**: Users can navigate and see a personalized dashboard

---

## Phase 3: Entity Management - Lists & Views
*Goal: Users can view entity lists and details*

### 3.1 Backend: Entity Metadata & Storage
- ⬜ **Entity Metadata**
  - [ ] Entity definition model (entityId, name, properties, workflow, etc.)
  - [ ] Property definition model (propertyId, type, label, validation, etc.)
  - [ ] Entity metadata repository
  - [ ] Entity metadata service (CRUD)
  - [ ] API: `GET /api/v1/metadata/entities`
  - [ ] API: `GET /api/v1/metadata/entities/{entityId}`
  - [ ] API: `POST /api/v1/metadata/entities` (admin)
  - [ ] API: `PUT /api/v1/metadata/entities/{entityId}` (admin)

- ⬜ **Entity Records Storage**
  - [ ] Entity record model (recordId, entityId, data JSONB, schemaVersion, etc.)
  - [ ] Entity record repository (JSONB storage)
  - [ ] Entity record service
  - [ ] Schema versioning support
  - [ ] API: `GET /api/v1/entities/{entityId}/records`
  - [ ] API: `GET /api/v1/entities/{entityId}/records/{recordId}`
  - [ ] Authorization checks on all endpoints

### 3.2 Frontend: Entity List View
- ⬜ **Metadata-Driven List Component**
  - [ ] Entity list page component
  - [ ] Load entity definition from metadata
  - [ ] Generate table columns from property definitions
  - [ ] Display records in table/grid
  - [ ] Pagination
  - [ ] Sorting by columns
  - [ ] Basic filtering (text search)
  - [ ] Row selection
  - [ ] Link to detail view

### 3.3 Frontend: Entity Detail View
- ⬜ **Metadata-Driven Detail Component**
  - [ ] Entity detail page component
  - [ ] Load entity definition
  - [ ] Generate form layout from properties
  - [ ] Display record data (read-only initially)
  - [ ] Field rendering by type (string, number, date, boolean, reference)
  - [ ] Reference field display (show referenced entity)
  - [ ] Calculated field display
  - [ ] Responsive layout

**Phase 3 Deliverable**: Users can view entity lists and detail records

---

## Phase 4: Entity Management - Create & Edit
*Goal: Users can create and edit entity records*

### 4.1 Backend: Entity CRUD
- ⬜ **Entity Record Operations**
  - [ ] Create entity record service
  - [ ] Update entity record service
  - [ ] Soft delete entity record service
  - [ ] Validation service integration
  - [ ] Authorization checks
  - [ ] Audit logging for create/update/delete
  - [ ] API: `POST /api/v1/entities/{entityId}/records`
  - [ ] API: `PUT /api/v1/entities/{entityId}/records/{recordId}`
  - [ ] API: `DELETE /api/v1/entities/{entityId}/records/{recordId}`

### 4.2 Frontend: Create/Edit Forms
- ⬜ **Metadata-Driven Form Component**
  - [ ] Entity form component (create/edit)
  - [ ] Generate form fields from property definitions
  - [ ] Field types: text, number, date, boolean, select, multi-select
  - [ ] Reference field (entity picker)
  - [ ] Required field validation (client-side)
  - [ ] Form validation display
  - [ ] Submit handler
  - [ ] Success/error feedback
  - [ ] Redirect after create

- ⬜ **Form Field Components**
  - [ ] Text input component
  - [ ] Number input component
  - [ ] Date picker component
  - [ ] Boolean (checkbox/switch) component
  - [ ] Single select dropdown
  - [ ] Multi-select component
  - [ ] Reference picker component
  - [ ] Calculated field display (read-only)

**Phase 4 Deliverable**: Users can create and edit entity records

---

## Phase 5: Search & Filtering
*Goal: Users can search and filter entities*

### 5.1 Backend: Search Infrastructure
- ⬜ **Search Indexing**
  - [ ] OpenSearch client setup
  - [ ] Index creation service
  - [ ] Document indexing on entity create/update
  - [ ] Denormalization service (extract indexed fields)
  - [ ] Search query service
  - [ ] API: `GET /api/v1/search?q={query}&entity={entityId}`

- ⬜ **Advanced Filtering**
  - [ ] Filter builder service
  - [ ] Filter by property values
  - [ ] Save search definitions
  - [ ] API: `POST /api/v1/entities/{entityId}/records/search`
  - [ ] API: `POST /api/v1/searches` (save search)
  - [ ] API: `GET /api/v1/searches` (user's saved searches)

### 5.2 Frontend: Search UI
- ⬜ **Global Search**
  - [ ] Global search bar component (header)
  - [ ] Search results page
  - [ ] Cross-entity search results
  - [ ] Result grouping by entity type
  - [ ] Link to entity detail from results

- ⬜ **Entity List Filtering**
  - [ ] Filter panel component
  - [ ] Filter by property values
  - [ ] Date range filters
  - [ ] Reference filters
  - [ ] Apply/clear filters
  - [ ] Save filter as search
  - [ ] Load saved searches

**Phase 5 Deliverable**: Users can search across entities and filter lists

---

## Phase 6: Workflow & Tasks
*Goal: Users can manage workflows and tasks*

### 6.1 Backend: Workflow Engine
- ⬜ **Workflow Metadata**
  - [ ] Workflow definition model (workflowId, states, transitions)
  - [ ] State definition model
  - [ ] Transition definition model (fromState, toState, conditions, actions)
  - [ ] Workflow metadata service
  - [ ] API: `GET /api/v1/metadata/workflows`
  - [ ] API: `GET /api/v1/metadata/workflows/{workflowId}`

- ⬜ **Workflow Runtime**
  - [ ] Workflow state tracking (on entity records)
  - [ ] Transition execution service
  - [ ] Transition validation (conditions)
  - [ ] Pre-transition rule evaluation
  - [ ] State change persistence
  - [ ] Workflow audit logging
  - [ ] API: `POST /api/v1/entities/{entityId}/records/{recordId}/transitions/{transitionId}`

### 6.2 Backend: Task System
- ⬜ **Task Management**
  - [ ] Task model (taskId, entityId, recordId, assignee, status, dueDate)
  - [ ] Task repository
  - [ ] Task service (create, update, complete)
  - [ ] Task assignment on workflow transitions
  - [ ] API: `GET /api/v1/tasks` (user's tasks)
  - [ ] API: `GET /api/v1/tasks/{taskId}`
  - [ ] API: `POST /api/v1/tasks/{taskId}/complete`
  - [ ] API: `POST /api/v1/tasks/{taskId}/decisions` (approve/reject/request-changes)

### 6.3 Frontend: Workflow UI
- ⬜ **Workflow Action Bar**
  - [ ] Workflow action bar component (on detail view)
  - [ ] Display current state
  - [ ] Show available transitions (from metadata)
  - [ ] Transition buttons
  - [ ] Transition confirmation dialogs
  - [ ] Transition comments/notes
  - [ ] State history display

### 6.4 Frontend: Tasks UI
- ⬜ **Tasks Page**
  - [ ] Tasks list page
  - [ ] Filter by status (open, in progress, done)
  - [ ] Task card component
  - [ ] Task detail view
  - [ ] Task actions (approve, reject, request changes)
  - [ ] Task comments
  - [ ] Link to related entity record

**Phase 6 Deliverable**: Users can manage workflows and complete tasks

---

## Phase 7: Validation & Rules
*Goal: Business rules are enforced*

### 7.1 Backend: Validation Engine
- ⬜ **Validation Framework**
  - [ ] Validation rule model (validationId, scope, ruleType, expression)
  - [ ] Field-level validation service
  - [ ] Entity-level validation service
  - [ ] Cross-entity validation service
  - [ ] Validation rule evaluation
  - [ ] Validation error collection
  - [ ] API validation integration

- ⬜ **Rule Engine**
  - [ ] Expression language parser
  - [ ] Rule evaluation context
  - [ ] Sandboxed execution
  - [ ] Deterministic rule evaluation
  - [ ] Version-aware rule execution

### 7.2 Frontend: Validation Display
- ⬜ **Form Validation**
  - [ ] Client-side validation (pre-submit)
  - [ ] Server validation error display
  - [ ] Field-level error messages
  - [ ] Entity-level error messages
  - [ ] Validation error summary

**Phase 7 Deliverable**: Business rules are enforced in forms and workflows

---

## Phase 8: Measures & Calculations
*Goal: Reusable business calculations work*

### 8.1 Backend: Measures Engine
- ⬜ **Measure Metadata**
  - [ ] Measure definition model (measureId, parameters, returnType, expression)
  - [ ] Measure metadata service
  - [ ] Measure versioning
  - [ ] API: `GET /api/v1/metadata/measures`
  - [ ] API: `POST /api/v1/metadata/measures` (admin)

- ⬜ **Measure Evaluation**
  - [ ] Measure evaluation service
  - [ ] Parameter resolution
  - [ ] Expression evaluation
  - [ ] Aggregate functions (sum, count, exists)
  - [ ] Measure caching (request-scoped)
  - [ ] Dependency tracking
  - [ ] API: `POST /api/v1/measures/{measureId}/evaluate`

### 8.2 Frontend: Measure Display
- ⬜ **Calculated Fields**
  - [ ] Display calculated field values
  - [ ] Measure result display in forms
  - [ ] Measure usage in validation (transparent to user)

**Phase 8 Deliverable**: Measures can be defined and used in rules

---

## Phase 9: Documents
*Goal: Users can manage controlled documents*

### 9.1 Backend: Document Control
- ⬜ **Document Metadata**
  - [ ] Controlled document model (documentId, title, type, status, versions)
  - [ ] Document revision model (revisionId, content, publishedAt)
  - [ ] Document metadata service
  - [ ] Document versioning
  - [ ] API: `GET /api/v1/documents`
  - [ ] API: `GET /api/v1/documents/{documentId}`
  - [ ] API: `POST /api/v1/documents` (create)
  - [ ] API: `POST /api/v1/documents/{documentId}/revisions` (publish)

### 9.2 Frontend: Document UI
- ⬜ **Document Management**
  - [ ] Documents list page
  - [ ] Document detail view
  - [ ] Document editor (Markdown/WYSIWYG)
  - [ ] Document version history
  - [ ] Document status display
  - [ ] Document workflow actions
  - [ ] Link documents to entities

**Phase 9 Deliverable**: Users can create and manage controlled documents

---

## Phase 10: Attachments
*Goal: Users can attach files to entities*

### 10.1 Backend: Attachment Service
- ⬜ **Attachment Management**
  - [ ] Attachment model (attachmentId, entityId, recordId, filename, contentType, size)
  - [ ] Attachment storage service
  - [ ] File upload handling
  - [ ] File download handling
  - [ ] Attachment metadata API
  - [ ] API: `POST /api/v1/entities/{entityId}/records/{recordId}/attachments`
  - [ ] API: `GET /api/v1/entities/{entityId}/records/{recordId}/attachments`
  - [ ] API: `GET /api/v1/attachments/{attachmentId}/download`
  - [ ] API: `DELETE /api/v1/attachments/{attachmentId}`

### 10.2 Frontend: Attachment UI
- ⬜ **Attachment Components**
  - [ ] Attachment list component (on detail view)
  - [ ] File upload component
  - [ ] File download links
  - [ ] Attachment preview (if applicable)
  - [ ] Delete attachment

**Phase 10 Deliverable**: Users can attach files to entity records

---

## Phase 11: Audit & History
*Goal: Users can view change history*

### 11.1 Backend: Audit System
- ⬜ **Audit Logging**
  - [ ] Audit event model (eventId, entityId, recordId, eventType, changes, actor, timestamp)
  - [ ] Audit service (record create, update, delete, transition)
  - [ ] Field-level change tracking
  - [ ] Audit query service
  - [ ] API: `GET /api/v1/entities/{entityId}/records/{recordId}/history`

### 11.2 Frontend: History UI
- ⬜ **History Display**
  - [ ] History timeline component (on detail view)
  - [ ] Display audit events
  - [ ] Show field changes (before/after)
  - [ ] Show workflow transitions
  - [ ] Filter by event type
  - [ ] User and timestamp display

**Phase 11 Deliverable**: Users can view complete change history

---

## Phase 12: Admin Capabilities
*Goal: Admins can configure the system*

### 12.1 Backend: Admin APIs
- ⬜ **User Management**
  - [ ] User CRUD APIs (admin only)
  - [ ] Role management APIs
  - [ ] Permission assignment APIs

- ⬜ **Metadata Management**
  - [ ] Entity definition CRUD (admin)
  - [ ] Workflow definition CRUD (admin)
  - [ ] Measure definition CRUD (admin)
  - [ ] Validation rule management

- ⬜ **System Configuration**
  - [ ] System settings API
  - [ ] Org topology management API
  - [ ] Licensing status API

### 12.2 Frontend: Admin UI
- ⬜ **Admin Pages**
  - [ ] User management page
  - [ ] Role management page
  - [ ] Entity definition editor (basic grid/form)
  - [ ] Workflow definition editor (basic grid)
  - [ ] Measure definition editor
  - [ ] System settings page
  - [ ] Org topology management page
  - [ ] Licensing status page

**Phase 12 Deliverable**: Admins can configure entities, workflows, and users

---

## Phase 13: Advanced Features
*Goal: Complete platform capabilities*

### 13.1 Scheduler
- ⬜ **Backend: Scheduler Engine**
  - [ ] Schedule definition model (scheduleId, rrule, action)
  - [ ] Scheduler service
  - [ ] Task creation from schedules
  - [ ] Entity record creation from schedules
  - [ ] API: `GET /api/v1/schedules`
  - [ ] API: `POST /api/v1/schedules` (admin)

- ⬜ **Frontend: Scheduler UI**
  - [ ] Schedules list page (admin)
  - [ ] Schedule creation form
  - [ ] Schedule execution history

### 13.2 Replication (Foundation)
- ⬜ **Backend: Replication Base**
  - [ ] Outbox event model
  - [ ] Inbox event model
  - [ ] Replication service (basic)
  - [ ] Event serialization
  - [ ] Idempotent event processing

### 13.3 Telemetry
- ⬜ **Backend: Telemetry**
  - [ ] Telemetry event model
  - [ ] Telemetry service
  - [ ] Telemetry storage
  - [ ] API: `GET /api/v1/telemetry` (admin)

### 13.4 Licensing (Foundation)
- ⬜ **Backend: Licensing**
  - [ ] License validation service
  - [ ] Entitlement service
  - [ ] Trial mode support
  - [ ] API: `GET /api/v1/licensing/status`

- ⬜ **Frontend: Licensing UI**
  - [ ] License status display (admin)
  - [ ] Trial expiration warnings

**Phase 13 Deliverable**: Complete platform with all core features

---

## Phase 14: Reference App
*Goal: Demonstrate platform capabilities*

### 14.1 Reference Implementation
- ⬜ **Purchase Order Entity**
  - [ ] Create PurchaseOrder entity definition
  - [ ] Define properties (requestedAmount, department, vendor, etc.)
  - [ ] Create sample records

- ⬜ **Approval Workflow**
  - [ ] Create approval workflow (Draft → Submitted → Approved → Rejected)
  - [ ] Define transitions
  - [ ] Assign to PurchaseOrder entity

- ⬜ **Budget Measure**
  - [ ] Create budget.available measure
  - [ ] Use in approval validation rule

- ⬜ **Scheduled Task**
  - [ ] Create 6-month audit reminder schedule
  - [ ] Test task creation

- ⬜ **Controlled Document**
  - [ ] Create SOP document
  - [ ] Publish document
  - [ ] Link to PurchaseOrder entity

**Phase 14 Deliverable**: Complete reference app demonstrating all capabilities

---

## 📊 Progress Tracking

### Overall Progress
- **Phase 1**: ⬜ 0/8 tasks
- **Phase 2**: ⬜ 0/4 tasks
- **Phase 3**: ⬜ 0/6 tasks
- **Phase 4**: ⬜ 0/4 tasks
- **Phase 5**: ⬜ 0/4 tasks
- **Phase 6**: ⬜ 0/8 tasks
- **Phase 7**: ⬜ 0/3 tasks
- **Phase 8**: ⬜ 0/3 tasks
- **Phase 9**: ⬜ 0/3 tasks
- **Phase 10**: ⬜ 0/3 tasks
- **Phase 11**: ⬜ 0/3 tasks
- **Phase 12**: ⬜ 0/4 tasks
- **Phase 13**: ⬜ 0/6 tasks
- **Phase 14**: ⬜ 0/5 tasks

**Total**: ⬜ 0/64 major task groups


---

## 📝 Notes

- Each phase builds on previous phases
- Backend APIs should be implemented before frontend UI
- Follow metadata-driven principles throughout
- Test each phase before moving to the next
- Update this document as tasks are completed

---

## 🔗 Related Documents

- **SPEC.md** - Complete specification
- **OSS_MENTAL_MODEL.md** - Core principles
- **AGENT_GUIDE.md** - How to implement tasks
- **TECH_STACK_AND_PROJECT_STRUCTURE_GUIDE.md** - Technical details

---

**Last Updated**: December 2025
