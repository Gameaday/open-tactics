package com.gameaday.opentactics.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

// Terrain constants
private const val PLAIN_MOVEMENT_COST = 1
private const val FOREST_MOVEMENT_COST = 2
private const val FOREST_DEFENSE_BONUS = 1
private const val FOREST_AVOIDANCE = 10
private const val MOUNTAIN_MOVEMENT_COST = 3
private const val MOUNTAIN_DEFENSE_BONUS = 2
private const val MOUNTAIN_AVOIDANCE = 20
private const val WATER_MOVEMENT_COST = 999
private const val FORT_DEFENSE_BONUS = 3
private const val FORT_AVOIDANCE = 20
private const val VILLAGE_DEFENSE_BONUS = 1
private const val VILLAGE_AVOIDANCE = 10

@Serializable
enum class TerrainType(
    val displayName: String,
    val movementCost: Int,
    val defensiveBonus: Int,
    val avoidanceBonus: Int,
) {
    PLAIN("Plain", PLAIN_MOVEMENT_COST, 0, 0),
    FOREST("Forest", FOREST_MOVEMENT_COST, FOREST_DEFENSE_BONUS, FOREST_AVOIDANCE),
    MOUNTAIN("Mountain", MOUNTAIN_MOVEMENT_COST, MOUNTAIN_DEFENSE_BONUS, MOUNTAIN_AVOIDANCE),
    WATER("Water", WATER_MOVEMENT_COST, 0, 0), // Impassable for most units
    FORT("Fort", PLAIN_MOVEMENT_COST, FORT_DEFENSE_BONUS, FORT_AVOIDANCE),
    VILLAGE("Village", PLAIN_MOVEMENT_COST, VILLAGE_DEFENSE_BONUS, VILLAGE_AVOIDANCE),
}

@Parcelize
@Serializable
data class Tile(
    val position: Position,
    var terrain: TerrainType,
    var occupant: Character? = null,
) : Parcelable {
    val isOccupied: Boolean
        get() = occupant != null

    fun canBeOccupiedBy(character: Character): Boolean {
        if (isOccupied) return false
        // Flying units can pass over water terrain
        if (terrain == TerrainType.WATER && !character.characterClass.canFly) return false
        return true
    }
}
