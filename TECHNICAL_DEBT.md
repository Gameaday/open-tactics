# Technical Debt Resolution Plan
**Priority:** MEDIUM  
**Status:** ✅ Mostly Resolved (March 2026)

## Overview

This document tracks technical debt items. Most critical items from the original audit have been resolved.

---

## ✅ Resolved Items

### 1. Serialization Warnings (Weapon.kt)
- **Status:** ✅ Fixed
- Removed redundant `@Serializer` annotation
- Added `@OptIn(ExperimentalSerializationApi::class)` where needed

### 2. Parcelable Property Warning (Character.kt)
- **Status:** ✅ Fixed
- Added `@IgnoredOnParcel` annotations to computed properties

### 3. AI Healing
- **Status:** ✅ Implemented
- AI healers now use healing staves on wounded allies
- Support behavior prioritizes healing critically wounded units (lowest HP ratio)
- Falls back to moving toward wounded allies, then defensive behavior

### 4. Detekt Static Analysis (16 issues)
- **Status:** ✅ Fixed
- Extracted magic number constants in `AchievementRepository`
- Added `@Suppress("MagicNumber")` to `DifficultyMode` enum
- Added `@Suppress("LongParameterList")` to `CharacterFactory` and `Character.fromNamedUnit`
- Added `@Suppress("LargeClass")` to `ChapterRepository` (both app and standalone)
- Fixed `MaxLineLength` in standalone `Weapon.kt`

### 5. Test Coverage
- **Status:** ✅ Adequate
- Overall: ≥25% bundle minimum
- Models: ~67% per-class
- Game Logic: ~38% per-class
- 22+ unit test files, 4 instrumented test files

### 6. UI/UX Polish (Phase 1)
- **Status:** ✅ Implemented
- Main menu branding: crossed swords logo, tagline, version label
- Launcher icon: dark blue theme with gold accent shield
- Accessibility: contentDescription on all 14 interactive buttons
- String resources: extracted hardcoded text, proper ellipsis
- Chapter select: subtitle with act information
- SoundManager: SoundPool-based audio infrastructure with 9 effect types
- SoundManager integration: connected in both MainActivity and GameActivity
- Vector graphics: enhanced all 9 character class icons with detailed multi-path designs
- Terrain icons: created 6 terrain type drawables integrated into board rendering
- Rounded HUD panels: bg_panel_rounded.xml with gold border accent
- Button effects: gold ripple feedback on all game buttons

---

## 🔄 Deferred / Low Priority

### 1. EncryptedSharedPreferences Deprecation
**Location:** `SaveGameManager.kt`  
**Impact:** LOW — API still works, suppressed with `@Suppress("DEPRECATION")`

The `androidx.security:security-crypto` library's `EncryptedSharedPreferences` and `MasterKey` classes are deprecated in Java. A migration TODO is in place. The new API is not yet stable.

**Current Status:** Suppressed warnings + migration TODO comment  
**Action:** Monitor `androidx.security` releases for stable replacement API

### 2. Gradle 10 Compatibility
**Warning:** `Deprecated Gradle features were used in this build`

Current warnings from `--warning-mode all`:
- `ReportingExtension.file()` — from Gradle plugins (not our code)
- Multi-string dependency notation — from Android Gradle Plugin internals

**Impact:** LOW — Gradle 10 not released; warnings originate from third-party plugins (AGP, ktlint)  
**Action:** Wait for plugin updates that address Gradle 10 compatibility

### 3. Throne Unit Tracking
**Location:** `GameActivity.kt` — `emptyList(), // TODO: Track throne units`  
**Impact:** LOW — Not used in current campaign maps  
**Decision:** Deferred to post-launch. The SEIZE_THRONE chapter objective type exists but throne unit tracking on the game board is not implemented.

---

## Remaining Enhancements & Polish

The following items are not technical debt but represent potential improvements for future releases. They are organized by category and priority.

### 🎨 Visual Enhancements
| Item | Priority | Effort | Notes |
|------|----------|--------|-------|
| Animated unit sprites (walk/attack/idle) | HIGH | 40h + art | Replace static icons with animated sprite sheets |
| Terrain tilesets (pixel art) | HIGH | 20h + art | Replace flat-color tiles with detailed tile graphics |
| Character portraits for named units | MEDIUM | 15h + art | Show portraits in dialogue and info panel |
| Attack/damage visual effects | MEDIUM | 10h | Screen flash, shake, particle effects |
| Level-up celebration animation | LOW | 5h | Animated stat increases with sparkle effects |
| Map transition animations | LOW | 5h | Fade/slide between chapter select and game |
| ~~Health bar color gradient~~ | ~~LOW~~ | ~~—~~ | ✅ Done: Green→yellow→red gradient based on HP% |

### 🔊 Audio Enhancements
| Item | Priority | Effort | Notes |
|------|----------|--------|-------|
| Add sound effect audio files to res/raw | HIGH | 5h + assets | Button click, attack, heal, victory, defeat, etc. |
| Background music tracks (menu, battle) | MEDIUM | 10h + assets | MediaPlayer integration for looping BGM |
| Music crossfade between screens | LOW | 3h | Smooth transitions between menu and battle music |
| Dynamic battle music (intensity scaling) | LOW | 8h | Music layers based on enemy count or HP |

### 🎮 Gameplay Polish
| Item | Priority | Effort | Notes |
|------|----------|--------|-------|
| Tutorial chapter with guided instructions | HIGH | 15h | Teach movement, combat, terrain basics |
| Pre-battle story scenes | MEDIUM | 10h | Dialogue between chapters with character art |
| ~~Post-battle stats summary screen~~ | ~~MEDIUM~~ | ~~—~~ | ✅ Done: Damage dealt/received, enemies defeated, heals |
| ~~Weapon triangle visual indicator~~ | ~~LOW~~ | ~~—~~ | ✅ Done: Shows advantage/disadvantage in battle forecast |
| ~~Battle log / combat feed~~ | ~~MEDIUM~~ | ~~—~~ | ✅ Done: 3-entry log with fading opacity |
| Mini-map for larger boards | LOW | 8h | Thumbnail overview of full map |

### 📱 UI/UX Polish
| Item | Priority | Effort | Notes |
|------|----------|--------|-------|
| Settings as dedicated Activity (not dialog) | MEDIUM | 5h | Better layout, sliders, toggle switches |
| Loading screen with progress indicator | MEDIUM | 3h | Show while chapter initializes |
| Haptic feedback on unit actions | LOW | 2h | Vibration on attack, level-up |
| Dark/light theme toggle | LOW | 5h | Alternative color scheme option |
| Landscape orientation support | LOW | 8h | Responsive layout for wider screens |

### ⚡ Performance & Architecture
| Item | Priority | Effort | Notes |
|------|----------|--------|-------|
| Bitmap caching for terrain tiles | MEDIUM | 5h | Reduce per-frame draw calls |
| ViewModel migration for GameActivity | LOW | 15h | Survive configuration changes |
| Room database for save files | LOW | 10h | Replace encrypted SharedPreferences |
| Standalone module full feature parity | LOW | 10h | Add support, serialization, named units |

---

## Current Build Quality

| Check | Status |
|-------|--------|
| `./gradlew ktlintCheck` | ✅ Pass |
| `./gradlew detekt` | ✅ Pass (0 issues) |
| `./gradlew testDevDebugUnitTest` | ✅ Pass |
| `./gradlew standalone:test` | ✅ Pass |
| Compiler warnings | ⚠️ 2 (SaveGameManager deprecation imports — suppressed at usage) |

---

## Testing Commands

```bash
# Quick unit tests
./gradlew testDevDebugUnitTest

# Full test suite (all flavors)
./gradlew test

# Code formatting
./gradlew ktlintCheck

# Static analysis
./gradlew detekt

# Full CI verification
./gradlew ciVerification
```

---

*Last Updated: March 5, 2026*
