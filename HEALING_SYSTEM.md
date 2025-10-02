# Healing System Implementation

## Overview

Added support for healing staves with EXP gain, addressing feedback that utility actions (not just attacks) should award experience.

## Features Implemented

### 1. Healing Actions

**Method**: `GameState.performHeal(healer, target)`

Healers can now use healing staves on wounded allies:
- **Heal staff**: Restores 10 HP
- **Mend staff**: Restores 20 HP
- Uses staff durability (consumes one use)
- Awards **12 EXP** per successful heal

### 2. Target Selection

**Method**: `GameState.calculateHealTargets(character)`

Finds valid heal targets:
- Must be same team (allies)
- Must be alive and wounded (currentHP < maxHP)
- Must be within staff range
- Returns list of targetable characters

### 3. UI Integration

**In GameActivity:**

1. **Auto-detection**: When healer has staff equipped and clicks "Attack" button:
   - System automatically detects healing staff
   - Shows green highlights on wounded allies
   - No separate "Heal" button needed

2. **Confirmation Dialog**: `showHealConfirmation(healer, target)`
   ```
   Use Staff
   
   Lyanna will heal Sir Garrett
   
   Staff: Heal
   Heal Amount: 10 HP
   Current HP: 15/28
   After: 25/28
   
   [Heal] [Cancel]
   ```

3. **Result Display**: `showSupportResult(result)`
   - Toast message: "Lyanna healed Sir Garrett for 10 HP!"
   - EXP gain toast: "Lyanna gained 12 EXP! (45/100)"
   - Level up dialog if threshold reached

### 4. EXP System Fixes

**Bug Fixed**: Duplicate EXP awarding
- Previously: EXP awarded both in `GameState.performAttack()` AND `GameActivity.showBattleResult()`
- Now: EXP only awarded once in GameState methods
- `BattleResult` and `SupportResult` now track EXP and level changes

**EXP Awards:**
- Attack hit: 10 EXP
- Defeat enemy: 25 × enemy level (min 20, scaled by level difference in UI display)
- Heal ally: **12 EXP** (new)

## Code Changes

### GameState.kt

1. **New Constants**:
   ```kotlin
   private const val EXPERIENCE_PER_HEAL = 12
   ```

2. **New Data Class**:
   ```kotlin
   data class SupportResult(
       val user: Character,
       val target: Character,
       val healAmount: Int,
       val expGained: Int = 0,
       val previousLevel: Int = 0,
   )
   ```

3. **Updated BattleResult**:
   ```kotlin
   data class BattleResult(
       val attacker: Character,
       val target: Character,
       val damage: Int,
       val targetDefeated: Boolean,
       val wasCritical: Boolean = false,
       val expGained: Int = 0,      // Added
       val previousLevel: Int = 0,  // Added
   )
   ```

4. **New Methods**:
   - `calculateHealTargets(character: Character): List<Character>`
   - `performHeal(healer: Character, target: Character): SupportResult`

### GameActivity.kt

1. **Updated Methods**:
   - `handleAttackAction()` - Now detects healing staves and shows heal targets
   - `handleTileClick()` - ACTION phase now handles both attack and heal target clicks
   - `showBattleResult()` - Removed duplicate EXP awarding, now uses EXP from result

2. **New Methods**:
   - `showHealConfirmation(healer, target)` - Dialog to confirm healing action
   - `showSupportResult(result)` - Display healing outcome with EXP gain

## Usage Example

### As Player:

1. Select healer unit (e.g., Lyanna with Heal staff equipped)
2. Click "Move" if needed to get in range
3. Click "Attack" button
4. Green highlights appear on wounded allies
5. Click wounded ally to see confirmation dialog
6. Click "Heal" to execute
7. See healing message and EXP gain

### Level Up Flow:

```
1. Healer at Level 1 with 88 EXP
2. Heals ally → gains 12 EXP
3. Toast: "Lyanna gained 12 EXP! (100/100)"
4. Level up dialog appears: "⭐ Level Up! ⭐"
5. Shows new stats for Level 2
6. Healer fully restored to max HP/MP
```

## Future Extensibility

The `SupportResult` pattern can be extended for:
- **Buff staves**: Award EXP for buffing allies
- **Rescue staves**: Award EXP for rescuing units
- **Warp staves**: Award EXP for teleporting allies
- **Barrier staves**: Award EXP for providing shields

Example:
```kotlin
fun performBuff(caster: Character, target: Character): SupportResult {
    val previousLevel = caster.level
    // Apply buff logic
    caster.gainExperience(EXPERIENCE_PER_BUFF)
    return SupportResult(...)
}
```

## Testing

All existing tests pass. The healing system:
- ✅ Compiles without errors
- ✅ Passes all unit tests
- ✅ Passes ktlint formatting
- ✅ Integrates with existing EXP/level up system
- ✅ Works with existing weapon durability system

## Design Decisions

1. **Why 12 EXP for healing?**
   - Slightly more than hitting (10 EXP) to encourage support play
   - Less than defeating enemies (20-50 EXP) to keep combat primary
   - Matches Fire Emblem's pattern of rewarding utility actions

2. **Why only heal wounded allies?**
   - Prevents EXP farming by healing already-full units
   - Encourages tactical use of healing resources
   - Matches behavior from Fire Emblem games

3. **Why no separate "Heal" button?**
   - Simplifies UI - no need for new button
   - Context-aware: "Attack" button automatically becomes "Heal/Support" button
   - Reduces cognitive load on players

## Known Limitations

1. **No AI healing**: Enemy healers don't use staves yet (TODO in GameState.kt line 383)
2. **Limited staves**: Only Heal and Mend currently available
3. **No range-2 staves**: All current staves are range 1
4. **No staff rank requirements**: Not checking WeaponRank yet

These can be addressed in future PRs.
