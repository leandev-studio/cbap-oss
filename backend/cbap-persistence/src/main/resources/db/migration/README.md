# Database Migrations

This directory contains Flyway migration scripts for the CBAP OSS database schema.

## Migration Files

### V1__Create_infrastructure_tables.sql
Creates infrastructure tables for authentication and system management:
- `cbap_users` - User accounts
- `cbap_roles` - Role definitions  
- `cbap_user_roles` - User-role mapping
- `cbap_permissions` - Permission definitions
- `cbap_role_permissions` - Role-permission mapping
- `cbap_password_reset_tokens` - Password reset tokens
- `cbap_org_units` - Organization topology (HQ/Region/Facility)

Also creates:
- UUID extension (uuid-ossp)
- Triggers for automatic `updated_at` timestamp updates

### V2__Create_metadata_foundation.sql
Creates foundation tables for metadata storage:
- `cbap_metadata_entities` - Entity definitions
- `cbap_metadata_properties` - Property definitions

These tables use JSONB for flexible metadata storage while maintaining queryability.

### V3__Seed_initial_data.sql
Inserts initial seed data:
- Default roles: Admin, User, Designer, Approver
- Default permissions (system, entity, workflow)
- Role-permission assignments
- Default admin user (username: `admin`, password: `admin123`)
- Default HQ OrgUnit

## Running Migrations

Migrations run automatically when the Spring Boot application starts. Flyway is configured in `application.yml`:

```yaml
spring:
  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-on-migrate: true
```

**Important**: This directory (`cbap-persistence/src/main/resources/db/migration/`) is the **single source of truth** for all Flyway migrations. The migrations are automatically copied to `cbap-app` during the Maven build process using the `maven-resources-plugin`, so you should **never** manually copy or create migrations in `cbap-app/src/main/resources/db/migration/`.

## Verification

After migrations run, verify the schema:

```bash
# Test connection and basic checks
./scripts/test-database-connection.sh

# Full schema verification
psql -h localhost -U cbap -d cbap -f scripts/verify-database-schema.sql
```

## Migration Naming

Follow Flyway naming convention:
- `V{version}__{description}.sql`
- Version numbers must be sequential
- Use double underscore `__` to separate version from description
- Example: `V1__Create_infrastructure_tables.sql`

## Best Practices

1. **Never modify existing migrations** - Create new migrations for changes
2. **Test migrations** - Test on a copy of production data before applying
3. **Backward compatibility** - Ensure migrations don't break existing data
4. **Idempotent operations** - Use `IF NOT EXISTS` where appropriate
5. **Transaction safety** - Each migration runs in a transaction

## Schema Design Principles

- **UUID primary keys** - All tables use UUID for primary keys
- **Timestamps** - Use `TIMESTAMP WITH TIME ZONE` for all timestamp fields
- **JSONB for flexibility** - Metadata stored in JSONB for extensibility
- **Audit fields** - All tables include `created_at`, `updated_at`, `created_by`
- **Tenant isolation** - Tables include `tenant_id` for multi-tenancy
- **Facility partitioning** - Tables include `facility_id` for data partitioning
- **Soft delete ready** - Status fields support soft delete pattern

## Default Admin User

After migrations:
- **Username**: `admin`
- **Password**: `admin123`
- **Role**: Admin (full system access)

**⚠️ IMPORTANT**: Change the default password in production!

## Troubleshooting

### Migration fails on startup

1. Check database connection in `application.yml`
2. Verify database exists
3. Check Flyway logs for specific errors
4. Ensure PostgreSQL version is 14+

### Tables not created

1. Check Flyway migration status: `SELECT * FROM flyway_schema_history;`
2. Verify migration files are in correct location
3. Check for syntax errors in SQL files
4. Verify Flyway is enabled in configuration

### Seed data missing

1. Check if V3 migration ran successfully
2. Verify no conflicts (seed data uses `ON CONFLICT DO NOTHING`)
3. Manually run V3 migration if needed
