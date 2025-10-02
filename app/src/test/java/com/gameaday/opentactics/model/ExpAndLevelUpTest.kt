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
        // Gain enough EXP for 2 level ups (100 + 100 = 200)
        character.gainExperience(200)

        assertEquals(3, character.level) // Should be level 3
        assertEquals(0, character.experience) // All EXP consumed for level ups
    }

    @Test
    fun testLevelUpAddsHealthIncrease() {
        // Damage the character first
        val initialMaxHp = character.maxHp
        character.takeDamage(10)
        val damagedHp = character.currentHp
        assertTrue(damagedHp < character.maxHp)

        // Level up
        character.gainExperience(100)

        // HP should increase by the stat gain (not full heal)
        // Current HP = damaged HP + (new max HP - old max HP)
        val hpGain = character.maxHp - initialMaxHp
        assertEquals(damagedHp + hpGain, character.currentHp)
    }

    @Test
    fun testLevelUpAddsManaIncrease() {
        // Use some mana first (if the character has any)
        if (character.maxMp > 0) {
            val initialMaxMp = character.maxMp
            character.currentMp = character.maxMp / 2
            val currentMp = character.currentMp

            // Level up
            character.gainExperience(100)

            // MP should increase by the stat gain (not full restore)
            val mpGain = character.maxMp - initialMaxMp
            assertEquals(currentMp + mpGain, character.currentMp)
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
    fun testExpRequirementIsFlat() {
        // Level 1->2 requires 100 EXP
        character.gainExperience(100)
        assertEquals(2, character.level)
        assertEquals(0, character.experience)

        // Level 2->3 also requires 100 EXP (flat requirement)
        character.gainExperience(100)
        assertEquals(3, character.level)
        assertEquals(0, character.experience)

        // Level 3->4 also requires 100 EXP (flat requirement)
        character.gainExperience(100)
        assertEquals(4, character.level)
        assertEquals(0, character.experience)
    }
}
