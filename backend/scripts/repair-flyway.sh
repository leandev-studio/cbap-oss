#!/bin/bash

# Script to repair Flyway checksums after updating migration files
# This is useful when you've modified an existing migration file

set -e

# Get the script directory and project root
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

echo "=========================================="
echo "Flyway Repair Script"
echo "=========================================="
echo ""

# Default values
DB_NAME="${1:-cbap_dev}"
DB_USER="${2:-cbap}"
DB_PASSWORD="${3:-cbap}"

echo "Database: $DB_NAME"
echo "User: $DB_USER"
echo ""

# Check if database exists
if ! psql -h localhost -U "$DB_USER" -d postgres -tc "SELECT 1 FROM pg_database WHERE datname = '$DB_NAME'" | grep -q 1; then
    echo "Error: Database '$DB_NAME' does not exist!"
    echo "Create it first: CREATE DATABASE $DB_NAME;"
    exit 1
fi

echo "Repairing Flyway checksums..."
echo "This will update the checksums in flyway_schema_history to match the current migration files."
echo ""

# Use Flyway Maven plugin to repair
cd "$PROJECT_ROOT/backend"
mvn flyway:repair -pl cbap-app \
    -Dflyway.url="jdbc:postgresql://localhost:5432/$DB_NAME" \
    -Dflyway.user="$DB_USER" \
    -Dflyway.password="$DB_PASSWORD" \
    -Dflyway.locations="classpath:db/migration" \
    -Dflyway.configFiles=cbap-app/src/main/resources/application-dev.yml

echo ""
echo "✓ Flyway repair completed!"
echo ""
echo "You can now restart the application."
