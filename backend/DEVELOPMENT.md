# Development Guide

## Multi-Module Build Issues

This project uses Maven multi-module architecture. When developing, it's crucial to understand how modules depend on each other and when to rebuild.

## Module Dependencies

```
cbap-app (Spring Boot launcher)
  ├── cbap-api
  │     ├── cbap-core
  │     ├── cbap-persistence
  │     └── cbap-security
  │           └── cbap-persistence
  ├── cbap-persistence
  ├── cbap-search
  └── cbap-bootstrap
```

## Development Workflow

### Initial Setup

1. **Always build from root first**:
   ```bash
   cd backend/
   mvn clean install
   ```

   This compiles all modules and installs them to your local Maven repository (`~/.m2/repository`).

### Making Changes

#### Changes to `cbap-app` only
- Save the file
- Spring Boot DevTools will automatically restart
- No manual build needed

#### Changes to other modules (e.g., `cbap-security`, `cbap-api`)
1. **Rebuild all modules**:
   ```bash
   cd backend/
   mvn clean install -DskipTests
   ```

2. **Restart the application** (DevTools will do this automatically if running)

#### Why rebuild is needed
When you change code in `cbap-security` or `cbap-api`:
- The compiled `.class` files in `target/classes` need to be updated
- These classes need to be packaged into `.jar` files
- The `.jar` files need to be installed to `~/.m2/repository`
- `cbap-app` loads these from the local repository, not from source

### Running in Development

#### Option 1: Use the convenience script (Recommended)
```bash
cd backend/
./scripts/run-dev.sh
```

This script:
- Checks if rebuild is needed
- Builds all modules if necessary
- Runs the application in dev mode

#### Option 2: Manual build and run
```bash
# Build all modules
cd backend/
mvn clean install -DskipTests

# Run the application
mvn spring-boot:run -pl cbap-app -Dspring-boot.run.profiles=dev
```

#### Option 3: IDE Run Configuration
If running from IDE (IntelliJ IDEA, Eclipse):
1. **First time**: Run `mvn install` from root to install all modules
2. **After changes to other modules**: Run `mvn install` again
3. **IDE will use classes from local repository**

### Troubleshooting

#### "Cannot find symbol" or "Package does not exist" errors
- **Cause**: Modules not built or not installed to local repository
- **Fix**: Run `mvn clean install` from `backend/` directory

#### Changes not reflected after restart
- **Cause**: Module not rebuilt after changes
- **Fix**: Run `mvn install` from `backend/` directory

#### "ClassNotFoundException" at runtime
- **Cause**: Dependency not installed or version mismatch
- **Fix**: 
  1. Run `mvn clean install` from `backend/` directory
  2. Check that all modules are listed in parent `pom.xml`
  3. Verify dependency versions match

### Best Practices

1. **Always build from root**: `cd backend/ && mvn install`
2. **Use the dev script**: `./scripts/run-dev.sh` handles rebuild checks
3. **Watch for module changes**: If you edit `cbap-security`, `cbap-api`, etc., rebuild
4. **Check build output**: Look for "BUILD SUCCESS" before running
5. **Use Spring Boot DevTools**: It auto-restarts on changes (after rebuild)

### Quick Reference

```bash
# Full rebuild (do this after changes to any module)
cd backend/
mvn clean install -DskipTests

# Quick run (assumes modules are already built)
cd backend/
mvn spring-boot:run -pl cbap-app -Dspring-boot.run.profiles=dev

# Check if rebuild needed (compare timestamps)
ls -la cbap-security/target/classes
ls -la cbap-security/src/main/java

# Force rebuild specific module
cd backend/
mvn clean install -pl cbap-security -am
```
