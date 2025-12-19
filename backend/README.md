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

**Important**: Always build from the root `backend/` directory to ensure all modules are compiled and installed to your local Maven repository:

```bash
# From backend/ directory
mvn clean install
```

This will:
1. Compile all modules in the correct order
2. Install them to your local Maven repository (`~/.m2/repository`)
3. Ensure `cbap-app` can find all dependencies

## Running

### Development Mode (Recommended)

**Always run from the root `backend/` directory** to ensure all modules are built:

```bash
# From backend/ directory - this builds all modules first
mvn spring-boot:run -pl cbap-app -Dspring-boot.run.profiles=dev
```

Or use the convenience script:
```bash
# From backend/ directory
./scripts/run-dev.sh
```

### Running with Dev Profile

```bash
# From backend/ directory
mvn spring-boot:run -pl cbap-app -Dspring-boot.run.profiles=dev
```

### Hot Reload (Spring Boot DevTools)

Spring Boot DevTools is included for automatic restart on code changes. After making changes:

1. **For Java code changes**: Save the file - DevTools will automatically restart the application
2. **For configuration changes**: Restart manually
3. **For multi-module changes**: Run `mvn install` from root first, then restart

**Note**: If you make changes to other modules (e.g., `cbap-security`), you must:
1. Run `mvn install` from the `backend/` directory to rebuild and install all modules
2. Restart the application (DevTools will do this automatically if running)

### Environment Variables

You can configure the backend using environment variables:

- `FRONTEND_ORIGIN`: Frontend URL for CORS (default: `http://localhost:3000`)
- `DB_URL`: Database connection URL
- `DB_USERNAME`: Database username
- `DB_PASSWORD`: Database password
- `SERVER_PORT`: Server port (default: `8080`)
- `JWT_SECRET`: JWT signing secret (change in production!)

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

### Flyway Checksum Issues

If you modify an existing migration file after it's been applied, Flyway will detect a checksum mismatch. To fix this:

**Option 1: Automatic Repair (Dev only)**
The dev profile has `repair-on-migrate: true` which will automatically repair checksums on startup.

**Option 2: Manual Repair**
```bash
./scripts/repair-flyway.sh cbap_dev cbap cbap
```

**Option 3: Reset Database (Development)**
```sql
DROP DATABASE cbap_dev;
CREATE DATABASE cbap_dev;
```
Then restart the application.

## API

The API is available at `http://localhost:8080/api/v1` by default.

Health check: `http://localhost:8080/actuator/health`
