#!/bin/bash

# CBAP Database Setup Script
# Creates the required PostgreSQL databases for development

set -e

DB_NAME="${1:-cbap}"
DB_USER="${2:-cbap}"
DB_PASSWORD="${3:-cbap}"

echo "Setting up CBAP database: $DB_NAME"

# Check if PostgreSQL is running
if ! pg_isready -h localhost -p 5432 > /dev/null 2>&1; then
    echo "Error: PostgreSQL is not running on localhost:5432"
    exit 1
fi

# Create database if it doesn't exist
psql -h localhost -U postgres -tc "SELECT 1 FROM pg_database WHERE datname = '$DB_NAME'" | grep -q 1 || \
    psql -h localhost -U postgres -c "CREATE DATABASE $DB_NAME;"

# Create user if it doesn't exist
psql -h localhost -U postgres -tc "SELECT 1 FROM pg_user WHERE usename = '$DB_USER'" | grep -q 1 || \
    psql -h localhost -U postgres -c "CREATE USER $DB_USER WITH PASSWORD '$DB_PASSWORD';"

# Grant privileges
psql -h localhost -U postgres -c "GRANT ALL PRIVILEGES ON DATABASE $DB_NAME TO $DB_USER;"

echo "Database $DB_NAME setup complete!"
echo ""
echo "To use this database:"
echo "  - Default: Already configured in application.yml"
echo "  - Dev profile: Set DB_URL=jdbc:postgresql://localhost:5432/${DB_NAME}_dev"
echo ""
