# Technical Debt Resolution Plan
**Priority:** HIGH  
**Timeline:** 1-2 weeks  
**Status:** 🔄 In Progress

## Overview

This document outlines the immediate technical debt items that need to be resolved before proceeding with major feature development. These items are blockers or will become blockers soon.

---

## 1. Deprecation Warnings

### Issue: EncryptedSharedPreferences API Deprecated
**Location:** `app/src/main/java/com/gameaday/opentactics/data/SaveGameManager.kt`  
**Lines:** 5-6, 27-38

**Current Code:**
```kotlin
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

// In createEncryptedPrefs():
val masterKey = MasterKey.Builder(context)
    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
    .build()

sharedPreferences = EncryptedSharedPreferences.create(
    context,
    PREFS_FILENAME,
    masterKey,
    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
) as EncryptedSharedPreferences
```

**Warnings:**
```
w: 'class EncryptedSharedPreferences : Any, SharedPreferences' is deprecated. Deprecated in Java.
w: 'class MasterKey : Any' is deprecated. Deprecated in Java.
w: 'enum class PrefKeyEncryptionScheme : Enum<...>' is deprecated. Deprecated in Java.
w: 'enum class PrefValueEncryptionScheme : Enum<...>' is deprecated. Deprecated in Java.
```

**Impact:** LOW - API still works but will be removed in future Android versions

**Resolution:**
The androidx.security:security-crypto library is being migrated to new APIs. Current approach:
1. **Short term:** Suppress deprecation warnings with @Suppress annotations
2. **Long term:** Migrate to new API when it's stable (currently still in development)

**Action Items:**
- [ ] Add `@Suppress("DEPRECATION")` to affected methods
- [ ] Add TODO comment to track migration path
- [ ] Monitor androidx.security updates for new API stability
- [ ] Plan migration when new API reaches beta/stable

**Estimated Time:** 30 minutes

---

### Issue: Serialization Annotation Warnings
**Location:** `app/src/main/java/com/gameaday/opentactics/model/Weapon.kt`  
**Line:** 19

**Current Code:**
```kotlin
@Serializer(forClass = IntRange::class)
object IntRangeSerializer : KSerializer<IntRange> {
    // ... implementation
}
```

**Warnings:**
```
w: @Serializer annotation has no effect on class 'object IntRangeSerializer', 
   because all members of KSerializer are already overridden
w: This declaration needs opt-in. Its usage should be marked with 
   '@kotlinx.serialization.ExperimentalSerializationApi' or 
   '@OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)'
```

**Impact:** LOW - Code works correctly, just needs cleanup

**Resolution:**
1. Remove unnecessary `@Serializer` annotation (has no effect)
2. Add `@OptIn(ExperimentalSerializationApi::class)` for experimental features

**Action Items:**
- [ ] Remove `@Serializer(forClass = IntRange::class)` line
- [ ] Add `@OptIn(ExperimentalSerializationApi::class)` to class if needed
- [ ] Verify serialization still works in tests

**Estimated Time:** 15 minutes

---

### Issue: Parcelable Property Warning
**Location:** `app/src/main/java/com/gameaday/opentactics/model/Character.kt`  
**Line:** 249

**Warning:**
```
w: Property would not be serialized into a 'Parcel'. 
   Add '@IgnoredOnParcel' annotation to remove the warning.
```

**Impact:** LOW - Property may not serialize correctly across process boundaries

**Resolution:**
Add `@IgnoredOnParcel` annotation to properties that shouldn't be parceled (likely computed properties)

**Action Items:**
- [ ] Identify which property at line 249 is causing the warning
- [ ] Determine if property should be parceled or ignored
- [ ] Add appropriate annotation (`@IgnoredOnParcel` or make it parcelable)
- [ ] Test character serialization works correctly

**Estimated Time:** 20 minutes

---

## 2. TODOs in Code

### Issue: AI Healing Not Implemented
**Location:** `app/src/main/java/com/gameaday/opentactics/game/GameState.kt`  
**Line:** 383  
**Also:** `standalone/src/main/kotlin/com/gameaday/opentactics/game/GameState.kt`

**Current Code:**
```kotlin
// TODO: Implement healing when staff weapons are added
```

**Context:**
This TODO is outdated - healing staves ARE implemented (see HEALING_SYSTEM.md). The TODO is for AI enemy healers using healing staves, not player healing.

**Impact:** MEDIUM - Enemy healers currently don't use their staves, making them less threatening

**Resolution:**
Implement AI logic to:
1. Detect when enemy unit has healing staff equipped
2. Find wounded allies within range
3. Prioritize healing critically wounded units (HP < 50%)
4. Use heal action instead of moving toward player units

**Action Items:**
- [ ] Update TODO comment to be more specific: "Enemy AI doesn't use healing staves yet"
- [ ] Create `GameState.aiShouldHeal(character)` method
- [ ] Implement `GameState.aiSelectHealTarget(healer)` method
- [ ] Add healing action to AI turn logic in `processEnemyTurn()`
- [ ] Add unit tests for AI healing behavior
- [ ] Test in-game with enemy healer units

**Estimated Time:** 2-3 hours

**Design:**
```kotlin
private fun aiShouldHeal(character: Character): Boolean {
    // Check if character has healing staff equipped
    val weapon = character.equippedWeapon ?: return false
    if (!weapon.tags.contains("HEAL")) return false
    
    // Check if any wounded allies in range
    return calculateHealTargets(character).isNotEmpty()
}

private fun aiSelectHealTarget(healer: Character): Character? {
    val targets = calculateHealTargets(healer)
    if (targets.isEmpty()) return null
    
    // Prioritize most wounded ally
    return targets.minByOrNull { it.currentHP.toFloat() / it.stats.hp }
}
```

---

### Issue: Throne Units Not Tracked
**Location:** `app/src/main/java/com/gameaday/opentactics/GameActivity.kt`  
**Context:** Comment in chapter creation

**Current Code:**
```kotlin
emptyList(), // TODO: Track throne units
```

**Impact:** LOW - Throne mechanic not used in current maps

**Resolution:**
This is a placeholder for future feature. Options:
1. Remove TODO if throne mechanic won't be implemented soon
2. Keep TODO but mark as low priority / future enhancement
3. Implement if throne mechanic is planned for campaign

**Action Items:**
- [ ] Decide if throne mechanic is in scope for v1.0
- [ ] If yes: Design throne mechanic (healing, defense bonus, victory condition)
- [ ] If no: Update TODO to say "Future: Throne unit mechanic"
- [ ] Document decision in STRATEGIC_PLAN.md

**Estimated Time:** 5 minutes (decision) or 4-6 hours (implementation)

**Recommendation:** Mark as future feature, not needed for launch

---

## 3. Build System Warnings

### Issue: Gradle 10 Compatibility
**Warning:**
```
Deprecated Gradle features were used in this build, 
making it incompatible with Gradle 10.
```

**Impact:** LOW - Gradle 10 not released yet, current build works fine

**Resolution:**
Run with `--warning-mode all` to identify specific deprecations, then fix them.

**Action Items:**
- [ ] Run `./gradlew build --warning-mode all` to see detailed warnings
- [ ] Research each deprecation and find modern alternative
- [ ] Update build scripts to use new APIs
- [ ] Verify build still works with changes
- [ ] Test all build variants (dev/prod debug/release)

**Estimated Time:** 1-2 hours

**Note:** Not urgent - can be deferred to Phase 2 if needed

---

## 4. Test Coverage Gaps

### Current Coverage
- Models: 67% ✅ (exceeds 38% per-class minimum)
- Game Logic: 38% ✅ (meets minimum)
- Overall: 26% ✅ (exceeds 25% bundle minimum)

### Gaps (Not Blockers)
- UI code (Activities) not well tested
- Factory classes have basic tests but could be more comprehensive
- Edge cases in combat calculations

**Resolution:**
Coverage is adequate for current phase. Improve incrementally:
1. Add tests as bugs are found
2. Test new features as they're added
3. Aim for 30% overall coverage by launch

**Action Items:**
- [ ] Add test for AI healing once implemented
- [ ] Add test for throne mechanic if implemented
- [ ] Monitor coverage in CI - fail if drops below 25%

**Estimated Time:** Ongoing (30 min per new feature)

---

## 5. Dependency Updates

### Current Status
All dependencies up-to-date as of Android Gradle 8.13.0, Kotlin 2.1.0

### Weekly Dependency Check
GitHub Actions workflow runs weekly to check for updates (see `.github/workflows/dependencies.yml`)

**Action Items:**
- [ ] Review last dependency check run
- [ ] Update any dependencies flagged as outdated
- [ ] Test after dependency updates
- [ ] Monitor OWASP dependency check for vulnerabilities

**Estimated Time:** 30 minutes per week

---

## Priority Order

### Week 1 Focus
1. ✅ Fix serialization warnings (30 min)
2. ✅ Add deprecation suppressions (30 min)  
3. ✅ Fix Parcelable warning (20 min)
4. ✅ Implement AI healing (2-3 hours)
5. ✅ Test all changes (1 hour)

**Total: ~5 hours of work**

### Week 2 Focus
1. ✅ Fix Gradle 10 warnings (1-2 hours)
2. ✅ Review and update dependencies (30 min)
3. ✅ Decide on throne mechanic (document decision)
4. ✅ Update documentation (30 min)
5. ✅ Verify all tests pass (30 min)

**Total: ~3-4 hours of work**

---

## Success Criteria

Technical debt is resolved when:
- ✅ Zero compiler warnings (errors or warnings)
- ✅ All deprecations either fixed or suppressed with migration plan
- ✅ AI healing implemented and tested
- ✅ Build system compatible with future Gradle versions
- ✅ All tests passing
- ✅ Code quality checks passing (ktlint, detekt)

---

## Testing Plan

After each fix:
1. Run `./gradlew ktlintCheck` - verify formatting
2. Run `./gradlew detekt` - verify static analysis
3. Run `./gradlew test` - verify all unit tests pass
4. Run `./gradlew assembleDevDebug` - verify builds successfully
5. Manual test affected features in app

After all fixes:
1. Run `./gradlew ciVerification` - full CI check
2. Manual test campaign on device
3. Verify AI healing works in Chapter 2+
4. Check no performance regressions

---

## Next Steps After Resolution

Once technical debt is cleared:
1. Begin Phase 2: Visual Transformation planning
2. Research art asset options (commission vs create)
3. Design tutorial chapter
4. Set up Phase 2 project board
5. Start sprite requirements documentation

---

## Notes

- All fixes should maintain backward compatibility
- No breaking changes to save game format
- Document any API changes in CHANGELOG.md
- Update relevant documentation files after changes

---

*Created: February 9, 2026*  
*Last Updated: February 9, 2026*  
*Status: Ready to begin implementation*
