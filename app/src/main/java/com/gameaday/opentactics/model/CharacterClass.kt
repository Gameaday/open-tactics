package com.gameaday.opentactics.model

import kotlinx.serialization.Serializable

@Serializable
@Suppress("LongParameterList")
enum class CharacterClass(
    val displayName: String,
    val baseStats: Stats,
    val growthRates: GrowthRates,
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
        GrowthRates(hp = 80, mp = 20, attack = 60, defense = 70, speed = 40, skill = 50, luck = 45),
        movementRange = 3,
        attackRange = 1,
        canFly = false,
        hasCanto = true, // Mounted unit
    ),

    ARCHER(
        "Archer",
        Stats(hp = 18, mp = 8, attack = 14, defense = 8, speed = 12, skill = 16, luck = 10),
        GrowthRates(hp = 60, mp = 30, attack = 70, defense = 40, speed = 65, skill = 80, luck = 55),
        movementRange = 4,
        attackRange = 3,
        canFly = false,
    ),

    MAGE(
        "Mage",
        Stats(hp = 15, mp = 20, attack = 16, defense = 6, speed = 10, skill = 18, luck = 12),
        GrowthRates(hp = 50, mp = 75, attack = 75, defense = 30, speed = 55, skill = 70, luck = 60),
        movementRange = 3,
        attackRange = 2,
        canFly = false,
    ),

    HEALER(
        "Healer",
        Stats(hp = 20, mp = 18, attack = 8, defense = 10, speed = 11, skill = 15, luck = 14),
        GrowthRates(hp = 65, mp = 70, attack = 35, defense = 45, speed = 60, skill = 65, luck = 70),
        movementRange = 3,
        attackRange = 2,
        canFly = false,
    ),

    THIEF(
        "Thief",
        Stats(hp = 16, mp = 10, attack = 11, defense = 7, speed = 18, skill = 14, luck = 16),
        GrowthRates(hp = 55, mp = 35, attack = 55, defense = 35, speed = 85, skill = 70, luck = 80),
        movementRange = 5,
        attackRange = 1,
        canFly = false,
    ),

    PEGASUS_KNIGHT(
        "Pegasus Knight",
        Stats(hp = 20, mp = 8, attack = 13, defense = 10, speed = 16, skill = 14, luck = 12),
        GrowthRates(hp = 60, mp = 25, attack = 60, defense = 45, speed = 75, skill = 65, luck = 60),
        movementRange = 6,
        attackRange = 1,
        canFly = true,
        hasCanto = true, // Flying mounted unit
    ),

    WYVERN_RIDER(
        "Wyvern Rider",
        Stats(hp = 28, mp = 6, attack = 16, defense = 16, speed = 12, skill = 12, luck = 8),
        GrowthRates(hp = 85, mp = 15, attack = 75, defense = 75, speed = 50, skill = 55, luck = 40),
        movementRange = 6,
        attackRange = 1,
        canFly = true,
        hasCanto = true, // Flying mounted unit
    ),

    MANAKETE(
        "Manakete",
        Stats(hp = 22, mp = 15, attack = 10, defense = 12, speed = 10, skill = 12, luck = 10),
        GrowthRates(hp = 70, mp = 60, attack = 50, defense = 55, speed = 50, skill = 55, luck = 50),
        movementRange = 3,
        attackRange = 1,
        canFly = false,
        canTransform = true,
        transformsToName = "DRAGON",
    ),

    DRAGON(
        "Dragon",
        Stats(hp = 35, mp = 20, attack = 22, defense = 20, speed = 14, skill = 16, luck = 12),
        GrowthRates(hp = 90, mp = 70, attack = 80, defense = 80, speed = 60, skill = 70, luck = 55),
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
