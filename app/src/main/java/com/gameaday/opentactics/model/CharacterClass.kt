package com.gameaday.opentactics.model

import kotlinx.serialization.Serializable

@Serializable
enum class CharacterClass(
    val displayName: String,
    val baseStats: Stats,
    val movementRange: Int,
    val attackRange: Int,
) {
    KNIGHT(
        "Knight",
        Stats(hp = 25, mp = 5, attack = 12, defense = 14, speed = 8, skill = 10, luck = 8),
        movementRange = 3,
        attackRange = 1,
    ),

    ARCHER(
        "Archer",
        Stats(hp = 18, mp = 8, attack = 14, defense = 8, speed = 12, skill = 16, luck = 10),
        movementRange = 4,
        attackRange = 3,
    ),

    MAGE(
        "Mage",
        Stats(hp = 15, mp = 20, attack = 16, defense = 6, speed = 10, skill = 18, luck = 12),
        movementRange = 3,
        attackRange = 2,
    ),

    HEALER(
        "Healer",
        Stats(hp = 20, mp = 18, attack = 8, defense = 10, speed = 11, skill = 15, luck = 14),
        movementRange = 3,
        attackRange = 2,
    ),

    THIEF(
        "Thief",
        Stats(hp = 16, mp = 10, attack = 11, defense = 7, speed = 18, skill = 14, luck = 16),
        movementRange = 5,
        attackRange = 1,
    ),
}
