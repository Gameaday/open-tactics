package com.gameaday.opentactics.game

import com.gameaday.opentactics.model.Character
import com.gameaday.opentactics.model.CharacterClass
import com.gameaday.opentactics.model.GameBoard
import com.gameaday.opentactics.model.Position
import com.gameaday.opentactics.model.Team
import com.gameaday.opentactics.model.Weapon
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GameStateBugFixTest {
    private lateinit var gameState: GameState
    private lateinit var gameBoard: GameBoard

    @Before
    fun setUp() {
        gameBoard = GameBoard(8, 8)
        gameState = GameState(gameBoard)
    }

    @Test
    fun testDefeatedCharacterRemovedFromBoard() {
        val player =
            Character(
                id = "player1",
                name = "Hero",
                characterClass = CharacterClass.KNIGHT,
                team = Team.PLAYER,
                position = Position(1, 1),
            )
        player.addWeapon(Weapon.ironSword())

        val enemy =
            Character(
                id = "enemy1",
                name = "Goblin",
                characterClass = CharacterClass.KNIGHT,
                team = Team.ENEMY,
                position = Position(2, 1),
            )
        enemy.addWeapon(Weapon.ironSword())

        gameState.addPlayerCharacter(player)
        gameState.addEnemyCharacter(enemy)
        gameBoard.placeCharacter(player, player.position)
        gameBoard.placeCharacter(enemy, enemy.position)

        // Kill the enemy by setting HP to 1 and attacking
        enemy.takeDamage(enemy.currentHp - 1)
        assertEquals(1, enemy.currentHp)

        // Perform attack which should kill the enemy
        gameState.selectCharacter(player)
        val result = gameState.performAttack(player, enemy)

        // Enemy should be removed from the board tile
        assertNull(
            "Defeated character should be removed from the board",
            gameBoard.getCharacterAt(Position(2, 1)),
        )
    }

    @Test
    fun testEndTurnSeparatesPlayerAndEnemyPhases() {
        // End turn from PLAYER should only switch to ENEMY
        assertEquals(Team.PLAYER, gameState.currentTeam)
        gameState.endTurn()
        assertEquals(Team.ENEMY, gameState.currentTeam)

        // End turn from ENEMY should switch back to PLAYER
        gameState.endTurn()
        assertEquals(Team.PLAYER, gameState.currentTeam)
    }

    @Test
    fun testEndTurnIncrementsCounterOnlyOnPlayerToEnemy() {
        assertEquals(0, gameState.turnCounter)
        gameState.endTurn() // Player -> Enemy
        assertEquals(1, gameState.turnCounter)
        gameState.endTurn() // Enemy -> Player
        assertEquals(1, gameState.turnCounter) // Not incremented again
    }

    @Test
    fun testExecuteAllEnemyActionsDoesNotChangePhase() {
        val enemy =
            Character(
                id = "enemy1",
                name = "Enemy",
                characterClass = CharacterClass.KNIGHT,
                team = Team.ENEMY,
                position = Position(5, 5),
            )
        enemy.addWeapon(Weapon.ironSword())

        gameState.addEnemyCharacter(enemy)
        gameBoard.placeCharacter(enemy, enemy.position)

        // Switch to enemy turn
        gameState.endTurn()
        assertEquals(Team.ENEMY, gameState.currentTeam)

        // Execute enemy actions should not auto-switch turns
        gameState.executeAllEnemyActions()
        assertEquals(Team.ENEMY, gameState.currentTeam)

        // Still need to call endTurn() to switch back
        gameState.endTurn()
        assertEquals(Team.PLAYER, gameState.currentTeam)
    }

    @Test
    fun testEndTurnResetsPlayerActions() {
        val player =
            Character(
                id = "player1",
                name = "Hero",
                characterClass = CharacterClass.KNIGHT,
                team = Team.PLAYER,
                position = Position(1, 1),
            )
        gameState.addPlayerCharacter(player)
        gameBoard.placeCharacter(player, player.position)

        player.hasActedThisTurn = true
        player.hasMovedThisTurn = true
        assertFalse(player.canAct)

        // End player turn - should reset actions
        gameState.endTurn()
        assertTrue(player.canAct)
        assertTrue(player.canMove)
    }

    @Test
    fun testEndTurnResetsEnemyActions() {
        val enemy =
            Character(
                id = "enemy1",
                name = "Goblin",
                characterClass = CharacterClass.KNIGHT,
                team = Team.ENEMY,
                position = Position(5, 5),
            )
        gameState.addEnemyCharacter(enemy)
        gameBoard.placeCharacter(enemy, enemy.position)

        // Switch to enemy turn
        gameState.endTurn()

        enemy.hasActedThisTurn = true
        assertFalse(enemy.canAct)

        // End enemy turn - should reset actions
        gameState.endTurn()
        assertTrue(enemy.canAct)
    }
}
