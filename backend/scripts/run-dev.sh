#!/bin/bash

# Development run script for CBAP Backend
# Ensures all modules are built before running the application

set -e

# Get the script directory and project root
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

echo "=========================================="
echo "CBAP Backend - Development Mode"
echo "=========================================="
echo ""

# Change to project root
cd "$PROJECT_ROOT"

# Check if we need to build
echo "Checking if build is needed..."
if [ ! -f "cbap-app/target/cbap-app-*.jar" ] || \
   [ "cbap-security/src/main/java" -nt "cbap-security/target/classes" ] || \
   [ "cbap-api/src/main/java" -nt "cbap-api/target/classes" ] || \
   [ "cbap-persistence/src/main/java" -nt "cbap-persistence/target/classes" ]; then
    echo "Building all modules..."
    mvn clean install -DskipTests
    echo ""
else
    echo "Modules are up to date. Skipping build."
    echo "To force rebuild, run: mvn clean install"
    echo ""
fi

# Run the application
echo "Starting CBAP Application in dev mode..."
echo ""
mvn spring-boot:run -pl cbap-app -Dspring-boot.run.profiles=dev
