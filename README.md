# Open Tactics

A Fire Emblem-style tactical RPG for Android, built with modern Kotlin architecture and comprehensive CI/CD.

[![CI](https://github.com/Gameaday/open-tactics/actions/workflows/ci.yml/badge.svg)](https://github.com/Gameaday/open-tactics/actions/workflows/ci.yml)
[![CD](https://github.com/Gameaday/open-tactics/actions/workflows/cd.yml/badge.svg)](https://github.com/Gameaday/open-tactics/actions/workflows/cd.yml)
[![QA](https://github.com/Gameaday/open-tactics/actions/workflows/qa.yml/badge.svg)](https://github.com/Gameaday/open-tactics/actions/workflows/qa.yml)

## Features

### Core Gameplay
- **Grid-based tactical combat** with positioning and movement strategy
- **5 character classes** with unique stats and abilities:
  - Knight: High HP/defense, short-range melee combat
  - Archer: Long-range attacks with moderate mobility  
  - Mage: Magical damage with medium range
  - Healer: Support role with healing abilities
  - Thief: High mobility and speed
- **Turn-based combat system** with player and AI phases
- **Terrain variety** affecting movement and combat (plains, forests, mountains, forts)
- **Experience and leveling** with stat progression
- **Strategic AI** opponents that move tactically

### Technical Implementation
- Modern Kotlin codebase with clean architecture
- Comprehensive unit test coverage
- Modular design ready for Android UI
- Console demonstration of core mechanics

## Development

### Prerequisites
- Android Studio Arctic Fox or later
- JDK 17+
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
./gradlew assembleDebug
```

#### Run Console Demo
```bash
./gradlew :standalone:run
```

#### Run Tests
```bash
./gradlew test
./gradlew connectedAndroidTest
```

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
./gradlew lintDebug
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
- **Testing**: Unit tests, instrumented tests across multiple API levels
- **Security**: OWASP dependency check, secret scanning
- **Performance**: Build size analysis, performance tests

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
│   ├── src/test/java/           # Unit tests
│   └── src/androidTest/java/    # Instrumented tests
├── standalone/                   # Console demo module
├── config/                      # Quality tools configuration
│   ├── detekt/                  # Detekt configuration
│   └── owasp/                   # OWASP dependency check
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

## Future Development

The core tactical mechanics are complete. Future Android development would include:

- Native Android UI with touch controls
- Sprite-based 2D pixel art graphics
- Campaign mode with story progression
- Additional character classes and abilities
- Sound effects and music
- Advanced AI and difficulty modes

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Run quality checks (`./gradlew qualityCheck`)
4. Commit your changes (`git commit -m 'Add amazing feature'`)
5. Push to the branch (`git push origin feature/amazing-feature`)
6. Open a Pull Request

### Code Standards
- Follow Kotlin coding conventions
- Maintain test coverage above 80%
- All code must pass ktlint and detekt checks
- Include tests for new functionality
- Update documentation for public APIs

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Development Notes

This project demonstrates a complete tactical RPG engine suitable for mobile deployment. The clean separation between game logic and presentation layers makes it easy to add different UI implementations while maintaining the same core gameplay experience.

The CI/CD pipeline ensures code quality and enables reliable releases to the Play Store with automated testing and deployment.