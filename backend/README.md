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

Requires PostgreSQL. Create the database before running:

```sql
CREATE DATABASE cbap;
-- Or for development:
CREATE DATABASE cbap_dev;
```

Update connection details in `application.yml` or use environment variables:

```yaml
spring:
  datasource:
    url: ${DB_URL:jdbc:postgresql://localhost:5432/cbap}
    username: ${DB_USERNAME:cbap}
    password: ${DB_PASSWORD:cbap}
```

**Profiles:**
- Default: Uses `cbap` database
- Dev profile (`--spring.profiles.active=dev`): Uses `cbap_dev` database
- Prod profile (`--spring.profiles.active=prod`): Uses production database

Flyway migrations run automatically on startup. Migrations are located in `cbap-persistence/src/main/resources/db/migration/` and are automatically copied to `cbap-app` during the build process.

## API

The API is available at `http://localhost:8080/api/v1` by default.

Health check: `http://localhost:8080/actuator/health`
