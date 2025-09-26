package com.gameaday.opentactics.model

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class CharacterTest {
    private lateinit var knight: Character
    private lateinit var archer: Character
    private lateinit var mage: Character

    @Before
    fun setUp() {
        knight =
            Character(
                id = "knight1",
                name = "Sir Lancelot",
                characterClass = CharacterClass.KNIGHT,
                team = Team.PLAYER,
                position = Position(2, 3),
            )

        archer =
            Character(
                id = "archer1",
                name = "Robin Hood",
                characterClass = CharacterClass.ARCHER,
                team = Team.ENEMY,
                position = Position(5, 5),
            )

        mage =
            Character(
                id = "mage1",
                name = "Merlin",
                characterClass = CharacterClass.MAGE,
                team = Team.PLAYER,
                position = Position(1, 1),
            )
    }

    @Test
    fun testCharacterInitialization() {
        assertEquals("knight1", knight.id)
        assertEquals("Sir Lancelot", knight.name)
        assertEquals(CharacterClass.KNIGHT, knight.characterClass)
        assertEquals(Team.PLAYER, knight.team)
        assertEquals(Position(2, 3), knight.position)
        assertEquals(1, knight.level)
        assertEquals(0, knight.experience)
    }

    @Test
    fun testInitialStats() {
        assertEquals(CharacterClass.KNIGHT.baseStats.hp, knight.currentStats.hp)
        assertEquals(CharacterClass.KNIGHT.baseStats.mp, knight.currentStats.mp)
        assertEquals(CharacterClass.KNIGHT.baseStats.attack, knight.currentStats.attack)
        assertEquals(CharacterClass.KNIGHT.baseStats.defense, knight.currentStats.defense)
        assertEquals(CharacterClass.KNIGHT.baseStats.speed, knight.currentStats.speed)
        assertEquals(CharacterClass.KNIGHT.baseStats.skill, knight.currentStats.skill)
        assertEquals(CharacterClass.KNIGHT.baseStats.luck, knight.currentStats.luck)
    }

    @Test
    fun testHealthManagement() {
        val initialHp = knight.currentHp
        assertEquals(knight.currentStats.hp, initialHp)
        assertTrue(knight.isAlive)

        // Take some damage
        knight.takeDamage(10)
        assertEquals(initialHp - 10, knight.currentHp)
        assertTrue(knight.isAlive)

        // Take lethal damage
        knight.takeDamage(1000)
        assertEquals(0, knight.currentHp)
        assertFalse(knight.isAlive)
    }

    @Test
    fun testCannotHealAboveMaxHP() {
        val maxHp = knight.currentStats.hp
        knight.takeDamage(10)

        knight.heal(5)
        assertEquals(maxHp - 5, knight.currentHp)

        knight.heal(100) // Overheal
        assertEquals(maxHp, knight.currentHp) // Should cap at max HP
    }

    @Test
    fun testManaManagement() {
        val initialMp = mage.currentMp
        assertEquals(mage.currentStats.mp, initialMp)

        // Use mana
        mage.useMana(5)
        assertEquals(initialMp - 5, mage.currentMp)

        // Cannot use more mana than available
        mage.useMana(1000)
        assertEquals(0, mage.currentMp) // Should not go below 0
    }

    @Test
    fun testRestoreMana() {
        val maxMp = mage.currentStats.mp
        mage.useMana(10)

        mage.restoreMana(5)
        assertEquals(maxMp - 5, mage.currentMp)

        mage.restoreMana(100) // Over-restore
        assertEquals(maxMp, mage.currentMp) // Should cap at max MP
    }

    @Test
    fun testActionStates() {
        assertTrue(knight.canAct)
        assertTrue(knight.canMove)
        assertFalse(knight.hasActed)
        assertFalse(knight.hasMoved)

        knight.hasActed = true
        assertFalse(knight.canAct)
        assertTrue(knight.canMove) // Should still be able to move

        knight.hasMoved = true
        assertFalse(knight.canMove)
    }

    @Test
    fun testResetActions() {
        knight.hasActed = true
        knight.hasMoved = true

        assertFalse(knight.canAct)
        assertFalse(knight.canMove)

        knight.resetActions()

        assertTrue(knight.canAct)
        assertTrue(knight.canMove)
        assertFalse(knight.hasActed)
        assertFalse(knight.hasMoved)
    }

    @Test
    fun testExperienceAndLeveling() {
        assertEquals(1, knight.level)
        assertEquals(0, knight.experience)

        knight.gainExperience(50)
        assertEquals(50, knight.experience)
        assertEquals(1, knight.level) // Should not level up yet

        knight.gainExperience(50) // Total 100 exp
        assertEquals(100, knight.experience)
        assertEquals(2, knight.level) // Should level up
    }

    @Test
    fun testLevelUpStatsIncrease() {
        val initialStats = knight.currentStats.copy()

        knight.gainExperience(100) // Level up

        // Stats should increase (assuming proper level up mechanics)
        assertTrue(knight.currentStats.hp >= initialStats.hp)
        assertTrue(knight.currentStats.attack >= initialStats.attack)
        assertTrue(knight.currentStats.defense >= initialStats.defense)
    }

    @Test
    fun testPositionUpdate() {
        val newPosition = Position(5, 7)
        knight.position = newPosition
        assertEquals(newPosition, knight.position)
    }

    @Test
    fun testTeamAssignment() {
        assertEquals(Team.PLAYER, knight.team)
        assertEquals(Team.ENEMY, archer.team)
    }

    @Test
    fun testCharacterClassSpecificStats() {
        // Knight should have high HP and defense
        assertTrue(knight.currentStats.hp > archer.currentStats.hp)
        assertTrue(knight.currentStats.defense > archer.currentStats.defense)

        // Archer should have better range and speed
        assertTrue(archer.characterClass.attackRange > knight.characterClass.attackRange)
        assertTrue(archer.currentStats.speed > knight.currentStats.speed)

        // Mage should have high MP and magic attack
        assertTrue(mage.currentStats.mp > knight.currentStats.mp)
        assertTrue(mage.currentStats.attack > knight.currentStats.attack) // Magic attack
    }

    @Test
    fun testDeadCharacterCannotAct() {
        knight.takeDamage(1000) // Kill character
        assertFalse(knight.isAlive)
        assertFalse(knight.canAct)
        assertFalse(knight.canMove)
    }

    @Test
    fun testCharacterToString() {
        val string = knight.toString()
        assertTrue(string.contains("Sir Lancelot"))
        assertTrue(string.contains("Knight"))
    }

    @Test
    fun testCharacterEquality() {
        val knight2 =
            Character(
                id = "knight1", // Same ID
                name = "Different Name",
                characterClass = CharacterClass.ARCHER, // Different class
                team = Team.ENEMY, // Different team
                position = Position(0, 0), // Different position
            )

        // Characters with same ID should be equal
        assertEquals(knight, knight2)
        assertEquals(knight.hashCode(), knight2.hashCode())
    }
}
