package com.gameaday.opentactics.model

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class TileTest {
    private lateinit var plainTile: Tile
    private lateinit var forestTile: Tile
    private lateinit var mountainTile: Tile
    private lateinit var waterTile: Tile
    private lateinit var fortTile: Tile
    private lateinit var character: Character

    @Before
    fun setUp() {
        val position = Position(0, 0)
        plainTile = Tile(position, TerrainType.PLAIN)
        forestTile = Tile(position, TerrainType.FOREST)
        mountainTile = Tile(position, TerrainType.MOUNTAIN)
        waterTile = Tile(position, TerrainType.WATER)
        fortTile = Tile(position, TerrainType.FORT)

        character =
            Character(
                id = "test",
                name = "Test Character",
                characterClass = CharacterClass.KNIGHT,
                team = Team.PLAYER,
                position = position,
            )
    }

    @Test
    fun testTileInitialization() {
        val position = Position(5, 3)
        val tile = Tile(position, TerrainType.FOREST)

        assertEquals(position, tile.position)
        assertEquals(TerrainType.FOREST, tile.terrain)
        assertNull(tile.occupant)
        assertFalse(tile.isOccupied)
    }

    @Test
    fun testTileOccupancy() {
        assertFalse(plainTile.isOccupied)
        assertNull(plainTile.occupant)

        plainTile.occupant = character
        assertTrue(plainTile.isOccupied)
        assertEquals(character, plainTile.occupant)

        plainTile.occupant = null
        assertFalse(plainTile.isOccupied)
        assertNull(plainTile.occupant)
    }

    @Test
    fun testCanBeOccupiedByCharacter() {
        // Empty tile should accept character
        assertTrue(plainTile.canBeOccupiedBy(character))
        assertTrue(forestTile.canBeOccupiedBy(character))
        assertTrue(mountainTile.canBeOccupiedBy(character))
        assertTrue(fortTile.canBeOccupiedBy(character))

        // Water should not accept character
        assertFalse(waterTile.canBeOccupiedBy(character))
    }

    @Test
    fun testCannotOccupyOccupiedTile() {
        val anotherCharacter =
            Character(
                id = "test2",
                name = "Another Character",
                characterClass = CharacterClass.ARCHER,
                team = Team.ENEMY,
                position = Position(1, 1),
            )

        plainTile.occupant = character
        assertFalse(plainTile.canBeOccupiedBy(anotherCharacter))
    }

    @Test
    fun testTerrainTypes() {
        assertEquals(TerrainType.PLAIN, plainTile.terrain)
        assertEquals(TerrainType.FOREST, forestTile.terrain)
        assertEquals(TerrainType.MOUNTAIN, mountainTile.terrain)
        assertEquals(TerrainType.WATER, waterTile.terrain)
        assertEquals(TerrainType.FORT, fortTile.terrain)
    }

    @Test
    fun testTerrainTypeProperties() {
        // Plain terrain
        assertEquals("Plain", TerrainType.PLAIN.displayName)
        assertEquals(1, TerrainType.PLAIN.movementCost)
        assertEquals(0, TerrainType.PLAIN.defensiveBonus)
        assertEquals(0, TerrainType.PLAIN.avoidanceBonus)

        // Forest terrain
        assertEquals("Forest", TerrainType.FOREST.displayName)
        assertEquals(2, TerrainType.FOREST.movementCost)
        assertEquals(1, TerrainType.FOREST.defensiveBonus)
        assertEquals(10, TerrainType.FOREST.avoidanceBonus)

        // Mountain terrain
        assertEquals("Mountain", TerrainType.MOUNTAIN.displayName)
        assertEquals(3, TerrainType.MOUNTAIN.movementCost)
        assertEquals(2, TerrainType.MOUNTAIN.defensiveBonus)
        assertEquals(20, TerrainType.MOUNTAIN.avoidanceBonus)

        // Water terrain
        assertEquals("Water", TerrainType.WATER.displayName)
        assertEquals(999, TerrainType.WATER.movementCost) // Impassable
        assertEquals(0, TerrainType.WATER.defensiveBonus)
        assertEquals(0, TerrainType.WATER.avoidanceBonus)

        // Fort terrain
        assertEquals("Fort", TerrainType.FORT.displayName)
        assertEquals(1, TerrainType.FORT.movementCost)
        assertEquals(3, TerrainType.FORT.defensiveBonus)
        assertEquals(20, TerrainType.FORT.avoidanceBonus)

        // Village terrain
        assertEquals("Village", TerrainType.VILLAGE.displayName)
        assertEquals(1, TerrainType.VILLAGE.movementCost)
        assertEquals(1, TerrainType.VILLAGE.defensiveBonus)
        assertEquals(10, TerrainType.VILLAGE.avoidanceBonus)
    }

    @Test
    fun testMutableTerrain() {
        val tile = Tile(Position(0, 0), TerrainType.PLAIN)
        assertEquals(TerrainType.PLAIN, tile.terrain)

        tile.terrain = TerrainType.FOREST
        assertEquals(TerrainType.FOREST, tile.terrain)
    }
}
