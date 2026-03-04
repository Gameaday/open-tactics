package com.gameaday.opentactics.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Tests for character action state tracking, used by
 * GameBoardView to dim acted units and by GameActivity
 * for undo/action flow.
 */
class CharacterActionStateTest {
    private lateinit var playerKnight: Character
    private lateinit var playerArcher: Character
    private lateinit var enemyMage: Character

    @Before
    fun setUp() {
        playerKnight =
            Character(
                id = "test_knight",
                name = "Test Knight",
                characterClass = CharacterClass.KNIGHT,
                team = Team.PLAYER,
                position = Position(0, 0),
            )
        playerArcher =
            Character(
                id = "test_archer",
                name = "Test Archer",
                characterClass = CharacterClass.ARCHER,
                team = Team.PLAYER,
                position = Position(1, 0),
            )
        enemyMage =
            Character(
                id = "test_enemy",
                name = "Enemy Mage",
                characterClass = CharacterClass.MAGE,
                team = Team.ENEMY,
                position = Position(5, 5),
            )
    }

    @Test
    fun testNewCharacterHasNotActed() {
        assertFalse(playerKnight.hasActedThisTurn)
        assertFalse(playerKnight.hasMovedThisTurn)
        assertTrue(playerKnight.canMove)
        assertTrue(playerKnight.canAct)
    }

    @Test
    fun testCharacterActedStateAfterMoving() {
        playerKnight.hasMovedThisTurn = true

        assertTrue(playerKnight.hasMovedThisTurn)
        assertFalse(playerKnight.hasActedThisTurn)
        assertFalse(playerKnight.canMove)
        // Can still act after moving (not yet attacked/waited)
        assertTrue(playerKnight.canAct)
    }

    @Test
    fun testCharacterFullyActedState() {
        playerKnight.hasMovedThisTurn = true
        playerKnight.hasActedThisTurn = true

        assertFalse(playerKnight.canMove)
        assertFalse(playerKnight.canAct)
    }

    @Test
    fun testResetTurnClearsActionState() {
        playerKnight.hasMovedThisTurn = true
        playerKnight.hasActedThisTurn = true

        playerKnight.resetTurn()

        assertFalse(playerKnight.hasMovedThisTurn)
        assertFalse(playerKnight.hasActedThisTurn)
        assertTrue(playerKnight.canMove)
        assertTrue(playerKnight.canAct)
    }

    @Test
    fun testEnemyActionStateTracked() {
        // Enemy characters should also track action state
        assertFalse(enemyMage.hasActedThisTurn)

        enemyMage.hasActedThisTurn = true
        assertTrue(enemyMage.hasActedThisTurn)
        assertFalse(enemyMage.canAct)
    }

    @Test
    fun testPositionDistanceCalculation() {
        // Used by GameActivity for trade adjacency check (distanceTo == 1)
        val pos1 = Position(2, 3)
        val pos2 = Position(2, 4) // adjacent
        val pos3 = Position(4, 3) // distance 2

        assertEquals(1, pos1.distanceTo(pos2))
        assertEquals(2, pos1.distanceTo(pos3))
        assertEquals(0, pos1.distanceTo(pos1))
    }

    @Test
    fun testAllCharacterClassesHaveDisplayName() {
        // Ensures fallback text rendering in GameBoardView has a valid initial
        CharacterClass.entries.forEach { charClass ->
            assertTrue(
                "CharacterClass ${charClass.name} must have a non-empty display name",
                charClass.displayName.isNotEmpty(),
            )
        }
    }

    @Test
    fun testPegasusKnightAndWyvernRiderClassesExist() {
        // These classes must exist since we now have icons for them
        val pegasus = CharacterClass.PEGASUS_KNIGHT
        val wyvern = CharacterClass.WYVERN_RIDER

        assertTrue(pegasus.displayName.isNotEmpty())
        assertTrue(wyvern.displayName.isNotEmpty())
        assertTrue(pegasus.canFly)
        assertTrue(wyvern.canFly)
    }
}
