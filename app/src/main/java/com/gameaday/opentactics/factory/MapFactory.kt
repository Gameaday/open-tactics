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
            MapLayout.BORDER_FORT -> createBorderFort()
            MapLayout.COASTAL_RUINS -> createCoastalRuins()
            MapLayout.DARK_FOREST -> createDarkForest()
            MapLayout.FORTRESS_INTERIOR -> createFortressInterior()
            MapLayout.THRONE_ROOM -> createThroneRoom()
            MapLayout.DESERT_OUTPOST -> createDesertOutpost()
            MapLayout.FROZEN_PASS -> createFrozenPass()
            MapLayout.CRIMSON_CAPITAL -> createCrimsonCapital()
            MapLayout.IMPERIAL_PALACE -> createImperialPalace()
            MapLayout.DRAGON_LAIR -> createDragonLair()
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

    /**
     * Create a fortified border outpost - defensive positions along a frontier
     */
    fun createBorderFort(): GameBoard {
        val board = GameBoard(14, 10)

        // Central fortifications
        board.getTile(6, 4)?.terrain = TerrainType.FORT
        board.getTile(7, 4)?.terrain = TerrainType.FORT
        board.getTile(6, 5)?.terrain = TerrainType.FORT
        board.getTile(7, 5)?.terrain = TerrainType.FORT

        // Walls around fort (mountains)
        for (x in 5..8) {
            board.getTile(x, 3)?.terrain = TerrainType.MOUNTAIN
            board.getTile(x, 6)?.terrain = TerrainType.MOUNTAIN
        }

        // Entry points (gaps)
        board.getTile(6, 3)?.terrain = TerrainType.PLAIN
        board.getTile(7, 6)?.terrain = TerrainType.PLAIN

        // Forest cover on flanks
        board.getTile(2, 2)?.terrain = TerrainType.FOREST
        board.getTile(3, 2)?.terrain = TerrainType.FOREST
        board.getTile(2, 7)?.terrain = TerrainType.FOREST
        board.getTile(3, 7)?.terrain = TerrainType.FOREST
        board.getTile(10, 2)?.terrain = TerrainType.FOREST
        board.getTile(11, 7)?.terrain = TerrainType.FOREST

        return board
    }

    /**
     * Create coastal ruins - scattered ruins along a coastline with water on one side
     */
    fun createCoastalRuins(): GameBoard {
        val board = GameBoard(14, 10)

        // Water along the bottom
        for (x in 0..13) {
            board.getTile(x, 9)?.terrain = TerrainType.WATER
            board.getTile(x, 8)?.terrain = TerrainType.WATER
        }

        // Ruined structures (forts)
        board.getTile(3, 3)?.terrain = TerrainType.FORT
        board.getTile(4, 3)?.terrain = TerrainType.FORT
        board.getTile(9, 4)?.terrain = TerrainType.FORT
        board.getTile(10, 4)?.terrain = TerrainType.FORT

        // Scattered mountains (rubble)
        board.getTile(5, 5)?.terrain = TerrainType.MOUNTAIN
        board.getTile(7, 2)?.terrain = TerrainType.MOUNTAIN
        board.getTile(11, 6)?.terrain = TerrainType.MOUNTAIN

        // Forest patches
        board.getTile(1, 1)?.terrain = TerrainType.FOREST
        board.getTile(2, 1)?.terrain = TerrainType.FOREST
        board.getTile(6, 6)?.terrain = TerrainType.FOREST
        board.getTile(7, 6)?.terrain = TerrainType.FOREST

        // Narrow beach paths
        board.getTile(4, 7)?.terrain = TerrainType.VILLAGE
        board.getTile(9, 7)?.terrain = TerrainType.VILLAGE

        return board
    }

    /**
     * Create a dense dark forest with hidden paths
     */
    fun createDarkForest(): GameBoard {
        val board = GameBoard(12, 10)

        // Dense forest coverage
        for (x in 1..10) {
            for (y in 1..8) {
                board.getTile(x, y)?.terrain = TerrainType.FOREST
            }
        }

        // Clear paths through the forest
        for (y in 0..9) {
            board.getTile(3, y)?.terrain = TerrainType.PLAIN
            board.getTile(8, y)?.terrain = TerrainType.PLAIN
        }
        for (x in 3..8) {
            board.getTile(x, 5)?.terrain = TerrainType.PLAIN
        }

        // Hidden fort in center
        board.getTile(5, 4)?.terrain = TerrainType.FORT
        board.getTile(6, 4)?.terrain = TerrainType.FORT

        // Mountain blocking direct path
        board.getTile(5, 2)?.terrain = TerrainType.MOUNTAIN
        board.getTile(6, 7)?.terrain = TerrainType.MOUNTAIN

        return board
    }

    /**
     * Create a fortress interior - tight corridors and rooms
     */
    fun createFortressInterior(): GameBoard {
        val board = GameBoard(14, 12)

        // Outer walls (mountains)
        for (x in 0..13) {
            board.getTile(x, 0)?.terrain = TerrainType.MOUNTAIN
            board.getTile(x, 11)?.terrain = TerrainType.MOUNTAIN
        }
        for (y in 0..11) {
            board.getTile(0, y)?.terrain = TerrainType.MOUNTAIN
            board.getTile(13, y)?.terrain = TerrainType.MOUNTAIN
        }

        // Inner walls creating rooms and corridors
        for (y in 2..5) {
            board.getTile(4, y)?.terrain = TerrainType.MOUNTAIN
        }
        for (y in 6..9) {
            board.getTile(9, y)?.terrain = TerrainType.MOUNTAIN
        }
        for (x in 6..8) {
            board.getTile(x, 4)?.terrain = TerrainType.MOUNTAIN
            board.getTile(x, 7)?.terrain = TerrainType.MOUNTAIN
        }

        // Door gaps in walls
        board.getTile(4, 3)?.terrain = TerrainType.PLAIN
        board.getTile(9, 8)?.terrain = TerrainType.PLAIN
        board.getTile(7, 4)?.terrain = TerrainType.PLAIN
        board.getTile(7, 7)?.terrain = TerrainType.PLAIN

        // Fort tiles in strategic positions
        board.getTile(2, 2)?.terrain = TerrainType.FORT
        board.getTile(11, 2)?.terrain = TerrainType.FORT
        board.getTile(2, 9)?.terrain = TerrainType.FORT
        board.getTile(11, 9)?.terrain = TerrainType.FORT

        // Throne at the back
        board.getTile(11, 5)?.terrain = TerrainType.FORT
        board.getTile(12, 5)?.terrain = TerrainType.FORT

        return board
    }

    /**
     * Create a throne room - final battle arena
     */
    fun createThroneRoom(): GameBoard {
        val board = GameBoard(16, 12)

        // Walls surrounding the throne room
        for (x in 0..15) {
            board.getTile(x, 0)?.terrain = TerrainType.MOUNTAIN
            board.getTile(x, 11)?.terrain = TerrainType.MOUNTAIN
        }
        for (y in 0..11) {
            board.getTile(0, y)?.terrain = TerrainType.MOUNTAIN
            board.getTile(15, y)?.terrain = TerrainType.MOUNTAIN
        }

        // Pillars (mountains) creating a grand hall
        board.getTile(3, 3)?.terrain = TerrainType.MOUNTAIN
        board.getTile(3, 8)?.terrain = TerrainType.MOUNTAIN
        board.getTile(6, 3)?.terrain = TerrainType.MOUNTAIN
        board.getTile(6, 8)?.terrain = TerrainType.MOUNTAIN
        board.getTile(9, 3)?.terrain = TerrainType.MOUNTAIN
        board.getTile(9, 8)?.terrain = TerrainType.MOUNTAIN

        // Throne platform (forts)
        board.getTile(12, 4)?.terrain = TerrainType.FORT
        board.getTile(13, 4)?.terrain = TerrainType.FORT
        board.getTile(14, 4)?.terrain = TerrainType.FORT
        board.getTile(12, 5)?.terrain = TerrainType.FORT
        board.getTile(13, 5)?.terrain = TerrainType.FORT
        board.getTile(14, 5)?.terrain = TerrainType.FORT
        board.getTile(12, 6)?.terrain = TerrainType.FORT
        board.getTile(13, 6)?.terrain = TerrainType.FORT
        board.getTile(14, 6)?.terrain = TerrainType.FORT

        // Entrance hall (main entrance at left)
        board.getTile(0, 5)?.terrain = TerrainType.PLAIN
        board.getTile(0, 6)?.terrain = TerrainType.PLAIN

        return board
    }

    /**
     * Create a desert outpost - arid landscape with scattered fortifications
     */
    fun createDesertOutpost(): GameBoard {
        val board = GameBoard(14, 10)

        // Sandy terrain is plain by default

        // Supply depot (forts)
        board.getTile(10, 3)?.terrain = TerrainType.FORT
        board.getTile(11, 3)?.terrain = TerrainType.FORT
        board.getTile(10, 4)?.terrain = TerrainType.FORT
        board.getTile(11, 4)?.terrain = TerrainType.FORT

        // Rocky outcrops (mountains)
        board.getTile(4, 2)?.terrain = TerrainType.MOUNTAIN
        board.getTile(5, 2)?.terrain = TerrainType.MOUNTAIN
        board.getTile(7, 7)?.terrain = TerrainType.MOUNTAIN
        board.getTile(8, 7)?.terrain = TerrainType.MOUNTAIN

        // Oasis (water + village)
        board.getTile(6, 4)?.terrain = TerrainType.WATER
        board.getTile(6, 5)?.terrain = TerrainType.WATER
        board.getTile(5, 4)?.terrain = TerrainType.VILLAGE
        board.getTile(7, 5)?.terrain = TerrainType.VILLAGE

        // Sparse desert vegetation (forest)
        board.getTile(2, 6)?.terrain = TerrainType.FOREST
        board.getTile(12, 7)?.terrain = TerrainType.FOREST
        board.getTile(3, 1)?.terrain = TerrainType.FOREST

        return board
    }

    /**
     * Create a frozen mountain pass - icy terrain with narrow pathways
     */
    fun createFrozenPass(): GameBoard {
        val board = GameBoard(14, 10)

        // Mountain walls forming a winding pass
        for (y in 0..9) {
            board.getTile(0, y)?.terrain = TerrainType.MOUNTAIN
            board.getTile(13, y)?.terrain = TerrainType.MOUNTAIN
        }
        for (x in 0..4) {
            board.getTile(x, 0)?.terrain = TerrainType.MOUNTAIN
            board.getTile(x, 9)?.terrain = TerrainType.MOUNTAIN
        }
        for (x in 9..13) {
            board.getTile(x, 0)?.terrain = TerrainType.MOUNTAIN
            board.getTile(x, 9)?.terrain = TerrainType.MOUNTAIN
        }

        // Inner mountain formations creating narrow paths
        board.getTile(3, 3)?.terrain = TerrainType.MOUNTAIN
        board.getTile(3, 4)?.terrain = TerrainType.MOUNTAIN
        board.getTile(4, 6)?.terrain = TerrainType.MOUNTAIN
        board.getTile(4, 7)?.terrain = TerrainType.MOUNTAIN
        board.getTile(9, 3)?.terrain = TerrainType.MOUNTAIN
        board.getTile(9, 4)?.terrain = TerrainType.MOUNTAIN
        board.getTile(10, 6)?.terrain = TerrainType.MOUNTAIN
        board.getTile(10, 7)?.terrain = TerrainType.MOUNTAIN

        // Frozen lake (water)
        board.getTile(6, 4)?.terrain = TerrainType.WATER
        board.getTile(7, 4)?.terrain = TerrainType.WATER
        board.getTile(6, 5)?.terrain = TerrainType.WATER
        board.getTile(7, 5)?.terrain = TerrainType.WATER

        // Sheltered spots (forts)
        board.getTile(2, 5)?.terrain = TerrainType.FORT
        board.getTile(11, 5)?.terrain = TerrainType.FORT

        return board
    }

    /**
     * Create the Crimson Capital streets - urban layout with buildings and alleys
     */
    fun createCrimsonCapital(): GameBoard {
        val board = GameBoard(16, 10)

        // Building blocks (mountains - impassable)
        for (y in 1..3) {
            board.getTile(3, y)?.terrain = TerrainType.MOUNTAIN
            board.getTile(4, y)?.terrain = TerrainType.MOUNTAIN
        }
        for (y in 6..8) {
            board.getTile(3, y)?.terrain = TerrainType.MOUNTAIN
            board.getTile(4, y)?.terrain = TerrainType.MOUNTAIN
        }
        for (y in 1..3) {
            board.getTile(8, y)?.terrain = TerrainType.MOUNTAIN
            board.getTile(9, y)?.terrain = TerrainType.MOUNTAIN
        }
        for (y in 6..8) {
            board.getTile(8, y)?.terrain = TerrainType.MOUNTAIN
            board.getTile(9, y)?.terrain = TerrainType.MOUNTAIN
        }

        // Guard posts (forts)
        board.getTile(6, 2)?.terrain = TerrainType.FORT
        board.getTile(6, 7)?.terrain = TerrainType.FORT
        board.getTile(12, 5)?.terrain = TerrainType.FORT
        board.getTile(14, 5)?.terrain = TerrainType.FORT

        // Market stalls (villages)
        board.getTile(6, 4)?.terrain = TerrainType.VILLAGE
        board.getTile(6, 5)?.terrain = TerrainType.VILLAGE

        // Decorative trees (forest)
        board.getTile(11, 2)?.terrain = TerrainType.FOREST
        board.getTile(11, 7)?.terrain = TerrainType.FOREST

        return board
    }

    /**
     * Create the Imperial Palace interior - grand halls with rooms and corridors
     */
    fun createImperialPalace(): GameBoard {
        val board = GameBoard(16, 10)

        // Palace walls (mountains forming rooms)
        for (x in 0..15) {
            board.getTile(x, 0)?.terrain = TerrainType.MOUNTAIN
            board.getTile(x, 9)?.terrain = TerrainType.MOUNTAIN
        }
        for (y in 0..9) {
            board.getTile(0, y)?.terrain = TerrainType.MOUNTAIN
        }

        // Internal walls creating corridors
        for (y in 0..3) {
            board.getTile(5, y)?.terrain = TerrainType.MOUNTAIN
        }
        for (y in 6..9) {
            board.getTile(5, y)?.terrain = TerrainType.MOUNTAIN
        }
        for (y in 2..7) {
            board.getTile(10, y)?.terrain = TerrainType.MOUNTAIN
        }

        // Door gaps
        board.getTile(5, 4)?.terrain = TerrainType.PLAIN
        board.getTile(5, 5)?.terrain = TerrainType.PLAIN
        board.getTile(10, 4)?.terrain = TerrainType.PLAIN
        board.getTile(10, 5)?.terrain = TerrainType.PLAIN

        // Chamber forts (defensive positions)
        board.getTile(3, 4)?.terrain = TerrainType.FORT
        board.getTile(3, 5)?.terrain = TerrainType.FORT
        board.getTile(8, 4)?.terrain = TerrainType.FORT
        board.getTile(8, 5)?.terrain = TerrainType.FORT
        board.getTile(13, 5)?.terrain = TerrainType.FORT
        board.getTile(14, 5)?.terrain = TerrainType.FORT

        // Exit to inner chambers
        board.getTile(15, 5)?.terrain = TerrainType.VILLAGE
        board.getTile(15, 6)?.terrain = TerrainType.VILLAGE

        return board
    }

    /**
     * Create a volcanic dragon lair - dramatic final battle arena
     */
    fun createDragonLair(): GameBoard {
        val board = GameBoard(16, 10)

        // Lava pools (water)
        board.getTile(3, 1)?.terrain = TerrainType.WATER
        board.getTile(4, 1)?.terrain = TerrainType.WATER
        board.getTile(3, 8)?.terrain = TerrainType.WATER
        board.getTile(4, 8)?.terrain = TerrainType.WATER
        board.getTile(11, 1)?.terrain = TerrainType.WATER
        board.getTile(12, 1)?.terrain = TerrainType.WATER
        board.getTile(11, 8)?.terrain = TerrainType.WATER
        board.getTile(12, 8)?.terrain = TerrainType.WATER

        // Rock formations (mountains)
        board.getTile(5, 3)?.terrain = TerrainType.MOUNTAIN
        board.getTile(5, 7)?.terrain = TerrainType.MOUNTAIN
        board.getTile(9, 2)?.terrain = TerrainType.MOUNTAIN
        board.getTile(9, 8)?.terrain = TerrainType.MOUNTAIN

        // Volcanic pillars creating cover
        board.getTile(3, 4)?.terrain = TerrainType.MOUNTAIN
        board.getTile(3, 6)?.terrain = TerrainType.MOUNTAIN
        board.getTile(11, 3)?.terrain = TerrainType.MOUNTAIN
        board.getTile(11, 7)?.terrain = TerrainType.MOUNTAIN

        // Ancient platforms (forts)
        board.getTile(7, 4)?.terrain = TerrainType.FORT
        board.getTile(7, 5)?.terrain = TerrainType.FORT
        board.getTile(7, 6)?.terrain = TerrainType.FORT
        board.getTile(13, 4)?.terrain = TerrainType.FORT
        board.getTile(14, 5)?.terrain = TerrainType.FORT
        board.getTile(13, 6)?.terrain = TerrainType.FORT

        return board
    }
}
