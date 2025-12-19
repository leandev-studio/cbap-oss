# CBAP OSS - Composable Business Application Platform

[![License](https://img.shields.io/badge/license-GPLv3-blue.svg)](LICENSE)

**CBAP OSS** is a metadata-driven runtime for building business applications where data models are schema-flexible, workflows are user-defined, authorization is fine-grained, and UX is auto-generated.

## Overview

CBAP OSS provides the open-source core of the Composable Business Application Platform. It enables building complete business applications with zero custom code through metadata-driven configuration.

### Key Features

- **Metadata-Driven**: Business behavior defined declaratively, not in code
- **Schema Versioning**: No destructive schema changes; old data remains readable
- **Workflow Engine**: First-class workflow support with state-based permissions
- **Fine-Grained Authorization**: RBAC with entity and property-level permissions
- **Auto-Generated UX**: Forms, lists, and workflows generated from metadata
- **Replication**: Base replication mechanism for multi-site deployments
- **Telemetry**: Built-in observability and diagnostics

## Architecture

The platform follows a modular monorepo structure:

```
cbap-oss/
├── backend/          # Java/Spring Boot backend modules
├── frontend/         # React/TypeScript frontend
├── docs/             # Documentation
├── infra/            # Infrastructure as code
└── examples/         # Reference applications
```

## Technology Stack

### Backend
- **Language**: Java 21+
- **Framework**: Spring Boot 3.3+
- **Database**: PostgreSQL
- **Search**: OpenSearch
- **Build**: Maven

### Frontend
- **Framework**: React 18+ with TypeScript
- **UI Library**: Material UI (MUI)
- **Build Tool**: Vite
- **State Management**: React Query
- **Forms**: React Hook Form
- **Routing**: React Router

## Getting Started

### Prerequisites

- **Java 21+** (for backend)
- **Node.js 18+** (for frontend)
- **PostgreSQL 14+** (for database)
- **Maven 3.8+** (for backend build)
- **OpenSearch 2.x** (optional, for search features)

### Backend Setup

```bash
cd backend

# Build all modules
mvn clean install

# Run the application
cd cbap-app
mvn spring-boot:run
```

The backend will start on `http://localhost:8080` by default.

### Frontend Setup

```bash
cd frontend

# Install dependencies
npm install

# Start development server
npm run dev
```

The frontend will start on `http://localhost:3000` by default.

### Database Setup

1. Create a PostgreSQL database:
```sql
CREATE DATABASE cbap;
```

2. Update `backend/cbap-app/src/main/resources/application.yml` with your database credentials.

3. Flyway will automatically run migrations on startup.

## Documentation

- **[SPEC.md](docs/SPEC.md)**: Complete platform specification
- **[OSS_MENTAL_MODEL.md](docs/OSS_MENTAL_MODEL.md)**: Contributor mental model
- **[TECH_STACK_AND_PROJECT_STRUCTURE_GUIDE.md](docs/TECH_STACK_AND_PROJECT_STRUCTURE_GUIDE.md)**: Technology choices and project structure
- **[COLOR_GUIDE.md](docs/COLOR_GUIDE.md)**: UI color system and theming
- **[EXPRESSION_LANGUAGE_V0.md](docs/EXPRESSION_LANGUAGE_V0.md)**: Expression language for rules and measures

## Project Structure

### Backend Modules

- **cbap-core**: Core runtime engine (metadata, rules, workflow, auth)
- **cbap-api**: REST API layer
- **cbap-persistence**: Database access and repositories
- **cbap-search**: Search indexing and queries
- **cbap-security**: Authentication and authorization
- **cbap-bootstrap**: Startup and node registration
- **cbap-app**: Spring Boot application launcher

### Frontend Modules

- **app-shell**: Application shell and navigation
- **metadata-ui**: Generated forms and lists
- **workflow-ui**: Tasks and approvals
- **document-ui**: Controlled documents
- **dashboard-ui**: Dashboards and widgets
- **shared**: Common components and utilities

## Development

### Building

```bash
# Backend
cd backend && mvn clean install

# Frontend
cd frontend && npm run build
```

### Testing

```bash
# Backend tests
cd backend && mvn test

# Frontend tests (when implemented)
cd frontend && npm test
```

## Contributing

Please read the [OSS_MENTAL_MODEL.md](docs/OSS_MENTAL_MODEL.md) before contributing. Key principles:

- **Metadata over Code**: Business behavior is defined declaratively
- **No Domain Logic**: OSS core is domain-agnostic
- **Deterministic & Safe**: All logic must be deterministic and side-effect free
- **Backward Compatible**: Schema changes must not break existing data

## License

See [LICENSE](LICENSE) file for details.

## Status

This project is in **early development** (v0.1.0). The specification is stable, but implementation is ongoing.

---

**Note**: This is the open-source core. Enterprise features (visual designers, advanced analytics, native mobile apps, etc.) are reserved for paid editions.
