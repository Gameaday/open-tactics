# Named Units System Implementation

## Overview

This implementation adds support for named units with unique characteristics and progression to the Open Tactics game. Named units can have custom growth rates that differ from their character class defaults, allowing for unique character progression similar to Fire Emblem's distinctive characters.

## Key Features

### 1. Named Unit Data Model (`NamedUnit.kt`)

The `NamedUnit` data class defines unique characters with:
- **id**: Unique identifier for the unit
- **name**: Display name
- **characterClass**: Base character class (Knight, Archer, etc.)
- **customGrowthRates**: Optional custom growth rates (if null, uses class defaults)
- **description**: Flavor text

### 2. Repository System

#### NamedUnitRepository
- Stores protagonist/player character definitions
- Includes main characters: Sir Garrett (Knight), Lyanna (Archer), Aldric (Mage)
- Includes recruitable units: Elara (Healer), Raven (Thief), Celeste (Pegasus Knight)

#### EnemyRepository
- Stores enemy and boss unit definitions
- Includes generic enemies: Bandits, Brigands, Rogues, Dark Mages
- Includes boss units with enhanced growth rates: Bandit Leader, Dark Knight Commander, General Ironhold

### 3. Character System Updates

#### Custom Growth Rates
- `Character` class now supports `customGrowthRates` field
- Level-up system uses custom rates if available, otherwise falls back to class defaults
- Ensures unique progression for named characters

#### Factory Method
```kotlin
Character.fromNamedUnit(
    namedUnit: NamedUnit,
    team: Team,
    position: Position,
    targetLevel: Int = 1,
    isBoss: Boolean = false,
    aiType: AIBehavior = AIBehavior.AGGRESSIVE
): Character
```

This method creates a character at any level by:
1. Starting at level 1 with base stats
2. Simulating level-ups using the character's growth rates
3. Accumulating stat bonuses based on probabilistic rolls

### 4. Enemy Spawning System

#### EnemyUnitSpawn Updates
- Added `namedUnitId` field to reference named units
- Added `toCharacter()` method that:
  - Looks up the named unit if `namedUnitId` is provided
  - Uses custom growth rates for the named unit
  - Falls back to generic character creation if no named unit is specified

### 5. Integration

#### GameActivity
- Player characters now created from `NamedUnitRepository`
- Enemy units created using `EnemyUnitSpawn.toCharacter()`
- Reinforcement spawning updated to support named units

#### ChapterRepository
- Chapter definitions updated to reference named enemies via `namedUnitId`
- Boss units linked to named boss definitions with enhanced stats

## Usage Examples

### Creating a Named Protagonist

```kotlin
val namedUnit = NamedUnitRepository.getProtagonist("player_knight")
val character = Character.fromNamedUnit(
    namedUnit = namedUnit!!,
    team = Team.PLAYER,
    position = Position(1, 6),
    targetLevel = 5  // Starts at level 5 with appropriate stats
)
```

### Spawning an Enemy at a Specific Level

```kotlin
val enemySpawn = EnemyUnitSpawn(
    id = "boss_1",
    name = "Bandit Leader",
    characterClass = CharacterClass.KNIGHT,
    level = 5,
    position = Position(10, 1),
    equipment = listOf("steel_sword"),
    namedUnitId = "boss_bandit_leader"  // Uses custom boss growth rates
)
val enemy = enemySpawn.toCharacter()
```

### Adding a New Named Character

To add a new named unit:

1. Add to appropriate repository (`NamedUnitRepository` or `EnemyRepository`):

```kotlin
"new_hero" to NamedUnit(
    id = "new_hero",
    name = "Hero Name",
    characterClass = CharacterClass.KNIGHT,
    customGrowthRates = GrowthRates(
        hp = 85, mp = 25, attack = 70,
        defense = 75, speed = 55, skill = 60, luck = 50
    ),
    description = "A brave hero"
)
```

2. Reference in chapter definitions:

```kotlin
EnemyUnitSpawn(
    id = "hero_recruit",
    name = "Hero Name",
    characterClass = CharacterClass.KNIGHT,
    level = 3,
    position = Position(5, 5),
    equipment = listOf("steel_sword"),
    namedUnitId = "new_hero"
)
```

## Growth Rate System

### How It Works

When a character levels up:
1. For each stat (HP, MP, Attack, Defense, Speed, Skill, Luck)
2. Roll a random number from 1-100
3. If the roll is ≤ growth rate percentage, increase that stat by 1
4. Guarantee at least one stat increase per level

### Custom Growth Rates

Named units can have growth rates that differ from their class:
- **High Growths**: 80-100% (almost guaranteed to increase)
- **Normal Growths**: 50-80% (typical for most stats)
- **Low Growths**: 20-50% (may not increase)
- **Minimal Growths**: 0-20% (rarely increases)

### Example Comparisons

**Default Knight**:
- HP: 80%, Attack: 60%, Defense: 70%, Speed: 40%

**Sir Garrett (Named Knight)**:
- HP: 85%, Attack: 65%, Defense: 70%, Speed: 45%
- Slightly better than default, befitting a protagonist

**Boss General**:
- HP: 100%, Attack: 80%, Defense: 90%, Speed: 60%
- Significantly enhanced for a climactic boss fight

## Testing

The implementation includes comprehensive unit tests (`NamedUnitTest.kt`) covering:
- Named unit creation and retrieval
- Custom growth rate application
- Level-scaled character creation
- Repository functionality
- Integration with enemy spawning system

All 14 tests pass successfully.

## Benefits

1. **Unique Characters**: Each named character has distinctive progression
2. **Flexible Design**: Easy to add new characters without code changes
3. **Balanced Progression**: Boss units can be stronger without being unfair
4. **Fire Emblem-style**: Follows genre conventions for character uniqueness
5. **Predictable Variance**: Characters grow similarly across playthroughs but with some randomness

## Future Enhancements

Possible extensions to this system:
1. **Personal Skills**: Named units could have unique abilities
2. **Support Bonuses**: Custom support relationship growth rates
3. **Class-specific Growth Curves**: Different growth patterns by level range
4. **Stat Caps**: Per-character maximum stat values
5. **Recruitment System**: Named units joining based on conditions
6. **Character Stories**: Tie narrative elements to named units
