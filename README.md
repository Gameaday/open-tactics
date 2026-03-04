# Open Tactics

A Fire Emblem-style tactical RPG for Android, built with modern Kotlin architecture and comprehensive CI/CD.

[![CI](https://github.com/Gameaday/open-tactics/actions/workflows/ci.yml/badge.svg)](https://github.com/Gameaday/open-tactics/actions/workflows/ci.yml)
[![CD](https://github.com/Gameaday/open-tactics/actions/workflows/cd.yml/badge.svg)](https://github.com/Gameaday/open-tactics/actions/workflows/cd.yml)
[![QA](https://github.com/Gameaday/open-tactics/actions/workflows/qa.yml/badge.svg)](https://github.com/Gameaday/open-tactics/actions/workflows/qa.yml)

## 📋 Project Status

**Status:** Pre-Alpha / Functional Prototype (March 2026)  
**Target Launch:** February 2027

| Area | Status |
|------|--------|
| Core game engine | ✅ Complete |
| 20-chapter campaign | ✅ Content defined |
| Character & combat systems | ✅ Complete |
| AI with healing behavior | ✅ Complete |
| Difficulty modes (Easy/Normal/Hard) | ✅ Complete |
| Achievement system (10 achievements) | ✅ Complete |
| Support conversations (7 pairs) | ✅ Complete |
| Battle quotes | ✅ Complete |
| Save/load system | ✅ Complete |
| Static analysis (detekt) | ✅ 0 issues |
| Code formatting (ktlint) | ✅ Clean |
| Sprite graphics | ❌ Not started |
| Audio/music | ❌ Not started |
| Tutorial chapter | ❌ Not started |

See also:
- **[TECHNICAL_DEBT.md](TECHNICAL_DEBT.md)** — Current tech debt status
- **[STRATEGIC_PLAN.md](STRATEGIC_PLAN.md)** — 12-month roadmap to launch
- **[CHANGELOG.md](CHANGELOG.md)** — Version history

## Features

### Core Gameplay
- **Grid-based tactical combat** with positioning and movement strategy
- **7 character classes** with unique stats and abilities:
  - Knight — High HP/defense, melee combat
  - Archer — Long-range attacks with moderate mobility  
  - Mage — Magical damage with medium range
  - Healer — Support role with healing staves
  - Thief — High mobility and speed
  - Pegasus Knight — Flying unit, high mobility
  - Wyvern Rider — Flying unit, high defense
- **Turn-based combat system** with player and AI phases
- **Weapon triangle** (Sword > Axe > Lance > Sword)
- **Critical hits** based on skill/luck stats
- **Terrain effects** (plains, forests, mountains, forts — affect movement and combat)
- **Experience and leveling** with stat growth rates
- **Difficulty scaling** (Easy/Normal/Hard)

### Campaign & Story
- **20-chapter campaign** across 4 acts:
  - Act 1 (Ch 1-5): Defense — Tutorial and early battles
  - Act 2 (Ch 6-10): Counterattack — New allies join
  - Act 3 (Ch 11-15): Invasion — Challenging encounters
  - Finale (Ch 16-20): Victory — Epic conclusion
- **6 named protagonist characters** with custom growth rates and battle quotes
- **Support conversations** between 7 character pairs (C/B/A ranks)
- **Chapter objectives**: Defeat All, Defeat Boss, Seize Throne, Survive, Escape, Defend
- **Reinforcement spawns** on specific turns

### Save System & Progression
- **Encrypted save/load** with auto-save
- **Player profiles** with statistics tracking
- **10 achievements** (First Victory, Veteran, Campaign Complete, etc.)
- **Chapter replay** support

### AI System
- **4 AI behavior patterns**: Aggressive, Defensive, Stationary, Support
- **AI healing** — enemy healers use staves on wounded allies
- **Boss behavior** — defensive positioning with powerful attacks

### Technical
- Modern Kotlin codebase (JDK 17, Android SDK 35)
- Comprehensive unit test coverage (22+ test files)
- Both Android app and standalone console modules
- Clean architecture with model/game/view/factory separation

## Development

### Prerequisites
- Android Studio Hedgehog or later  
- JDK 21 LTS
- Android SDK 24+

### Build Variants
- **Debug**: Development builds with debug symbols
- **Staging**: Pre-production builds for testing  
- **Release**: Production builds for Play Store

### Product Flavors
- **Dev**: Development environment with debug features
- **Prod**: Production environment with analytics

### Quick Start

#### Clone and Build
```bash
git clone https://github.com/Gameaday/open-tactics.git
cd open-tactics

# Build debug variants for all flavors
./gradlew assembleAllDebug

# Or build specific variant
./gradlew assembleDevDebug
./gradlew assembleProdDebug
```

#### Run Console Demo
```bash
./gradlew :standalone:run
```

#### Run Tests
```bash
# Run all module tests (standalone + app)
./gradlew test

# Run all app unit tests across all flavors
./gradlew testAllUnitTests

# Run tests for specific flavor
./gradlew testDevDebugUnitTest
./gradlew testProdDebugUnitTest

# Run instrumented tests (requires connected device/emulator)
./gradlew connectedDevDebugAndroidTest
```

#### Test Coverage
```bash
# Generate coverage report
./gradlew jacocoTestReport

# Verify coverage meets minimum requirements
./gradlew jacocoTestCoverageVerification

# View HTML coverage report
open app/build/reports/jacoco/jacocoTestReport/html/index.html
```

#### Instrumented Tests
```bash
# Run all instrumented tests (requires device/emulator)
./gradlew connectedAllDebugAndroidTest

# Run specific flavor tests
./gradlew connectedDevDebugAndroidTest
./gradlew connectedProdDebugAndroidTest
```

#### Comprehensive CI Verification
```bash
# Run all CI checks locally (recommended before pushing)
./gradlew ciVerification
```

For testing details, see the **Run Tests** section above.

### Code Quality

#### Linting and Formatting
```bash
# Check code style
./gradlew ktlintCheck

# Auto-format code
./gradlew ktlintFormat

# Static analysis
./gradlew detekt

# Android lint
./gradlew lint
```

#### Quality Gates
- ktlint for code formatting
- Detekt for static analysis
- Android Lint for Android-specific issues
- OWASP dependency check for security
- Comprehensive test coverage

### CI/CD Pipeline

#### Continuous Integration
- **Code Quality**: ktlint, detekt, Android Lint
- **Testing**: Unit tests, instrumented tests across multiple API levels (24, 29, 34)
- **Security**: OWASP dependency check (7-day cache, optimized for CI), secret scanning
- **Performance**: Build size analysis, performance tests
- **Coverage**: JaCoCo test coverage verification (25% bundle, 38% per class)

#### Test Coverage
- **Unit Tests**: 22+ test files covering models, factories, game logic, achievements
- **Instrumented Tests**: 4 test files for Activities and UI components
- **Standalone Tests**: 3 test files for console module
- **Total Coverage**: Models ~67%, Game Logic ~38%, Overall ≥25%

#### Continuous Deployment
- **Automated Versioning**: Based on git commits and tags
- **Signed Builds**: Release builds with proper signing
- **Play Store**: Automated deployment to internal/alpha/beta tracks
- **GitHub Releases**: Automatic release creation with APKs

#### Workflows
- **CI** (`ci.yml`): Runs on every push/PR
- **CD** (`cd.yml`): Runs on tags and manual dispatch
- **QA** (`qa.yml`): Additional quality checks
- **Dependencies** (`dependencies.yml`): Weekly dependency updates

### Release Process

#### Versioning
- Version code: Auto-generated from git commit count
- Version name: Semantic versioning (e.g., 1.0.0)
- Git tags: `v1.0.0`, `v1.0.0-beta`, `v1.0.0-alpha`

#### Play Store Tracks
- **Internal**: Development builds (`internal` track)
- **Alpha**: Pre-release builds (`alpha` track)  
- **Beta**: Release candidates (`beta` track)
- **Production**: Public releases (`production` track)

#### Creating a Release
```bash
# Tag a release
git tag v1.0.0
git push origin v1.0.0

# Manual deployment
gh workflow run cd.yml -f release_type=beta
```

### Security

#### Signing Configuration
Store signing credentials as GitHub Secrets:
- `KEYSTORE_BASE64`: Base64-encoded keystore file
- `KEYSTORE_PASSWORD`: Keystore password
- `KEY_ALIAS`: Key alias
- `KEY_PASSWORD`: Key password

#### Play Store Deployment
- `PLAY_STORE_SERVICE_ACCOUNT`: Service account JSON for Play Store API

### Project Structure

```
├── app/                          # Android app module
│   ├── src/main/java/           # Main source code
│   │   ├── data/                # Save system, game data, achievements
│   │   ├── factory/             # Character, weapon, map, item factories
│   │   ├── game/                # Game state and battle logic
│   │   ├── model/               # Core models (Character, Weapon, Chapter, etc.)
│   │   └── view/                # Game board rendering
│   ├── src/test/java/           # Unit tests (22+ test files)
│   └── src/androidTest/java/    # Instrumented tests (4 test files)
├── standalone/                   # Console demo module
│   ├── src/main/kotlin/         # Standalone game logic (no Android deps)
│   └── src/test/kotlin/         # Standalone tests
├── config/                      # Quality tools configuration
│   ├── detekt/                  # Detekt static analysis rules
│   └── owasp/                   # OWASP dependency check suppressions
├── fastlane/                    # Play Store metadata
├── .github/workflows/           # CI/CD workflows
└── gradle/libs.versions.toml    # Version catalog
```

### Documentation

Generate documentation:
```bash
./gradlew dokkaHtml
```

View generated docs in `build/dokka/html/index.html`

## Gameplay Overview

The game follows classic Fire Emblem mechanics:

1. **Unit Selection**: Choose from your available characters
2. **Movement Phase**: Move units across the tactical grid
3. **Action Phase**: Attack enemies or use abilities
4. **Enemy Turn**: AI-controlled opponents take their actions
5. **Victory Conditions**: Defeat all enemies or complete objectives

### Combat System
- Damage based on attacker's attack stat vs defender's defense
- Position matters - terrain provides defensive bonuses
- Experience gained from combat and defeating enemies
- Permadeath - fallen units are removed from battle

## Next Milestones

The core tactical engine and campaign content are complete. Remaining work for launch:

1. **Phase 1 — Foundation Polish** (in progress): Tutorial chapter, settings UI, accessibility
2. **Phase 2 — Visual Transformation**: Pixel art sprites, terrain tileset, animations
3. **Phase 3 — Audio**: Sound effects, background music
4. **Phase 4 — Content Polish**: Balance testing, difficulty tuning, story polish
5. **Phase 5 — Beta Testing**: Closed beta with feedback cycle
6. **Phase 6 — Marketing & Launch**: Play Store optimization, press kit

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Run comprehensive CI checks (`./gradlew ciVerification`)
4. Commit your changes (`git commit -m 'Add amazing feature'`)
5. Push to the branch (`git push origin feature/amazing-feature`)
6. Open a Pull Request

### Code Standards
- Follow Kotlin coding conventions
- Maintain test coverage above 25% (unit tests), add instrumented tests for UI
- All code must pass ktlint and detekt checks
- Include tests for new functionality (unit and/or instrumented)
- Update documentation for public APIs

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Development Notes

This project demonstrates a complete tactical RPG engine suitable for mobile deployment. The clean separation between game logic and presentation layers makes it easy to add different UI implementations while maintaining the same core gameplay experience.

The CI/CD pipeline ensures code quality and enables reliable releases to the Play Store with automated testing and deployment.