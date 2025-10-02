package com.gameaday.opentactics.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ExpAndLevelUpTest {
    private lateinit var character: Character

    @Before
    fun setUp() {
        character =
            Character(
                id = "test_knight",
                name = "Test Knight",
                characterClass = CharacterClass.KNIGHT,
                team = Team.PLAYER,
                position = Position(0, 0),
                level = 1,
                experience = 0,
            )
    }

    @Test
    fun testExpGainDoesNotLevelUp() {
        val initialLevel = character.level
        val initialHp = character.currentHp

        // Gain some EXP but not enough to level up (needs 100)
        character.gainExperience(50)

        assertEquals(50, character.experience)
        assertEquals(initialLevel, character.level)
        assertEquals(initialHp, character.currentHp) // HP shouldn't change
    }

    @Test
    fun testExpGainCausesLevelUp() {
        val initialLevel = character.level
        val initialMaxHp = character.maxHp

        // Gain enough EXP to level up (100 for level 1->2)
        character.gainExperience(100)

        assertEquals(0, character.experience) // EXP resets after level up
        assertEquals(initialLevel + 1, character.level)
        assertEquals(character.maxHp, character.currentHp) // HP restored to max on level up
    }

    @Test
    fun testMultipleLevelUps() {
        // Gain enough EXP for 2 level ups (100 + 200 = 300)
        character.gainExperience(300)

        assertEquals(3, character.level) // Should be level 3
        assertTrue(character.experience < 300) // Some EXP leftover
    }

    @Test
    fun testLevelUpRestoresHealth() {
        // Damage the character first
        character.takeDamage(10)
        val damagedHp = character.currentHp
        assertTrue(damagedHp < character.maxHp)

        // Level up
        character.gainExperience(100)

        // HP should be fully restored
        assertEquals(character.maxHp, character.currentHp)
    }

    @Test
    fun testLevelUpRestoresMana() {
        // Use some mana first (if the character has any)
        if (character.maxMp > 0) {
            val initialMaxMp = character.maxMp
            character.currentMp = character.maxMp / 2

            // Level up
            character.gainExperience(100)

            // MP should be fully restored
            assertEquals(character.maxMp, character.currentMp)
        }
    }

    @Test
    fun testMaxLevel() {
        // Try to level up past max level (20)
        character.gainExperience(10000) // Huge amount of EXP

        // Should cap at level 20
        assertTrue(character.level <= 20)
    }

    @Test
    fun testExpRequirementScalesWithLevel() {
        // Level 1->2 requires 100 EXP
        character.gainExperience(100)
        assertEquals(2, character.level)

        // Level 2->3 requires 200 EXP
        character.gainExperience(200)
        assertEquals(3, character.level)

        // Level 3->4 requires 300 EXP
        character.gainExperience(300)
        assertEquals(4, character.level)
    }
}
