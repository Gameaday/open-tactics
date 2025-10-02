package com.gameaday.opentactics.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TerrainInfoTest {
    @Test
    fun testPlainTerrainProperties() {
        val terrain = TerrainType.PLAIN

        assertEquals("Plain", terrain.displayName)
        assertEquals(1, terrain.movementCost)
        assertEquals(0, terrain.defensiveBonus)
        assertEquals(0, terrain.avoidanceBonus)
    }

    @Test
    fun testForestTerrainProperties() {
        val terrain = TerrainType.FOREST

        assertEquals("Forest", terrain.displayName)
        assertEquals(2, terrain.movementCost)
        assertEquals(1, terrain.defensiveBonus)
        assertEquals(10, terrain.avoidanceBonus)
    }

    @Test
    fun testMountainTerrainProperties() {
        val terrain = TerrainType.MOUNTAIN

        assertEquals("Mountain", terrain.displayName)
        assertEquals(3, terrain.movementCost)
        assertEquals(2, terrain.defensiveBonus)
        assertEquals(20, terrain.avoidanceBonus)
    }

    @Test
    fun testFortTerrainProperties() {
        val terrain = TerrainType.FORT

        assertEquals("Fort", terrain.displayName)
        assertEquals(1, terrain.movementCost)
        assertEquals(3, terrain.defensiveBonus)
        assertEquals(20, terrain.avoidanceBonus)
    }

    @Test
    fun testVillageTerrainProperties() {
        val terrain = TerrainType.VILLAGE

        assertEquals("Village", terrain.displayName)
        assertEquals(1, terrain.movementCost)
        assertEquals(1, terrain.defensiveBonus)
        assertEquals(10, terrain.avoidanceBonus)
    }

    @Test
    fun testWaterTerrainImpassable() {
        val terrain = TerrainType.WATER

        assertEquals("Water", terrain.displayName)
        assertEquals(999, terrain.movementCost) // Effectively impassable
        assertEquals(0, terrain.defensiveBonus)
        assertEquals(0, terrain.avoidanceBonus)
    }

    @Test
    fun testTileCanBeOccupiedByGroundUnit() {
        val position = Position(0, 0)
        val plainTile = Tile(position, TerrainType.PLAIN)
        val groundUnit =
            Character(
                id = "knight",
                name = "Knight",
                characterClass = CharacterClass.KNIGHT,
                team = Team.PLAYER,
                position = position,
            )

        assertTrue(plainTile.canBeOccupiedBy(groundUnit))
    }

    @Test
    fun testTileCannotBeOccupiedByGroundUnitOnWater() {
        val position = Position(0, 0)
        val waterTile = Tile(position, TerrainType.WATER)
        val groundUnit =
            Character(
                id = "knight",
                name = "Knight",
                characterClass = CharacterClass.KNIGHT,
                team = Team.PLAYER,
                position = position,
            )

        assertFalse(waterTile.canBeOccupiedBy(groundUnit))
    }

    @Test
    fun testTileCanBeOccupiedByFlyingUnitOnWater() {
        val position = Position(0, 0)
        val waterTile = Tile(position, TerrainType.WATER)
        val flyingUnit =
            Character(
                id = "pegasus",
                name = "Pegasus Knight",
                characterClass = CharacterClass.PEGASUS_KNIGHT,
                team = Team.PLAYER,
                position = position,
            )

        assertTrue(waterTile.canBeOccupiedBy(flyingUnit))
    }

    @Test
    fun testOccupiedTileCannotBeOccupied() {
        val position = Position(0, 0)
        val tile = Tile(position, TerrainType.PLAIN)
        val unit1 =
            Character(
                id = "knight1",
                name = "Knight 1",
                characterClass = CharacterClass.KNIGHT,
                team = Team.PLAYER,
                position = position,
            )
        val unit2 =
            Character(
                id = "knight2",
                name = "Knight 2",
                characterClass = CharacterClass.KNIGHT,
                team = Team.PLAYER,
                position = position,
            )

        tile.occupant = unit1
        assertFalse(tile.canBeOccupiedBy(unit2))
    }
}
