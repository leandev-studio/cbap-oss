# Database Setup Guide

## Quick Start

The application requires a PostgreSQL database. You have two options:

### Option 1: Create the default database

```sql
CREATE DATABASE cbap;
```

Then run the application normally:
```bash
mvn spring-boot:run
```

### Option 2: Use the dev profile (recommended for development)

The dev profile uses `cbap_dev` database. First create it:

```sql
CREATE DATABASE cbap_dev;
```

Then run with the dev profile:
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

Or set the environment variable:
```bash
export SPRING_PROFILES_ACTIVE=dev
mvn spring-boot:run
```

## Configuration Files

- **`application.yml`**: Default configuration (uses `cbap` database)
- **`application-dev.yml`**: Development profile (uses `cbap_dev` database)
- **`application-prod.yml`**: Production profile

## Database Configuration

The database connection can be configured via:

1. **Environment variables** (recommended):
   ```bash
   export DB_URL=jdbc:postgresql://localhost:5432/cbap_dev
   export DB_USERNAME=cbap
   export DB_PASSWORD=cbap
   ```

2. **Configuration files**: Edit `application.yml` or profile-specific files

3. **Command line**:
   ```bash
   mvn spring-boot:run -Dspring-boot.run.arguments="--spring.datasource.url=jdbc:postgresql://localhost:5432/cbap_dev"
   ```

## Setup Script

A helper script is available to create the database:

```bash
./scripts/setup-database.sh cbap cbap cbap
# Or for dev:
./scripts/setup-database.sh cbap_dev cbap cbap
```

## Database Migrations

Flyway automatically runs migrations on application startup. The migrations are located in:
- **Source**: `backend/cbap-persistence/src/main/resources/db/migration/` (single source of truth)
- **Runtime**: `cbap-app/target/classes/db/migration/` (copied during build)

**How it works:**
1. Migrations are stored in `cbap-persistence/src/main/resources/db/migration/`
2. During build, `maven-resources-plugin` copies them to `cbap-app/target/classes/db/migration/`
3. Migrations are **excluded from the `cbap-persistence` JAR** to prevent Flyway from finding duplicates
4. Flyway reads from `classpath:db/migration` (the copied location)

This ensures a single source of truth and prevents duplicate migration errors.

### Migration Files

- **V1__Create_infrastructure_tables.sql**: Creates infrastructure tables (users, roles, permissions, org_units)
- **V2__Create_metadata_foundation.sql**: Creates metadata foundation tables (entities, properties)
- **V3__Seed_initial_data.sql**: Inserts seed data (default roles, permissions, admin user)

### Verifying Migrations

After running the application, verify the schema:

```bash
# Using the verification script
./scripts/test-database-connection.sh

# Or manually check
psql -h localhost -U cbap -d cbap -f scripts/verify-database-schema.sql
```

### Default Credentials

After migrations run, you can log in with:
- **Username**: `admin`
- **Password**: `admin123` (change on first login)

**Note**: The default password is for development only. Change it in production!

## Current Configuration

- **Default (no profile)**: `cbap` database
- **Dev profile**: `cbap_dev` database
- **Prod profile**: Configure via environment variables

## Troubleshooting

### Error: "database does not exist"

**Solution**: Create the database first:
```sql
CREATE DATABASE cbap;
-- Or
CREATE DATABASE cbap_dev;
```

### Error: "password authentication failed"

**Solution**: Update the password in `application.yml` or set `DB_PASSWORD` environment variable.

### Using different database for development

**Solution**: Run with dev profile:
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```
