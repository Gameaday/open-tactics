# Changelog

All notable changes to Open Tactics will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- **20-Chapter Campaign** across 4 acts (Defense, Counterattack, Invasion, Finale)
- **17 unique map layouts** including river crossings, fortress interiors, and dragon lairs
- **Difficulty modes** (Easy/Normal/Hard) with enemy stat and EXP scaling
- **Achievement system** with 10 milestones tracked in player profile
- **Battle quotes** for all named protagonists and boss characters
- **Support conversations** between 7 character pairs at ranks C, B, A
- **AI healing** — enemy healers now use healing staves on wounded allies
- **Critical hit system** with skill/luck-based critical rates
- **Healing system** with Heal and Mend staves, EXP for healing
- **Chapter replay** — completed chapters show replay option in chapter select
- **New character classes**: Pegasus Knight, Wyvern Rider
- **Named characters**: Sir Garrett, Lyanna, Aldric, Elara, Raven, Celeste
- **Boss characters**: Captain Voss, Sorceress Mira, Warlord Kael, Emperor Darius
- CI/CD pipeline with GitHub Actions
- Automated ktlint code formatting and linting
- Automatic version numbering based on git commits
- Multi-flavor build variants (dev/prod)
- ProGuard configuration for release builds
- Play Store deployment automation

### Fixed
- Resolved all 16 detekt static analysis issues (magic numbers, parameter lists, class size, line length)
- Extracted achievement threshold constants in AchievementRepository
- Suppressed EncryptedSharedPreferences deprecation warnings with migration plan
- Fixed serialization annotations in Weapon.kt (`@OptIn(ExperimentalSerializationApi::class)`)
- Added `@IgnoredOnParcel` annotation to Character.kt computed properties
- Fixed heal EXP condition (no longer awards EXP when healing already-full HP units)
- AI behavior fallback now correctly uses `aiType` field

### Changed
- Standalone module synced with app (healing, critical hits, 20 chapters, EXP scaling)
- Updated TECHNICAL_DEBT.md with current resolution status
- Updated README.md with accurate feature list and project status
- Enhanced build configuration with signing support
- Improved testing infrastructure

## [1.0.0] - 2024-09-26

### Added
- Complete Fire Emblem-style tactical RPG game engine
- Grid-based battlefield system with varied terrain
- Five character classes with unique abilities
- Turn-based combat with strategic positioning
- Enhanced Android MVP with professional graphics
- Complete save/load system with auto-save
- Player profile management with statistics
- Production-ready menu system
- Battle animations with screen effects
- Encrypted save data with Android security
- Professional UI/UX with Material Design

### Core Features
- **Tactical Combat**: Strategic grid-based battles
- **Character Classes**: Knight, Archer, Mage, Healer, Thief
- **Terrain System**: Plains, forests, mountains, forts with tactical bonuses
- **AI Opponents**: Smart enemy behavior with tactical movement
- **Progression**: Experience, leveling, and stat growth
- **Visual Polish**: Colored terrain tiles with character class icons

### Technical
- Modern Kotlin architecture with clean separation
- Comprehensive unit test coverage
- Both Android app and standalone console demo
- Modular design ready for expansion
- ViewBinding and Material Design components