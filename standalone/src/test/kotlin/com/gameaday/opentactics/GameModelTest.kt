package com.gameaday.opentactics

import com.gameaday.opentactics.model.Character
import com.gameaday.opentactics.model.CharacterClass
import com.gameaday.opentactics.model.GameBoard
import com.gameaday.opentactics.model.Position
import com.gameaday.opentactics.model.Team
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class GameModelTest {
    @Test
    fun testPositionDistance() {
        val pos1 = Position(0, 0)
        val pos2 = Position(3, 4)
        assertEquals(7, pos1.distanceTo(pos2))
    }

    @Test
    fun testPositionAdjacency() {
        val pos1 = Position(1, 1)
        val pos2 = Position(1, 2)
        val pos3 = Position(2, 2)

        assertTrue(pos1.isAdjacentTo(pos2))
        assertFalse(pos1.isAdjacentTo(pos3))
    }

    @Test
    fun testCharacterStats() {
        val knight =
            Character(
                id = "test_knight",
                name = "Test Knight",
                characterClass = CharacterClass.KNIGHT,
                team = Team.PLAYER,
                position = Position(0, 0),
            )

        assertEquals(CharacterClass.KNIGHT.baseStats.hp, knight.currentStats.hp)
        assertEquals(knight.currentStats.hp, knight.currentHp)
        assertTrue(knight.isAlive)
        assertTrue(knight.canAct)
        assertTrue(knight.canMove)
    }

    @Test
    fun testCharacterDamage() {
        val character =
            Character(
                id = "test",
                name = "Test",
                characterClass = CharacterClass.KNIGHT,
                team = Team.PLAYER,
                position = Position(0, 0),
            )

        val initialHp = character.currentHp
        character.takeDamage(10)
        assertEquals(initialHp - 10, character.currentHp)

        character.takeDamage(100) // Should not go below 0
        assertEquals(0, character.currentHp)
        assertFalse(character.isAlive)
    }

    @Test
    fun testGameBoard() {
        val board = GameBoard(5, 5)

        assertTrue(board.isValidPosition(Position(0, 0)))
        assertTrue(board.isValidPosition(Position(4, 4)))
        assertFalse(board.isValidPosition(Position(-1, 0)))
        assertFalse(board.isValidPosition(Position(5, 0)))

        val character =
            Character(
                id = "test",
                name = "Test",
                characterClass = CharacterClass.KNIGHT,
                team = Team.PLAYER,
                position = Position(2, 2),
            )

        assertTrue(board.placeCharacter(character, Position(2, 2)))
        assertEquals(character, board.getCharacterAt(Position(2, 2)))

        assertTrue(board.moveCharacter(character, Position(3, 3)))
        assertNull(board.getCharacterAt(Position(2, 2)))
        assertEquals(character, board.getCharacterAt(Position(3, 3)))
    }
}
