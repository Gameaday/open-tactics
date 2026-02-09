package com.gameaday.opentactics.factory

import com.gameaday.opentactics.model.MapLayout
import com.gameaday.opentactics.model.TerrainType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MapFactoryTest {
    @Test
    fun testCreateTestMap() {
        val board = MapFactory.createTestMap()

        assertEquals(12, board.width)
        assertEquals(8, board.height)

        // Verify terrain is set correctly
        assertEquals(TerrainType.FOREST, board.getTile(3, 1)?.terrain)
        assertEquals(TerrainType.FOREST, board.getTile(4, 1)?.terrain)
        assertEquals(TerrainType.MOUNTAIN, board.getTile(7, 1)?.terrain)
        assertEquals(TerrainType.MOUNTAIN, board.getTile(8, 1)?.terrain)
        assertEquals(TerrainType.FORT, board.getTile(5, 1)?.terrain)
    }

    @Test
    fun testCreatePlainsBattle() {
        val board = MapFactory.createPlainsBattle()

        assertEquals(14, board.width)
        assertEquals(10, board.height)

        // Verify some terrain features exist
        assertNotNull(board.getTile(3, 3))
        assertEquals(TerrainType.FOREST, board.getTile(3, 3)?.terrain)
        assertEquals(TerrainType.MOUNTAIN, board.getTile(1, 1)?.terrain)
    }

    @Test
    fun testCreateForestAmbush() {
        val board = MapFactory.createForestAmbush()

        assertEquals(12, board.width)
        assertEquals(10, board.height)

        // Verify dense forest clusters
        assertEquals(TerrainType.FOREST, board.getTile(2, 2)?.terrain)
        assertEquals(TerrainType.FOREST, board.getTile(3, 3)?.terrain)
        assertEquals(TerrainType.FOREST, board.getTile(7, 5)?.terrain)
    }

    @Test
    fun testCreateMountainPass() {
        val board = MapFactory.createMountainPass()

        assertEquals(12, board.width)
        assertEquals(10, board.height)

        // Verify mountain walls create a pass
        assertEquals(TerrainType.MOUNTAIN, board.getTile(1, 0)?.terrain)
        assertEquals(TerrainType.MOUNTAIN, board.getTile(2, 0)?.terrain)
        assertEquals(TerrainType.MOUNTAIN, board.getTile(9, 0)?.terrain)
        assertEquals(TerrainType.MOUNTAIN, board.getTile(10, 0)?.terrain)

        // Verify center is passable
        assertEquals(TerrainType.PLAIN, board.getTile(5, 0)?.terrain)
    }

    @Test
    fun testCreateCastleSiege() {
        val board = MapFactory.createCastleSiege()

        assertEquals(14, board.width)
        assertEquals(12, board.height)

        // Verify castle walls (mountains)
        assertEquals(TerrainType.MOUNTAIN, board.getTile(7, 2)?.terrain)
        assertEquals(TerrainType.MOUNTAIN, board.getTile(11, 2)?.terrain)

        // Verify throne room / fort
        assertEquals(TerrainType.FORT, board.getTile(9, 5)?.terrain)
        assertEquals(TerrainType.FORT, board.getTile(9, 6)?.terrain)

        // Verify gates
        assertEquals(TerrainType.PLAIN, board.getTile(9, 2)?.terrain)
    }

    @Test
    fun testCreateVillageDefense() {
        val board = MapFactory.createVillageDefense()

        assertEquals(14, board.width)
        assertEquals(10, board.height)

        // Verify villages
        assertEquals(TerrainType.VILLAGE, board.getTile(3, 3)?.terrain)
        assertEquals(TerrainType.VILLAGE, board.getTile(10, 3)?.terrain)
        assertEquals(TerrainType.VILLAGE, board.getTile(3, 7)?.terrain)
        assertEquals(TerrainType.VILLAGE, board.getTile(10, 7)?.terrain)

        // Verify central fort
        assertEquals(TerrainType.FORT, board.getTile(7, 5)?.terrain)
    }

    @Test
    fun testCreateRiverCrossing() {
        val board = MapFactory.createRiverCrossing()

        assertEquals(14, board.width)
        assertEquals(10, board.height)

        // Verify river
        assertEquals(TerrainType.WATER, board.getTile(6, 0)?.terrain)
        assertEquals(TerrainType.WATER, board.getTile(7, 0)?.terrain)
        assertEquals(TerrainType.WATER, board.getTile(6, 5)?.terrain)

        // Verify bridge crossings
        assertEquals(TerrainType.PLAIN, board.getTile(6, 3)?.terrain)
        assertEquals(TerrainType.PLAIN, board.getTile(7, 3)?.terrain)

        // Verify fort positions
        assertEquals(TerrainType.FORT, board.getTile(5, 3)?.terrain)
    }

    @Test
    fun testCreateMapWithAllLayouts() {
        // Test that all map layouts can be created without exceptions
        val layouts = MapLayout.entries

        for (layout in layouts) {
            val board = MapFactory.createMap(layout)
            assertNotNull(board)
            assertTrue(board.width > 0)
            assertTrue(board.height > 0)
        }
    }

    @Test
    fun testAllMapsHaveValidTiles() {
        val layouts = MapLayout.entries

        for (layout in layouts) {
            val board = MapFactory.createMap(layout)

            // Verify all tiles are initialized
            for (x in 0 until board.width) {
                for (y in 0 until board.height) {
                    assertNotNull("Tile at ($x, $y) should not be null for $layout", board.getTile(x, y))
                }
            }
        }
    }
}
