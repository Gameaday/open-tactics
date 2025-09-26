package com.gameaday.opentactics.model

enum class TerrainType(
    val displayName: String,
    val movementCost: Int,
    val defensiveBonus: Int,
    val avoidanceBonus: Int
) {
    PLAIN("Plain", 1, 0, 0),
    FOREST("Forest", 2, 1, 10),
    MOUNTAIN("Mountain", 3, 2, 20),
    WATER("Water", 999, 0, 0), // Impassable for most units
    FORT("Fort", 1, 3, 20),
    VILLAGE("Village", 1, 1, 10)
}

data class Tile(
    val position: Position,
    var terrain: TerrainType,
    var occupant: Character? = null
) {
    val isOccupied: Boolean
        get() = occupant != null
    
    fun canBeOccupiedBy(character: Character): Boolean {
        if (isOccupied) return false
        if (terrain == TerrainType.WATER) return false // TODO: Add flying units later
        return true
    }
}