package com.gameaday.opentactics.model

data class GameBoard(
    val width: Int,
    val height: Int
) {
    private val tiles: Array<Array<Tile>> = Array(height) { y ->
        Array(width) { x ->
            Tile(Position(x, y), TerrainType.PLAIN)
        }
    }

    fun getTile(position: Position): Tile? {
        return if (isValidPosition(position)) {
            tiles[position.y][position.x]
        } else null
    }

    fun getTile(x: Int, y: Int): Tile? {
        return getTile(Position(x, y))
    }

    fun isValidPosition(position: Position): Boolean {
        return position.x in 0 until width && position.y in 0 until height
    }

    fun getCharacterAt(position: Position): Character? {
        return getTile(position)?.occupant
    }

    fun moveCharacter(character: Character, newPosition: Position): Boolean {
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

    fun placeCharacter(character: Character, position: Position): Boolean {
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

    fun getCharactersByTeam(team: Team): List<Character> {
        return getAllCharacters().filter { it.team == team }
    }

    // Create a simple test map
    companion object {
        fun createTestMap(): GameBoard {
            val board = GameBoard(12, 8)
            
            // Add some terrain variety
            board.getTile(2, 2)?.terrain = TerrainType.FOREST
            board.getTile(3, 2)?.terrain = TerrainType.FOREST
            board.getTile(8, 4)?.terrain = TerrainType.MOUNTAIN
            board.getTile(9, 4)?.terrain = TerrainType.MOUNTAIN
            board.getTile(5, 1)?.terrain = TerrainType.FORT
            
            return board
        }
    }
}