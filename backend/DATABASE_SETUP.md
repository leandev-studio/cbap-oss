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
