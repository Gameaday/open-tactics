package com.gameaday.opentactics.game

import com.gameaday.opentactics.model.Character
import com.gameaday.opentactics.model.CharacterClass
import com.gameaday.opentactics.model.GameBoard
import com.gameaday.opentactics.model.Position
import com.gameaday.opentactics.model.Team
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GameStateTest {
    private lateinit var gameState: GameState
    private lateinit var gameBoard: GameBoard

    @Before
    fun setUp() {
        gameBoard = GameBoard(8, 8)
        gameState = GameState(gameBoard)
    }

    @Test
    fun testGameStateInitialization() {
        assertEquals(gameBoard, gameState.board)
        assertEquals(0, gameState.turnCounter)
        assertEquals(Team.PLAYER, gameState.currentTeam)
        assertFalse(gameState.isGameOver)
    }

    @Test
    fun testAddPlayerCharacter() {
        val character =
            Character(
                id = "player1",
                name = "Hero",
                characterClass = CharacterClass.KNIGHT,
                team = Team.PLAYER,
                position = Position(1, 1),
            )

        gameState.addPlayerCharacter(character)
        val playerCharacters = gameState.getPlayerCharacters()

        assertEquals(1, playerCharacters.size)
        assertTrue(playerCharacters.contains(character))
    }

    @Test
    fun testAddEnemyCharacter() {
        val character =
            Character(
                id = "enemy1",
                name = "Villain",
                characterClass = CharacterClass.ARCHER,
                team = Team.ENEMY,
                position = Position(6, 6),
            )

        gameState.addEnemyCharacter(character)
        val enemyCharacters = gameState.getEnemyCharacters()

        assertEquals(1, enemyCharacters.size)
        assertTrue(enemyCharacters.contains(character))
    }

    @Test
    fun testGetAliveCharacters() {
        val aliveCharacter =
            Character(
                id = "alive",
                name = "Alive",
                characterClass = CharacterClass.KNIGHT,
                team = Team.PLAYER,
                position = Position(1, 1),
            )

        val deadCharacter =
            Character(
                id = "dead",
                name = "Dead",
                characterClass = CharacterClass.ARCHER,
                team = Team.PLAYER,
                position = Position(2, 2),
            )
        deadCharacter.takeDamage(1000) // Kill the character

        gameState.addPlayerCharacter(aliveCharacter)
        gameState.addPlayerCharacter(deadCharacter)

        val aliveCharacters = gameState.getAlivePlayerCharacters()
        assertEquals(1, aliveCharacters.size)
        assertTrue(aliveCharacters.contains(aliveCharacter))
        assertFalse(aliveCharacters.contains(deadCharacter))
    }

    @Test
    fun testCurrentPlayerCharacter() {
        assertNull(gameState.currentPlayerCharacter)

        val character =
            Character(
                id = "current",
                name = "Current",
                characterClass = CharacterClass.KNIGHT,
                team = Team.PLAYER,
                position = Position(1, 1),
            )

        gameState.addPlayerCharacter(character)
        gameState.selectCharacter(character)

        assertEquals(character, gameState.currentPlayerCharacter)
    }

    @Test
    fun testSelectCharacter() {
        val character =
            Character(
                id = "selectable",
                name = "Selectable",
                characterClass = CharacterClass.KNIGHT,
                team = Team.PLAYER,
                position = Position(1, 1),
            )

        gameState.addPlayerCharacter(character)
        assertTrue(gameState.selectCharacter(character))
        assertEquals(character, gameState.currentPlayerCharacter)
    }

    @Test
    fun testCannotSelectEnemyCharacter() {
        val enemyCharacter =
            Character(
                id = "enemy",
                name = "Enemy",
                characterClass = CharacterClass.ARCHER,
                team = Team.ENEMY,
                position = Position(6, 6),
            )

        gameState.addEnemyCharacter(enemyCharacter)
        assertFalse(gameState.selectCharacter(enemyCharacter))
        assertNull(gameState.currentPlayerCharacter)
    }

    @Test
    fun testGameOverConditions() {
        assertFalse(gameState.isGameWon())
        assertFalse(gameState.isGameLost())
        assertFalse(gameState.isGameOver)

        // Add a player character
        val player =
            Character(
                id = "player",
                name = "Player",
                characterClass = CharacterClass.KNIGHT,
                team = Team.PLAYER,
                position = Position(1, 1),
            )
        gameState.addPlayerCharacter(player)

        // Add an enemy character
        val enemy =
            Character(
                id = "enemy",
                name = "Enemy",
                characterClass = CharacterClass.ARCHER,
                team = Team.ENEMY,
                position = Position(6, 6),
            )
        gameState.addEnemyCharacter(enemy)

        // Kill all enemies - should win
        enemy.takeDamage(1000)
        assertTrue(gameState.isGameWon())
        assertTrue(gameState.isGameOver)

        // Reset and kill all players - should lose
        val newGameState = GameState(gameBoard)
        newGameState.addPlayerCharacter(player)
        newGameState.addEnemyCharacter(enemy)
        player.takeDamage(1000)

        assertTrue(newGameState.isGameLost())
        assertTrue(newGameState.isGameOver)
    }

    @Test
    fun testEndTurn() {
        assertEquals(Team.PLAYER, gameState.currentTeam)
        assertEquals(0, gameState.turnCounter)

        // endTurn() from PLAYER switches to ENEMY, executes enemy turn (which is instant if no enemies),
        // then automatically switches back to PLAYER with incremented turn counter
        gameState.endTurn()
        assertEquals(Team.PLAYER, gameState.currentTeam) // Back to PLAYER after empty enemy turn
        assertEquals(1, gameState.turnCounter) // Turn counter incremented

        gameState.endTurn()
        assertEquals(Team.PLAYER, gameState.currentTeam)
        assertEquals(2, gameState.turnCounter)
    }

    @Test
    fun testCharacterActionTracking() {
        val character =
            Character(
                id = "actor",
                name = "Actor",
                characterClass = CharacterClass.KNIGHT,
                team = Team.PLAYER,
                position = Position(1, 1),
            )

        gameState.addPlayerCharacter(character)
        assertTrue(character.canAct)

        // Simulate using action
        character.hasActed = true
        assertFalse(character.canAct)

        // End turn should reset actions
        gameState.endTurn() // Switch to enemy
        gameState.endTurn() // Back to player, should reset actions

        assertTrue(character.canAct)
    }
}
