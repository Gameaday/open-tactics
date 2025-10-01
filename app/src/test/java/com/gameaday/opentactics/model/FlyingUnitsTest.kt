package com.gameaday.opentactics.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class FlyingUnitsTest {
    private lateinit var pegasusKnight: Character
    private lateinit var wyvernRider: Character
    private lateinit var knight: Character
    private lateinit var waterTile: Tile
    private lateinit var plainTile: Tile

    @Before
    fun setUp() {
        pegasusKnight =
            Character(
                id = "pegasus1",
                name = "Shanna",
                characterClass = CharacterClass.PEGASUS_KNIGHT,
                team = Team.PLAYER,
                position = Position(0, 0),
            )

        wyvernRider =
            Character(
                id = "wyvern1",
                name = "Heath",
                characterClass = CharacterClass.WYVERN_RIDER,
                team = Team.PLAYER,
                position = Position(1, 0),
            )

        knight =
            Character(
                id = "knight1",
                name = "Sir Arthur",
                characterClass = CharacterClass.KNIGHT,
                team = Team.PLAYER,
                position = Position(2, 0),
            )

        waterTile = Tile(Position(5, 5), TerrainType.WATER)
        plainTile = Tile(Position(6, 6), TerrainType.PLAIN)
    }

    @Test
    fun testFlyingCharacterClassesCanFly() {
        assertTrue(CharacterClass.PEGASUS_KNIGHT.canFly)
        assertTrue(CharacterClass.WYVERN_RIDER.canFly)
        assertTrue(CharacterClass.DRAGON.canFly)

        assertFalse(CharacterClass.KNIGHT.canFly)
        assertFalse(CharacterClass.ARCHER.canFly)
        assertFalse(CharacterClass.MAGE.canFly)
        assertFalse(CharacterClass.MANAKETE.canFly)
    }

    @Test
    fun testFlyingUnitsCanCrossWater() {
        assertTrue(waterTile.canBeOccupiedBy(pegasusKnight))
        assertTrue(waterTile.canBeOccupiedBy(wyvernRider))
        assertFalse(waterTile.canBeOccupiedBy(knight))
    }

    @Test
    fun testAllUnitsCanOccupyPlainTerrain() {
        assertTrue(plainTile.canBeOccupiedBy(pegasusKnight))
        assertTrue(plainTile.canBeOccupiedBy(wyvernRider))
        assertTrue(plainTile.canBeOccupiedBy(knight))
    }

    @Test
    fun testFlyingUnitsHaveHighMovementRange() {
        assertEquals(6, CharacterClass.PEGASUS_KNIGHT.movementRange)
        assertEquals(6, CharacterClass.WYVERN_RIDER.movementRange)

        // Compare to ground units
        assertEquals(3, CharacterClass.KNIGHT.movementRange)
        assertEquals(4, CharacterClass.ARCHER.movementRange)
    }

    @Test
    fun testPegasusKnightStats() {
        val stats = CharacterClass.PEGASUS_KNIGHT.baseStats

        // Pegasus Knights are fast but fragile
        assertTrue(stats.speed > 12)
        assertTrue(stats.defense < 15)
        assertTrue(stats.attack > 10)
    }

    @Test
    fun testWyvernRiderStats() {
        val stats = CharacterClass.WYVERN_RIDER.baseStats

        // Wyvern Riders are strong and tanky
        assertTrue(stats.attack > 14)
        assertTrue(stats.defense > 14)
        assertTrue(stats.hp > 25)
    }

    @Test
    fun testTransformedDragonCanFly() {
        val manakete =
            Character(
                id = "manakete1",
                name = "Tiki",
                characterClass = CharacterClass.MANAKETE,
                team = Team.PLAYER,
                position = Position(0, 0),
            )

        // Before transformation, cannot cross water
        assertFalse(waterTile.canBeOccupiedBy(manakete))

        // Transform to dragon
        manakete.transform()

        // After transformation, can cross water
        assertTrue(waterTile.canBeOccupiedBy(manakete))
    }

    @Test
    fun testOccupiedWaterTileBlocksEvenFlyingUnits() {
        waterTile.occupant = knight

        assertFalse(waterTile.canBeOccupiedBy(pegasusKnight))
        assertFalse(waterTile.canBeOccupiedBy(wyvernRider))
    }
}
