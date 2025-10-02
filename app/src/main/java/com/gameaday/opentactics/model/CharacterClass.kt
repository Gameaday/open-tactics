package com.gameaday.opentactics.model

import kotlinx.serialization.Serializable

@Serializable
@Suppress("LongParameterList")
enum class CharacterClass(
    val displayName: String,
    val baseStats: Stats,
    val movementRange: Int,
    val attackRange: Int,
    val canFly: Boolean = false,
    val canTransform: Boolean = false,
    val transformsToName: String? = null,
    val hasCanto: Boolean = false, // Can move again after attacking (mounted units)
) {
    KNIGHT(
        "Knight",
        Stats(hp = 25, mp = 5, attack = 12, defense = 14, speed = 8, skill = 10, luck = 8),
        movementRange = 3,
        attackRange = 1,
        canFly = false,
        hasCanto = true, // Mounted unit
    ),

    ARCHER(
        "Archer",
        Stats(hp = 18, mp = 8, attack = 14, defense = 8, speed = 12, skill = 16, luck = 10),
        movementRange = 4,
        attackRange = 3,
        canFly = false,
    ),

    MAGE(
        "Mage",
        Stats(hp = 15, mp = 20, attack = 16, defense = 6, speed = 10, skill = 18, luck = 12),
        movementRange = 3,
        attackRange = 2,
        canFly = false,
    ),

    HEALER(
        "Healer",
        Stats(hp = 20, mp = 18, attack = 8, defense = 10, speed = 11, skill = 15, luck = 14),
        movementRange = 3,
        attackRange = 2,
        canFly = false,
    ),

    THIEF(
        "Thief",
        Stats(hp = 16, mp = 10, attack = 11, defense = 7, speed = 18, skill = 14, luck = 16),
        movementRange = 5,
        attackRange = 1,
        canFly = false,
    ),

    PEGASUS_KNIGHT(
        "Pegasus Knight",
        Stats(hp = 20, mp = 8, attack = 13, defense = 10, speed = 16, skill = 14, luck = 12),
        movementRange = 6,
        attackRange = 1,
        canFly = true,
        hasCanto = true, // Flying mounted unit
    ),

    WYVERN_RIDER(
        "Wyvern Rider",
        Stats(hp = 28, mp = 6, attack = 16, defense = 16, speed = 12, skill = 12, luck = 8),
        movementRange = 6,
        attackRange = 1,
        canFly = true,
        hasCanto = true, // Flying mounted unit
    ),

    MANAKETE(
        "Manakete",
        Stats(hp = 22, mp = 15, attack = 10, defense = 12, speed = 10, skill = 12, luck = 10),
        movementRange = 3,
        attackRange = 1,
        canFly = false,
        canTransform = true,
        transformsToName = "DRAGON",
    ),

    DRAGON(
        "Dragon",
        Stats(hp = 35, mp = 20, attack = 22, defense = 20, speed = 14, skill = 16, luck = 12),
        movementRange = 5,
        attackRange = 2,
        canFly = true,
        canTransform = false,
    ),
    ;

    val transformsTo: CharacterClass?
        get() =
            transformsToName?.let { name ->
                values().find { it.name == name }
            }
}
