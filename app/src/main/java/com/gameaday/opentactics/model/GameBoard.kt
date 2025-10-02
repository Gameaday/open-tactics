package com.gameaday.opentactics.model

data class GameBoard(
    val width: Int,
    val height: Int,
) {
    private val tiles: Array<Array<Tile>> =
        Array(height) { y ->
            Array(width) { x ->
                Tile(Position(x, y), TerrainType.PLAIN)
            }
        }

    fun getTile(position: Position): Tile? =
        if (isValidPosition(position)) {
            tiles[position.y][position.x]
        } else {
            null
        }

    fun getTile(
        x: Int,
        y: Int,
    ): Tile? = getTile(Position(x, y))

    fun isValidPosition(position: Position): Boolean = position.x in 0 until width && position.y in 0 until height

    fun getCharacterAt(position: Position): Character? = getTile(position)?.occupant

    fun moveCharacter(
        character: Character,
        newPosition: Position,
    ): Boolean {
        val currentTile = getTile(character.position)
        val newTile = getTile(newPosition)

        if (currentTile == null || newTile == null) return false
        if (!newTile.canBeOccupiedBy(character)) return false

        // Remove from current position
        currentTile.occupant = null

        // Place at new position
        newTile.occupant = character
        character.position = newPosition

        return true
    }

    fun placeCharacter(
        character: Character,
        position: Position,
    ): Boolean {
        val tile = getTile(position) ?: return false
        if (!tile.canBeOccupiedBy(character)) return false

        tile.occupant = character
        character.position = position
        return true
    }

    fun removeCharacter(character: Character) {
        val tile = getTile(character.position)
        tile?.occupant = null
    }

    fun getAllCharacters(): List<Character> {
        val characters = mutableListOf<Character>()
        for (y in 0 until height) {
            for (x in 0 until width) {
                tiles[y][x].occupant?.let { characters.add(it) }
            }
        }
        return characters
    }

    fun getCharactersByTeam(team: Team): List<Character> = getAllCharacters().filter { it.team == team }

    // Create a simple test map
    companion object {
        // Test map dimensions and terrain positions
        private const val TEST_MAP_WIDTH = 12
        private const val TEST_MAP_HEIGHT = 8
        private const val FOREST_X1 = 2
        private const val FOREST_Y1 = 2
        private const val FOREST_X2 = 3
        private const val MOUNTAIN_X1 = 8
        private const val MOUNTAIN_Y1 = 4
        private const val MOUNTAIN_X2 = 9
        private const val FORT_X = 5
        private const val FORT_Y = 1

        fun createTestMap(): GameBoard {
            val board = GameBoard(TEST_MAP_WIDTH, TEST_MAP_HEIGHT)

            // Add some terrain variety
            board.getTile(FOREST_X1, FOREST_Y1)?.terrain = TerrainType.FOREST
            board.getTile(FOREST_X2, FOREST_Y1)?.terrain = TerrainType.FOREST
            board.getTile(MOUNTAIN_X1, MOUNTAIN_Y1)?.terrain = TerrainType.MOUNTAIN
            board.getTile(MOUNTAIN_X2, MOUNTAIN_Y1)?.terrain = TerrainType.MOUNTAIN
            board.getTile(FORT_X, FORT_Y)?.terrain = TerrainType.FORT

            return board
        }
    }
}
