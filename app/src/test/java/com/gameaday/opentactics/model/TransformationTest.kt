package com.gameaday.opentactics.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class TransformationTest {
    private lateinit var manakete: Character
    private lateinit var knight: Character

    @Before
    fun setUp() {
        manakete =
            Character(
                id = "manakete1",
                name = "Tiki",
                characterClass = CharacterClass.MANAKETE,
                team = Team.PLAYER,
                position = Position(0, 0),
            )

        knight =
            Character(
                id = "knight1",
                name = "Sir Arthur",
                characterClass = CharacterClass.KNIGHT,
                team = Team.PLAYER,
                position = Position(1, 1),
            )
    }

    @Test
    fun testManaketeCanTransform() {
        assertTrue(manakete.canTransformNow())
        assertFalse(knight.canTransformNow())
    }

    @Test
    fun testTransformToRagon() {
        val originalHp = manakete.currentHp
        val originalMp = manakete.currentMp

        assertTrue(manakete.transform())

        // Verify transformation state
        assertTrue(manakete.isTransformed)
        assertEquals(CharacterClass.DRAGON, manakete.characterClass)
        assertEquals(CharacterClass.MANAKETE, manakete.originalClass)

        // Verify stats changed
        assertTrue(manakete.maxHp > originalHp)
        assertEquals(manakete.maxHp, manakete.currentHp)
        assertEquals(manakete.maxMp, manakete.currentMp)

        // Verify can't transform again
        assertFalse(manakete.canTransformNow())
        assertFalse(manakete.transform())
    }

    @Test
    fun testRevertTransformation() {
        // Transform first
        manakete.transform()
        assertTrue(manakete.isTransformed)

        // Take some damage
        val dragonMaxHp = manakete.maxHp
        manakete.takeDamage(10)
        val damagedHp = manakete.currentHp

        // Revert transformation
        assertTrue(manakete.canRevertTransform())
        assertTrue(manakete.revertTransform())

        // Verify state
        assertFalse(manakete.isTransformed)
        assertEquals(CharacterClass.MANAKETE, manakete.characterClass)
        assertNull(manakete.originalClass)

        // Verify HP scaled proportionally
        val expectedHpRatio = damagedHp.toFloat() / dragonMaxHp
        val expectedHp = (manakete.maxHp * expectedHpRatio).toInt()
        assertEquals(expectedHp, manakete.currentHp)
    }

    @Test
    fun testNonTransformableCharacterCantTransform() {
        assertFalse(knight.canTransformNow())
        assertFalse(knight.transform())
        assertFalse(knight.isTransformed)
        assertNull(knight.originalClass)
    }

    @Test
    fun testTransformationPreservesIdentity() {
        val originalId = manakete.id
        val originalName = manakete.name
        val originalTeam = manakete.team
        val originalPosition = manakete.position

        manakete.transform()

        assertEquals(originalId, manakete.id)
        assertEquals(originalName, manakete.name)
        assertEquals(originalTeam, manakete.team)
        assertEquals(originalPosition, manakete.position)
    }

    @Test
    fun testTransformationGrantsFlying() {
        assertFalse(manakete.characterClass.canFly)

        manakete.transform()

        assertTrue(manakete.characterClass.canFly)
    }

    @Test
    fun testRevertWithZeroHpClampsToOne() {
        manakete.transform()
        manakete.currentHp = 1 // Minimal HP

        manakete.revertTransform()

        // Should have at least 1 HP
        assertTrue(manakete.currentHp >= 1)
    }

    @Test
    fun testTransformationIncreasesMovementRange() {
        val originalMovement = manakete.characterClass.movementRange

        manakete.transform()

        assertTrue(manakete.characterClass.movementRange > originalMovement)
    }

    @Test
    fun testTransformationIncreasesAttackRange() {
        val originalRange = manakete.characterClass.attackRange

        manakete.transform()

        assertTrue(manakete.characterClass.attackRange >= originalRange)
    }
}
