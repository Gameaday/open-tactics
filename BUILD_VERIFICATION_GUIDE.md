# Build Verification and Testing Guide

## Overview
This document provides comprehensive guidance on verifying builds, running tests, and ensuring quality in the Open Tactics project. It covers all CI/CD checks and how to run them locally.

## Quick Verification

### Run All CI Checks
```bash
./gradlew ciVerification
```

This runs:
- Code formatting (ktlint)
- Static analysis (detekt)
- Android Lint
- All unit tests (standalone + app)
- Code coverage analysis
- Debug APK builds

### Individual Checks

#### 1. Code Formatting
```bash
# Check formatting
./gradlew ktlintCheck

# Auto-fix formatting
./gradlew ktlintFormat
```

#### 2. Static Analysis
```bash
# Run detekt
./gradlew detekt

# View report at: app/build/reports/detekt/detekt.html
```

#### 3. Android Lint
```bash
# Run lint checks
./gradlew lint

# View report at: app/build/reports/lint-results-*.html
```

#### 4. Security Analysis
```bash
# Run OWASP dependency check (optimized for speed)
./gradlew dependencyCheckAnalyze

# View report at: app/build/reports/dependency-check/dependency-check-report.html
```

## Testing

### Unit Tests

#### Run All Unit Tests
```bash
# All modules
./gradlew test

# Specific module
./gradlew :standalone:test
./gradlew :app:test

# All app flavors
./gradlew testAllUnitTests
```

#### Run Specific Test Classes
```bash
# Standalone performance tests
./gradlew :standalone:test --tests "*Performance*"

# App model tests
./gradlew :app:testDevDebugUnitTest --tests "*.model.*"

# Game logic tests
./gradlew :app:testDevDebugUnitTest --tests "*.game.*"
```

### Code Coverage

#### Generate Coverage Report
```bash
# Generate HTML coverage report
./gradlew jacocoTestReport

# View at: app/build/reports/jacoco/jacocoTestReport/html/index.html
```

#### Verify Coverage Thresholds
```bash
# Check if coverage meets minimum requirements
./gradlew jacocoTestCoverageVerification
```

Current coverage thresholds:
- Bundle (overall): 25%
- Individual classes: 38%

Coverage by package:
- `model`: 67% (core game models)
- `game`: 38% (game logic)
- `view`: Excluded (requires instrumented tests)
- `data`: Excluded (requires instrumented tests)

### Instrumented Tests (Android-Specific)

Instrumented tests run on actual devices or emulators to verify Android-specific functionality like Activities, Views, and Android framework integration.

#### Prerequisites
- Connected Android device with USB debugging enabled, OR
- Running Android emulator

#### Run All Instrumented Tests
```bash
# All flavors
./gradlew connectedAllDebugAndroidTest

# Specific flavor
./gradlew connectedDevDebugAndroidTest
./gradlew connectedProdDebugAndroidTest
```

#### View Test Results
Reports are generated at:
- `app/build/reports/androidTests/connected/`
- `app/build/outputs/androidTest-results/`

#### Test Coverage
Instrumented tests cover:
- **MainActivity**: Launch, navigation, button interactions
- **GameActivity**: Game UI, action buttons, unit info display
- **ChapterSelectActivity**: Chapter selection UI
- **View components**: Custom game views (TacticalGameView)
- **Data layer**: SaveGameManager, database operations

## Building

### Debug Builds
```bash
# Build all debug variants
./gradlew assembleAllDebug

# Specific variants
./gradlew assembleDevDebug
./gradlew assembleProdDebug
```

### Staging Builds
```bash
./gradlew assembleDevStaging
./gradlew assembleProdStaging
```

### Release Builds
```bash
# Requires signing configuration
./gradlew assembleDevRelease
./gradlew assembleProdRelease
```

### APK Location
Built APKs are located at:
```
app/build/outputs/apk/{flavor}/{buildType}/
```

Example:
- `app/build/outputs/apk/dev/debug/app-dev-debug.apk`

## Android Compatibility

### Test Compatibility with Different API Levels
```bash
# Test minimum SDK (API 24)
./gradlew assembleDevDebug -PminSdkVersion=24

# Test target SDK (API 34)
./gradlew assembleDevDebug -PminSdkVersion=34
```

### Verify APK for Different Architectures
The project builds universal APKs by default. To build split APKs per architecture:
```bash
# Configure in app/build.gradle.kts if needed
# splits { abi { ... } }
```

## Documentation

### Generate API Documentation
```bash
# Generate Dokka HTML documentation
./gradlew dokkaHtml

# View documentation
open app/build/dokka/html/index.html
open standalone/build/dokka/html/index.html
```

## Console Demo

### Run Standalone Console Demo
```bash
./gradlew :standalone:run
```

This runs a text-based tactical RPG demo showcasing core game mechanics without Android dependencies.

## Continuous Integration

### CI Workflows

#### Main CI Workflow (`ci.yml`)
Runs on every push/PR:
1. **Lint Check**: ktlint formatting
2. **Unit Tests**: All unit tests with coverage
3. **Build Debug**: Dev debug APK
4. **Build Staging**: Dev staging APK
5. **Instrumented Tests**: On API 24, 29, 34 (main/develop branches only)

#### QA Workflow (`qa.yml`)
Additional quality checks:
1. **Code Quality**: Android Lint + Detekt
2. **Performance Tests**: Standalone performance tests
3. **Security Analysis**: OWASP dependency check
4. **Documentation**: Dokka generation
5. **Compatibility**: Build for API 24, 29, 34, 35

### Local CI Simulation

To simulate CI locally before pushing:

```bash
# 1. Run all quality checks
./gradlew ciVerification

# 2. Run security analysis (optional, takes longer)
./gradlew dependencyCheckAnalyze

# 3. Generate documentation
./gradlew dokkaHtml

# 4. Run instrumented tests (if device available)
./gradlew connectedDevDebugAndroidTest

# 5. Test standalone module
./gradlew :standalone:run
```

## Troubleshooting

### Build Cache Issues
```bash
# Clean build
./gradlew clean

# Clean and rebuild
./gradlew clean assembleDevDebug
```

### Gradle Daemon Issues
```bash
# Stop all Gradle daemons
./gradlew --stop

# Run with --no-daemon
./gradlew test --no-daemon
```

### Test Failures
```bash
# Run tests with stack traces
./gradlew test --stacktrace

# Run with info logging
./gradlew test --info

# Run specific test with debug
./gradlew test --tests "TestClassName" --debug
```

### Instrumented Test Issues
```bash
# List connected devices
adb devices

# Check logcat during tests
adb logcat

# Clear app data before testing
adb shell pm clear com.gameaday.opentactics.dev
```

## Performance Optimization

### Gradle Build Performance
```bash
# Use configuration cache
./gradlew build --configuration-cache

# Use build cache
./gradlew build --build-cache

# Parallel execution (default, but can be explicit)
./gradlew build --parallel
```

### Test Execution Performance
```bash
# Run tests in parallel (when possible)
./gradlew test --parallel

# Skip expensive checks during development
./gradlew assembleDevDebug -x lint -x detekt
```

## Best Practices

### Before Committing
1. Run code formatting: `./gradlew ktlintFormat`
2. Run unit tests: `./gradlew test`
3. Check coverage: `./gradlew jacocoTestCoverageVerification`
4. Run lint: `./gradlew lint`

### Before Merging
1. Run full CI verification: `./gradlew ciVerification`
2. Check detekt: `./gradlew detekt`
3. Verify builds: `./gradlew assembleAllDebug`
4. Run instrumented tests: `./gradlew connectedDevDebugAndroidTest` (if device available)

### When Adding Dependencies
1. Check for vulnerabilities: `./gradlew dependencyCheckAnalyze`
2. Verify license compatibility
3. Update suppressions.xml if needed

### When Changing Android-Specific Code
1. Run instrumented tests: `./gradlew connectedDevDebugAndroidTest`
2. Test on multiple API levels if possible
3. Verify on both debug and release builds

## Test Coverage Goals

### Current Status
- **Overall**: 26% (unit tests only)
- **Models**: 67% (well covered)
- **Game Logic**: 38% (adequately covered)
- **UI/Activities**: Requires instrumented tests
- **Data Layer**: Requires instrumented tests

### Improvement Strategy
1. ✅ Add instrumented test infrastructure
2. ✅ Create basic Activity tests
3. 🔄 Expand instrumented test coverage
4. 🔄 Add UI interaction tests
5. 🔄 Test data persistence
6. 🔄 Gradually increase thresholds

## Resources

### Test Reports Locations
- Unit tests: `app/build/reports/tests/`
- Instrumented tests: `app/build/reports/androidTests/`
- Coverage: `app/build/reports/jacoco/`
- Lint: `app/build/reports/lint-results-*.html`
- Detekt: `app/build/reports/detekt/detekt.html`
- OWASP: `app/build/reports/dependency-check/`

### Documentation
- API docs: `app/build/dokka/html/`
- Project docs: `README.md`
- Implementation notes: `*_IMPLEMENTATION.md` files

### CI/CD Workflows
- `.github/workflows/ci.yml` - Main CI
- `.github/workflows/qa.yml` - Quality assurance
- `.github/workflows/cd.yml` - Continuous deployment
- `.github/workflows/dependencies.yml` - Dependency updates
