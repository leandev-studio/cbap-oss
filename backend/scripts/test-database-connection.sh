#!/bin/bash

# CBAP OSS - Database Connection Test Script
# Tests PostgreSQL connection and verifies Flyway migrations

set -e

DB_HOST="${DB_HOST:-localhost}"
DB_PORT="${DB_PORT:-5432}"
DB_NAME="${DB_NAME:-cbap}"
DB_USER="${DB_USER:-cbap}"
DB_PASSWORD="${DB_PASSWORD:-cbap}"

echo "Testing database connection..."
echo "Host: $DB_HOST"
echo "Port: $DB_PORT"
echo "Database: $DB_NAME"
echo "User: $DB_USER"
echo ""

# Test connection
PGPASSWORD="$DB_PASSWORD" psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -c "SELECT version();" > /dev/null 2>&1

if [ $? -eq 0 ]; then
    echo "✅ Database connection successful"
else
    echo "❌ Database connection failed"
    exit 1
fi

# Check if tables exist
echo ""
echo "Checking for CBAP tables..."
TABLE_COUNT=$(PGPASSWORD="$DB_PASSWORD" psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -t -c "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'public' AND table_name LIKE 'cbap_%';" | tr -d ' ')

if [ "$TABLE_COUNT" -ge 9 ]; then
    echo "✅ Found $TABLE_COUNT CBAP tables"
else
    echo "⚠️  Found only $TABLE_COUNT CBAP tables (expected at least 9)"
    echo "   Run migrations: mvn spring-boot:run or check Flyway logs"
fi

# Check for seed data
echo ""
echo "Checking seed data..."
ADMIN_EXISTS=$(PGPASSWORD="$DB_PASSWORD" psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -t -c "SELECT COUNT(*) FROM cbap_users WHERE username = 'admin';" | tr -d ' ')

if [ "$ADMIN_EXISTS" -eq 1 ]; then
    echo "✅ Admin user exists"
else
    echo "⚠️  Admin user not found - migrations may not have run"
fi

ROLE_COUNT=$(PGPASSWORD="$DB_PASSWORD" psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -t -c "SELECT COUNT(*) FROM cbap_roles;" | tr -d ' ')

if [ "$ROLE_COUNT" -ge 4 ]; then
    echo "✅ Found $ROLE_COUNT roles (expected at least 4)"
else
    echo "⚠️  Found only $ROLE_COUNT roles (expected at least 4)"
fi

echo ""
echo "✅ Database connection test complete"
