package com.gameaday.opentactics.model

import com.gameaday.opentactics.game.GameState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ActionSystemTest {
    private lateinit var board: GameBoard
    private lateinit var gameState: GameState
    private lateinit var knight: Character
    private lateinit var pegasusKnight: Character
    private lateinit var archer: Character
    private lateinit var enemy: Character

    @Before
    fun setUp() {
        board = GameBoard.createTestMap()
        gameState = GameState(board)

        knight =
            Character(
                id = "knight1",
                name = "Test Knight",
                characterClass = CharacterClass.KNIGHT,
                team = Team.PLAYER,
                position = Position(1, 1),
            )

        pegasusKnight =
            Character(
                id = "pegasus1",
                name = "Test Pegasus",
                characterClass = CharacterClass.PEGASUS_KNIGHT,
                team = Team.PLAYER,
                position = Position(5, 5),
            )

        archer =
            Character(
                id = "archer1",
                name = "Test Archer",
                characterClass = CharacterClass.ARCHER,
                team = Team.PLAYER,
                position = Position(3, 3),
            )

        enemy =
            Character(
                id = "enemy1",
                name = "Enemy",
                characterClass = CharacterClass.KNIGHT,
                team = Team.ENEMY,
                position = Position(2, 2),
            )

        gameState.addPlayerCharacter(knight)
        gameState.addPlayerCharacter(pegasusKnight)
        gameState.addPlayerCharacter(archer)
        gameState.addEnemyCharacter(enemy)

        board.placeCharacter(knight, knight.position)
        board.placeCharacter(pegasusKnight, pegasusKnight.position)
        board.placeCharacter(archer, archer.position)
        board.placeCharacter(enemy, enemy.position)
    }

    @Test
    fun testMountedUnitHasCanto() {
        assertTrue("Knight should have Canto", knight.characterClass.hasCanto)
        assertTrue("Pegasus Knight should have Canto", pegasusKnight.characterClass.hasCanto)
        assertFalse("Archer should not have Canto", archer.characterClass.hasCanto)
    }

    @Test
    fun testMoveAttackWaitFlow() {
        gameState.selectCharacter(archer)

        // Archer can move initially
        assertTrue(archer.canMoveNow())
        assertFalse(archer.hasMovedThisTurn)
        assertFalse(archer.hasActedThisTurn)

        // Perform move
        val moveResult = gameState.performMove(archer, Position(4, 3))
        assertTrue("Move should succeed", moveResult)
        assertEquals(Position(4, 3), archer.position)
        assertTrue(archer.hasMovedThisTurn)
        assertFalse(archer.hasActedThisTurn)

        // Archer cannot move again (no Canto)
        assertFalse(archer.canMoveNow())

        // Commit wait
        gameState.performWait()
        assertTrue(archer.hasActedThisTurn)
        assertTrue(archer.hasMovedThisTurn)
    }

    @Test
    fun testCantoMoveAttackMove() {
        // Add weapons for combat
        knight.addWeapon(Weapon.ironSword())
        enemy.addWeapon(Weapon.ironSword())

        gameState.selectCharacter(knight)

        // Knight moves closer to enemy
        val movePos = Position(2, 1)
        assertTrue(gameState.performMove(knight, movePos))
        assertEquals(movePos, knight.position)
        assertTrue(knight.hasMovedThisTurn)
        assertFalse(knight.hasActedThisTurn)

        // Knight can attack after moving
        assertTrue(knight.canAct)
        val battleResult = gameState.performPlayerAttack(enemy)
        assertNotNull("Attack should succeed", battleResult)
        assertTrue(knight.hasActedThisTurn)

        // Knight with Canto can move again after attacking
        assertTrue("Knight should be able to move after attack (Canto)", knight.canStillMoveAfterAttack)
        assertTrue(knight.canMoveNow())

        // Perform Canto movement
        val cantoPos = Position(3, 1)
        assertTrue(gameState.performMove(knight, cantoPos))
        assertEquals(cantoPos, knight.position)
    }

    @Test
    fun testCantoAttackMove() {
        // Add weapons
        pegasusKnight.addWeapon(Weapon.ironLance())

        // Position Pegasus next to enemy for immediate attack
        pegasusKnight.position = Position(3, 2)
        board.placeCharacter(pegasusKnight, pegasusKnight.position)

        gameState.selectCharacter(pegasusKnight)

        // Attack without moving first
        assertFalse(pegasusKnight.hasMovedThisTurn)
        val battleResult = gameState.performPlayerAttack(enemy)
        assertNotNull("Attack should succeed", battleResult)
        assertTrue(pegasusKnight.hasActedThisTurn)

        // Pegasus can move after attacking (Canto)
        assertTrue("Pegasus should be able to move after attack", pegasusKnight.canStillMoveAfterAttack)
        assertTrue(pegasusKnight.canMoveNow())

        // Perform Canto movement
        val cantoPos = Position(4, 2)
        assertTrue(gameState.performMove(pegasusKnight, cantoPos))
        assertEquals(cantoPos, pegasusKnight.position)
    }

    @Test
    fun testUndoMove() {
        gameState.selectCharacter(archer)

        val originalPos = archer.position
        val newPos = Position(4, 3)

        // Move archer
        assertTrue(gameState.performMove(archer, newPos))
        assertEquals(newPos, archer.position)
        assertTrue(archer.hasMovedThisTurn)

        // Undo the move
        assertTrue(gameState.undoMove())
        assertEquals(originalPos, archer.position)
        assertFalse(archer.hasMovedThisTurn)
        assertNull(archer.previousPosition)
    }

    @Test
    fun testCannotUndoAfterAction() {
        knight.addWeapon(Weapon.ironSword())
        enemy.addWeapon(Weapon.ironSword())

        gameState.selectCharacter(knight)

        // Move knight
        val originalPos = knight.position
        val movePos = Position(2, 1)
        assertTrue(gameState.performMove(knight, movePos))

        // Attack enemy
        gameState.performPlayerAttack(enemy)
        assertTrue(knight.hasActedThisTurn)

        // Cannot undo move after attacking
        assertFalse(gameState.undoMove())
        assertEquals(movePos, knight.position)
    }

    @Test
    fun testNonCantoUnitCannotMoveAfterAttack() {
        // Archer doesn't have Canto
        assertFalse("Archer should not have Canto", archer.characterClass.hasCanto)

        // Manually set archer to hasActedThisTurn to simulate having attacked
        archer.hasActedThisTurn = false
        archer.hasMovedThisTurn = false
        archer.commitAction()

        // After attacking, non-Canto units cannot move
        assertFalse("Archer should not be able to move after attack", archer.canStillMoveAfterAttack)
        assertFalse("Archer canMoveNow should be false", archer.canMoveNow())
    }

    @Test
    fun testCommitWait() {
        gameState.selectCharacter(archer)

        assertFalse(archer.hasMovedThisTurn)
        assertFalse(archer.hasActedThisTurn)

        gameState.performWait()

        assertTrue(archer.hasMovedThisTurn)
        assertTrue(archer.hasActedThisTurn)
        assertFalse(archer.canMoveNow())
        assertFalse(archer.canAct)
    }

    @Test
    fun testActionSystemReset() {
        knight.hasMovedThisTurn = true
        knight.hasActedThisTurn = true
        knight.previousPosition = Position(0, 0)
        knight.canStillMoveAfterAttack = true

        knight.resetTurn()

        assertFalse(knight.hasMovedThisTurn)
        assertFalse(knight.hasActedThisTurn)
        assertNull(knight.previousPosition)
        assertFalse(knight.canStillMoveAfterAttack)
    }

    @Test
    fun testCanMoveNowWithCanto() {
        knight.hasMovedThisTurn = false
        assertTrue("Should be able to move initially", knight.canMoveNow())

        knight.hasMovedThisTurn = true
        knight.canStillMoveAfterAttack = false
        assertFalse("Should not be able to move after moving", knight.canMoveNow())

        knight.canStillMoveAfterAttack = true
        assertTrue("Should be able to move with Canto flag", knight.canMoveNow())
    }

    @Test
    fun testTransformedDragonRetainsCanto() {
        val manakete =
            Character(
                id = "manakete1",
                name = "Manakete",
                characterClass = CharacterClass.MANAKETE,
                team = Team.PLAYER,
                position = Position(7, 7),
            )

        assertFalse("Manakete should not have Canto", manakete.characterClass.hasCanto)

        manakete.transform()
        assertTrue("Dragon should be transformed", manakete.isTransformed)
        assertEquals(CharacterClass.DRAGON, manakete.characterClass)
        assertFalse("Dragon should not have Canto", manakete.characterClass.hasCanto)
    }
}
