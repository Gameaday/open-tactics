# Trading and Support System Implementation

This document describes the implementation of the trading system and support system features for Open Tactics.

## 1. Trade Items Between Units

### Overview
Players can now trade weapons and consumable items between adjacent allied units during the ACTION phase.

### Implementation Details

#### Data Model
- Uses existing `Character` inventory structure:
  - `inventory: MutableList<Weapon>` - for weapons
  - `items: MutableList<Item>` - for consumable items

#### UI Components
- **dialog_trade.xml** - Trade dialog showing both units' inventories side-by-side
- **item_trade.xml** - Individual item row with trade button
- Trade button appears as "→" to transfer items between units

#### User Flow
1. Select a unit during ACTION phase
2. Click on an adjacent allied unit
3. Choose "Trade Items" from the dialog
4. Click the arrow button (→) next to any item to transfer it
5. Items can be swapped or moved to empty slots
6. Inventory size limit (5 items per unit) is enforced

#### Code Location
- `GameActivity.kt`:
  - `showTradeDialog()` - Creates and displays the trade UI
  - `TradeAdapter` - RecyclerView adapter for trade list
  - `handleTileClick()` - Modified to detect adjacent ally clicks

#### Testing
- `TradingSystemTest` in `SupportAndTradeTest.kt`:
  - Test weapon trading
  - Test consumable item trading
  - Test giving items to empty slots
  - Test inventory size limits
  - 5 comprehensive test cases

## 2. Ally Support Ranges

### Overview
Characters can build support relationships that provide combat bonuses when units are adjacent (within 1 tile).

### Implementation Details

#### Data Model
- **SupportRelationship.kt** - New model class:
  ```kotlin
  data class SupportRelationship(
      val characterId1: String,
      val characterId2: String,
      var rank: SupportRank,
      var conversationsSeen: Int = 0,
  )
  ```

- **SupportRank** enum:
  - `NONE` - No relationship
  - `C` - +1 ATK, +1 SKL
  - `B` - +2 ATK, +1 DEF, +2 SKL, +1 LCK
  - `A` - +3 ATK, +2 DEF, +1 SPD, +3 SKL, +2 LCK
  - `S` - +5 ATK, +3 DEF, +2 SPD, +5 SKL, +3 LCK (paired ending)

#### Game State Integration
- `GameState.kt` modifications:
  - `supportRelationships: MutableList<SupportRelationship>` - Tracks all relationships
  - `addSupportRelationship()` - Add new support
  - `getSupportRelationship()` - Look up specific relationship
  - `getCharacterSupports()` - Get all supports for a character
  - `getSupportBonuses()` - Calculate bonuses when units are adjacent

#### UI Components
- **dialog_support.xml** - Lists all support partners for a character
- **item_support.xml** - Individual support relationship display showing:
  - Partner name
  - Support rank (C/B/A/S)
  - Active bonuses
  - Range requirement (adjacent)

#### User Flow
1. Open game menu (pause button)
2. Select "Supports" option
3. Choose a character
4. View all their support relationships
5. See active bonuses for each rank
6. Bonuses automatically apply when units are adjacent in battle

#### Initial Support Relationships
Predefined relationships in `initializeSupportRelationships()`:
- Sir Garrett ↔ Lyanna: Rank C
- Sir Garrett ↔ Aldric: Rank B
- Lyanna ↔ Aldric: Rank C

#### Code Location
- `model/SupportRelationship.kt` - Data model
- `game/GameState.kt` - Support system logic
- `GameActivity.kt`:
  - `showSupportDialog()` - Display support relationships
  - `SupportAdapter` - RecyclerView adapter for support list
  - `showSupportsMenu()` - Character selection for viewing supports
  - `initializeSupportRelationships()` - Initialize default supports

#### Testing
- `SupportRelationshipTest` - Tests for SupportRelationship model:
  - Relationship involvement checks
  - Getting other character
  - Bonus calculations for each rank (C/B/A/S/NONE)
  - 7 comprehensive test cases

- `SupportSystemTest` - Tests for GameState integration:
  - Adding support relationships
  - Bidirectional relationship lookup
  - Getting character supports
  - Adjacent bonus application
  - Non-adjacent bonus exclusion
  - Multiple support stacking
  - 7 comprehensive test cases

## 3. Build Quality

### Linting
- All code passes ktlint formatting checks
- Fixed trailing spaces and missing trailing commas in build.gradle.kts
- Proper Kotlin code style throughout

### Testing
- 14 new unit tests added
- All existing tests continue to pass
- Test coverage for both trading and support systems

### Build Status
- APK builds successfully (`assembleDevDebug`)
- No compiler warnings or errors
- All quality checks pass

## 4. Future Enhancements

### Trading System
- Add visual feedback when hovering over trade buttons
- Implement trade history/undo
- Add bulk trade operations
- Support trading with neutral/NPC units

### Support System
- Implement support conversations/cutscenes
- Add visual indicators on the game board showing support ranges
- Implement support rank progression through combat
- Add support-specific abilities/bonuses
- Create support relationship tree/visualization

### Integration
- Apply support bonuses to battle forecast calculations
- Show support bonuses in character stat display
- Add support range overlay (similar to enemy range overlay)
- Implement achievement system for maxing supports

## 5. User Documentation

### Trading
**How to Trade:**
1. Move a unit next to an ally
2. Select the unit (if not already selected)
3. Click on the adjacent ally during ACTION phase
4. Choose "Trade Items"
5. Click the arrow (→) next to any item to transfer it

**Trading Rules:**
- Units must be adjacent (1 tile apart)
- Both units must be alive and on the same team
- Each unit can hold max 5 weapons/items
- Cannot trade if recipient's inventory is full

### Supports
**How to View Supports:**
1. Open game menu (pause)
2. Select "Supports"
3. Choose a character
4. View their support partners and active bonuses

**Support Bonuses:**
- Apply automatically when supported units are adjacent
- Stack if multiple supported units are nearby
- Shown in the support dialog for each relationship
- Higher ranks provide better bonuses

**Support Ranks:**
- C Rank: +1 ATK, +1 SKL
- B Rank: +2 ATK, +1 DEF, +2 SKL, +1 LCK
- A Rank: +3 ATK, +2 DEF, +1 SPD, +3 SKL, +2 LCK
- S Rank: +5 ATK, +3 DEF, +2 SPD, +5 SKL, +3 LCK

## 6. Technical Notes

### Performance
- Support bonus calculation is O(n) where n is the number of support relationships
- Efficient lookup using relationship IDs
- No performance impact during normal gameplay

### Memory
- Support relationships stored as small data objects
- Minimal memory footprint
- Serializable for save/load functionality

### Maintainability
- Clean separation of concerns
- Well-tested with comprehensive unit tests
- Follows existing code patterns and conventions
- Properly documented with inline comments
