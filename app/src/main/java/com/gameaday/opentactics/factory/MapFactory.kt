@file:Suppress("MagicNumber") // Map dimensions and positions are inherently numeric

package com.gameaday.opentactics.factory

import com.gameaday.opentactics.model.GameBoard
import com.gameaday.opentactics.model.MapLayout
import com.gameaday.opentactics.model.TerrainType

/**
 * Factory for creating different map layouts for the game.
 * This centralizes map creation and makes it easy to add new map types.
 */
object MapFactory {
    /**
     * Create a game board based on the specified map layout
     * @param layout The type of map to create
     * @return A configured GameBoard with terrain set up
     */
    fun createMap(layout: MapLayout): GameBoard =
        when (layout) {
            MapLayout.TEST_MAP -> createTestMap()
            MapLayout.PLAINS_BATTLE -> createPlainsBattle()
            MapLayout.FOREST_AMBUSH -> createForestAmbush()
            MapLayout.MOUNTAIN_PASS -> createMountainPass()
            MapLayout.CASTLE_SIEGE -> createCastleSiege()
            MapLayout.VILLAGE_DEFENSE -> createVillageDefense()
            MapLayout.RIVER_CROSSING -> createRiverCrossing()
        }

    /**
     * Create the basic test map - simple 12x8 map with minimal terrain
     */
    fun createTestMap(): GameBoard {
        val board = GameBoard(12, 8)

        // Add some terrain variety
        board.getTile(3, 1)?.terrain = TerrainType.FOREST
        board.getTile(4, 1)?.terrain = TerrainType.FOREST
        board.getTile(7, 1)?.terrain = TerrainType.MOUNTAIN
        board.getTile(8, 1)?.terrain = TerrainType.MOUNTAIN
        board.getTile(5, 1)?.terrain = TerrainType.FORT

        return board
    }

    /**
     * Create an open plains battlefield - large open area with minimal cover
     */
    fun createPlainsBattle(): GameBoard {
        val board = GameBoard(14, 10)

        // Scattered forest patches for minimal cover
        board.getTile(3, 3)?.terrain = TerrainType.FOREST
        board.getTile(4, 3)?.terrain = TerrainType.FOREST
        board.getTile(9, 6)?.terrain = TerrainType.FOREST
        board.getTile(10, 6)?.terrain = TerrainType.FOREST

        // A few mountains on edges
        board.getTile(1, 1)?.terrain = TerrainType.MOUNTAIN
        board.getTile(12, 8)?.terrain = TerrainType.MOUNTAIN

        return board
    }

    /**
     * Create a forest ambush map - dense forests with limited visibility
     */
    fun createForestAmbush(): GameBoard {
        val board = GameBoard(12, 10)

        // Dense forest clusters
        for (x in 2..4) {
            for (y in 2..4) {
                board.getTile(x, y)?.terrain = TerrainType.FOREST
            }
        }

        for (x in 7..9) {
            for (y in 5..7) {
                board.getTile(x, y)?.terrain = TerrainType.FOREST
            }
        }

        // Additional scattered forests
        board.getTile(5, 1)?.terrain = TerrainType.FOREST
        board.getTile(6, 8)?.terrain = TerrainType.FOREST
        board.getTile(1, 6)?.terrain = TerrainType.FOREST
        board.getTile(10, 2)?.terrain = TerrainType.FOREST

        return board
    }

    /**
     * Create a mountain pass - narrow path through mountains
     */
    fun createMountainPass(): GameBoard {
        val board = GameBoard(12, 10)

        // Mountain ranges on sides creating a pass
        for (y in 0..9) {
            // Left mountain wall
            board.getTile(1, y)?.terrain = TerrainType.MOUNTAIN
            board.getTile(2, y)?.terrain = TerrainType.MOUNTAIN

            // Right mountain wall
            board.getTile(9, y)?.terrain = TerrainType.MOUNTAIN
            board.getTile(10, y)?.terrain = TerrainType.MOUNTAIN
        }

        // Add some obstacles in the pass
        board.getTile(5, 3)?.terrain = TerrainType.MOUNTAIN
        board.getTile(6, 6)?.terrain = TerrainType.FOREST
        board.getTile(5, 7)?.terrain = TerrainType.FOREST

        return board
    }

    /**
     * Create a castle siege map - fortified position with throne
     */
    fun createCastleSiege(): GameBoard {
        val board = GameBoard(14, 12)

        // Castle walls (mountains representing walls)
        for (x in 7..11) {
            board.getTile(x, 2)?.terrain = TerrainType.MOUNTAIN // North wall
            board.getTile(x, 9)?.terrain = TerrainType.MOUNTAIN // South wall
        }
        for (y in 3..8) {
            board.getTile(7, y)?.terrain = TerrainType.MOUNTAIN // West wall
            board.getTile(11, y)?.terrain = TerrainType.MOUNTAIN // East wall
        }

        // Throne room / fort in the center
        board.getTile(9, 5)?.terrain = TerrainType.FORT
        board.getTile(9, 6)?.terrain = TerrainType.FORT

        // Gates (gaps in walls)
        board.getTile(9, 2)?.terrain = TerrainType.PLAIN // North gate
        board.getTile(9, 9)?.terrain = TerrainType.PLAIN // South gate

        // Defensive positions near gates
        board.getTile(8, 3)?.terrain = TerrainType.FORT
        board.getTile(10, 3)?.terrain = TerrainType.FORT
        board.getTile(8, 8)?.terrain = TerrainType.FORT
        board.getTile(10, 8)?.terrain = TerrainType.FORT

        return board
    }

    /**
     * Create a village defense map - scattered villages to protect
     */
    fun createVillageDefense(): GameBoard {
        val board = GameBoard(14, 10)

        // Villages to defend
        board.getTile(3, 3)?.terrain = TerrainType.VILLAGE
        board.getTile(10, 3)?.terrain = TerrainType.VILLAGE
        board.getTile(3, 7)?.terrain = TerrainType.VILLAGE
        board.getTile(10, 7)?.terrain = TerrainType.VILLAGE

        // Forest patches near villages
        board.getTile(2, 3)?.terrain = TerrainType.FOREST
        board.getTile(4, 3)?.terrain = TerrainType.FOREST
        board.getTile(9, 3)?.terrain = TerrainType.FOREST
        board.getTile(11, 3)?.terrain = TerrainType.FOREST

        board.getTile(2, 7)?.terrain = TerrainType.FOREST
        board.getTile(4, 7)?.terrain = TerrainType.FOREST
        board.getTile(9, 7)?.terrain = TerrainType.FOREST
        board.getTile(11, 7)?.terrain = TerrainType.FOREST

        // Central fort
        board.getTile(7, 5)?.terrain = TerrainType.FORT

        return board
    }

    /**
     * Create a river crossing map - map split by water with limited crossing points
     */
    fun createRiverCrossing(): GameBoard {
        val board = GameBoard(14, 10)

        // River running through the middle
        for (y in 0..9) {
            board.getTile(6, y)?.terrain = TerrainType.WATER
            board.getTile(7, y)?.terrain = TerrainType.WATER
        }

        // Bridge crossings (plains through water)
        board.getTile(6, 3)?.terrain = TerrainType.PLAIN
        board.getTile(7, 3)?.terrain = TerrainType.PLAIN
        board.getTile(6, 7)?.terrain = TerrainType.PLAIN
        board.getTile(7, 7)?.terrain = TerrainType.PLAIN

        // Fort positions near bridges
        board.getTile(5, 3)?.terrain = TerrainType.FORT
        board.getTile(8, 3)?.terrain = TerrainType.FORT
        board.getTile(5, 7)?.terrain = TerrainType.FORT
        board.getTile(8, 7)?.terrain = TerrainType.FORT

        // Forests on both sides for cover
        board.getTile(2, 2)?.terrain = TerrainType.FOREST
        board.getTile(3, 2)?.terrain = TerrainType.FOREST
        board.getTile(10, 8)?.terrain = TerrainType.FOREST
        board.getTile(11, 8)?.terrain = TerrainType.FOREST

        return board
    }
}
