# UI Screenshots and Feature Demos

## New Features Visual Guide

### 1. Terrain Tooltip Feature

**When:** Clicking on empty tiles during unit selection
**What:** Shows terrain information in a dialog

```
┌─────────────────────────────────┐
│     Terrain Info                │
├─────────────────────────────────┤
│ Terrain: Mountain               │
│                                 │
│ Movement Cost: 3                │
│ Defense Bonus: +2               │
│ Avoidance Bonus: +20%           │
│                                 │
│ High ground offers excellent    │
│ defensive position but is hard  │
│ to traverse.                    │
│                                 │
│           [OK]                  │
└─────────────────────────────────┘
```

**Available Terrain Types:**
- **Plain**: 1 cost, +0 def, +0 avoid - "Open terrain with no special properties"
- **Forest**: 2 cost, +1 def, +10 avoid - "Dense forest provides cover and slows movement"
- **Mountain**: 3 cost, +2 def, +20 avoid - "High ground offers excellent defensive position"
- **Fort**: 1 cost, +3 def, +20 avoid - "Fortified position provides strong defensive bonuses"
- **Village**: 1 cost, +1 def, +10 avoid - "Village tiles offer moderate defensive bonus"
- **Water**: 999 cost, +0 def, +0 avoid - "Impassable for most units. Only flying units can cross"

---

### 2. In-Game Help Dialog

**When:** Long-press screen → Select "Help" from menu
**What:** Comprehensive game guide

```
┌─────────────────────────────────┐
│     Game Help                   │
├─────────────────────────────────┤
│ Welcome to Open Tactics!        │
│                                 │
│ BASIC CONTROLS:                 │
│ • Tap a unit to select it       │
│ • Tap Move to see movement range│
│ • Tap Attack to see attack range│
│ • Tap Wait to end unit's turn   │
│ • Tap End Turn to end your phase│
│                                 │
│ COMBAT:                         │
│ • Units attack based on weapon  │
│ • Weapon triangle affects damage│
│ • Terrain provides bonuses      │
│ • Speed enables double attacks  │
│                                 │
│ TERRAIN EFFECTS:                │
│ • Plains: No bonuses (1 cost)   │
│ • Forest: +1 Def, +10 Avoid (2) │
│ • Mountain: +2 Def, +20 Avoid(3)│
│ • Fort: +3 Def, +20 Avoid (1)   │
│ • Water: Impassable (flying)    │
│                                 │
│ UI FEATURES:                    │
│ • Toggle Ranges: Show enemies   │
│ • Undo: Undo last move          │
│ • Long-press menu: Save/Load    │
│ • Tap terrain: View details     │
│                                 │
│ TIPS:                           │
│ • Use terrain to your advantage │
│ • Keep units together           │
│ • Watch enemy ranges            │
│ • Save often!                   │
│                                 │
│           [OK]                  │
└─────────────────────────────────┘
```

---

### 3. EXP Gain Effect

**When:** After attacking/defeating an enemy
**What:** Toast notification showing EXP gained

```
Battle Result Toast:
┌─────────────────────────────────┐
│ Sir Garrett defeated Dark Knight│
│ (15 damage)                     │
└─────────────────────────────────┘

Then 500ms later:
┌─────────────────────────────────┐
│ Sir Garrett gained 35 EXP!      │
│ (85/100)                        │
└─────────────────────────────────┘
```

**EXP Awards:**
- Hitting enemy: **10 EXP**
- Defeating enemy: **20-50 EXP** (scaled by level difference)
  - Formula: `30 + (enemyLevel - playerLevel) * 5`
  - Clamped between 20 and 50

---

### 4. Level Up Display

**When:** Character gains enough EXP to level up
**What:** Celebration dialog with stat display

```
┌─────────────────────────────────┐
│    ⭐ Level Up! ⭐              │
├─────────────────────────────────┤
│ Sir Garrett reached Level 2!    │
│                                 │
│ HP: 28                          │
│ MP: 0                           │
│ ATK: 9                          │
│ DEF: 8                          │
│ SPD: 6                          │
│ SKL: 7                          │
│ LCK: 5                          │
│                                 │
│           [OK]                  │
└─────────────────────────────────┘
```

**Level Up Features:**
- Full HP/MP restoration
- Stat increases based on class growth rates
- Max level: 20
- EXP requirement: level × 100

---

### 5. Game Menu (Updated)

**When:** Long-press anywhere on game screen
**What:** Game menu with new "Help" option

```
┌─────────────────────────────────┐
│     Game Menu                   │
├─────────────────────────────────┤
│                                 │
│   Save Game                     │
│                                 │
│   Load Game                     │
│                                 │
│   Settings                      │
│                                 │
│   Help                    ← NEW │
│                                 │
│   Quit to Menu                  │
│                                 │
└─────────────────────────────────┘
```

---

## Existing Features (Verified Working)

### Enemy Range Toggle

**When:** Click "Ranges" button (top-right corner)
**What:** Shows/hides red overlay of all enemy attack ranges

```
Game Board View:
┌────────────────────────────────────┐
│ Ch1: First Battle  [Ranges] ← BTN │
│ Defeat all enemies                 │
│ Turn: 1                            │
│                                    │
│    [Game Board with red overlay]   │
│    Red tiles = Enemy can attack    │
│                                    │
├────────────────────────────────────┤
│ [Move][Undo][Attack][Wait][Inv]   │
│ [End Turn]                         │
└────────────────────────────────────┘
```

---

## Usage Flow Example

1. **Player selects a unit** → Character info appears
2. **Player clicks Move** → Blue tiles show movement range
3. **Player clicks empty tile** → **Terrain tooltip shows** (NEW)
4. **Player moves unit** → Undo button appears
5. **Player clicks Attack** → Red tiles show attack range
6. **Player selects enemy** → Battle forecast dialog
7. **Player confirms attack** → Battle animation plays
8. **Battle completes** → Damage shown in toast
9. **500ms later** → **EXP gain toast** (NEW)
10. **If level up** → **⭐ Level Up dialog** (NEW)
11. **Player confused?** → Long-press → **Help dialog** (NEW)

---

## Testing the Features

To test these features in the app:

1. **Build and install**: `./gradlew assembleDevDebug`
2. **Start a new game** → Select Chapter 1
3. **Click empty tiles** → See terrain tooltips
4. **Long-press screen** → Select "Help" → Read game guide
5. **Battle enemies** → Watch for EXP gain notifications
6. **Keep battling** → Witness level up celebration!

---

## Technical Implementation

All features implemented in `GameActivity.kt`:

- `showTerrainTooltip(position: Position)` - Lines ~1320-1350
- `showHelpDialog()` - Lines ~1352-1400
- `showExpGainEffect(character, expGained)` - Lines ~1135-1145
- `showLevelUpEffect(character, oldLevel, newLevel)` - Lines ~1147-1170

Tests in:
- `app/src/test/java/.../model/TerrainInfoTest.kt`
- `app/src/test/java/.../model/ExpAndLevelUpTest.kt`
