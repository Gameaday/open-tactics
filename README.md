# Open Tactics

A Fire Emblem-style tactical RPG for Android, built with modern Kotlin architecture.

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

## Quick Start

### Run the Console Demo
```bash
./gradlew run
```

This will run a tactical battle simulation showing the complete gameplay loop with visual ASCII representation of the battlefield.

### Run Tests
```bash
./gradlew test
```

## Project Structure

```
src/main/kotlin/com/gameaday/opentactics/
├── Main.kt                    # Console demo entry point
├── model/                     # Core game data models
│   ├── Character.kt          # Character entities with stats
│   ├── CharacterClass.kt     # Class definitions and base stats
│   ├── GameBoard.kt          # Tactical grid and positioning
│   ├── Position.kt           # 2D coordinates and utilities
│   ├── Stats.kt              # Character statistics
│   └── Tile.kt               # Individual board tiles and terrain
└── game/                     # Game logic and state management
    └── GameState.kt          # Turn management, AI, combat resolution
```

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
- Save/load functionality
- Additional character classes and abilities
- Sound effects and music
- Play Store deployment preparation

## Development Notes

This project demonstrates a complete tactical RPG engine suitable for mobile deployment. The clean separation between game logic and presentation layers makes it easy to add different UI implementations (Android, web, desktop) while maintaining the same core gameplay experience.