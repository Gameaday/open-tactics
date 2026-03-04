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

*Last Updated: March 4, 2026*
