# CBAP Backend

Java/Spring Boot backend for the Composable Business Application Platform.

## Modules

- **cbap-core**: Core runtime engine
- **cbap-api**: REST API layer
- **cbap-persistence**: Database access
- **cbap-search**: Search indexing
- **cbap-security**: Authentication/authorization
- **cbap-bootstrap**: Startup and initialization
- **cbap-app**: Spring Boot application launcher

## Building

```bash
mvn clean install
```

## Running

```bash
cd cbap-app
mvn spring-boot:run
```

## Configuration

Configuration files are in `cbap-app/src/main/resources/`:

- `application.yml`: Base configuration
- `application-dev.yml`: Development overrides
- `application-prod.yml`: Production overrides

## Database

Requires PostgreSQL. Update connection details in `application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/cbap
    username: cbap
    password: cbap
```

Flyway migrations run automatically on startup.

## API

The API is available at `http://localhost:8080/api/v1` by default.

Health check: `http://localhost:8080/actuator/health`
