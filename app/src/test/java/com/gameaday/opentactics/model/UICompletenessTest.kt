package com.gameaday.opentactics.model

import com.gameaday.opentactics.data.SoundManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests for UI completeness and character class coverage
 */
class UICompletenessTest {
    @Test
    fun allCharacterClassesHaveDisplayNames() {
        CharacterClass.values().forEach { characterClass ->
            assertNotNull(
                "CharacterClass ${characterClass.name} should have a display name",
                characterClass.displayName,
            )
            assert(characterClass.displayName.isNotEmpty()) {
                "CharacterClass ${characterClass.name} displayName should not be empty"
            }
        }
    }

    @Test
    fun allCharacterClassesHaveBaseStats() {
        CharacterClass.values().forEach { characterClass ->
            val stats = characterClass.baseStats
            assert(stats.hp > 0) {
                "CharacterClass ${characterClass.name} should have positive HP"
            }
            assert(stats.attack >= 0) {
                "CharacterClass ${characterClass.name} should have non-negative attack"
            }
            assert(stats.defense >= 0) {
                "CharacterClass ${characterClass.name} should have non-negative defense"
            }
        }
    }

    @Test
    fun characterInfoShowsAllSevenStats() {
        // Verify Stats data class has all 7 expected stats
        val stats = Stats(hp = 25, mp = 5, attack = 12, defense = 14, speed = 8, skill = 10, luck = 8)
        assertEquals(25, stats.hp)
        assertEquals(5, stats.mp)
        assertEquals(12, stats.attack)
        assertEquals(14, stats.defense)
        assertEquals(8, stats.speed)
        assertEquals(10, stats.skill)
        assertEquals(8, stats.luck)
    }

    @Test
    fun characterEquippedWeaponInfo() {
        val character =
            Character(
                id = "test1",
                name = "TestUnit",
                characterClass = CharacterClass.KNIGHT,
                team = Team.PLAYER,
                position = Position(0, 0),
            )

        // No weapon equipped initially
        assert(character.equippedWeapon == null) {
            "Character should have no weapon equipped initially"
        }

        // Equip a weapon
        character.addWeapon(Weapon.ironSword())
        assertNotNull(
            "Character should have an equipped weapon after adding one",
            character.equippedWeapon,
        )
        assertEquals("Iron Sword", character.equippedWeapon?.name)
    }

    @Test
    fun characterLevelAndExpInfo() {
        val character =
            Character(
                id = "test1",
                name = "TestUnit",
                characterClass = CharacterClass.KNIGHT,
                team = Team.PLAYER,
                position = Position(0, 0),
                level = 3,
                experience = 42,
            )

        assertEquals(3, character.level)
        assertEquals(42, character.experience)
    }

    @Test
    fun nineCharacterClassesExist() {
        // Verify all 9 character classes are defined
        val classes = CharacterClass.values()
        assertEquals(
            "There should be 9 character classes",
            9,
            classes.size,
        )

        // Verify specific classes exist
        val classNames = classes.map { it.name }.toSet()
        assert("KNIGHT" in classNames)
        assert("ARCHER" in classNames)
        assert("MAGE" in classNames)
        assert("HEALER" in classNames)
        assert("THIEF" in classNames)
        assert("PEGASUS_KNIGHT" in classNames)
        assert("WYVERN_RIDER" in classNames)
        assert("MANAKETE" in classNames)
        assert("DRAGON" in classNames)
    }

    @Test
    fun characterStatsDisplayString() {
        val stats = Stats(hp = 25, mp = 5, attack = 12, defense = 14, speed = 8, skill = 10, luck = 8)
        val displayStr = stats.toDisplayString()
        assertNotNull("Display string should not be null", displayStr)
        assert(displayStr.isNotEmpty()) { "Display string should not be empty" }
        // Verify display string contains all stat abbreviations
        assert(displayStr.contains("HP")) { "Display string should contain HP" }
        assert(displayStr.contains("ATK")) { "Display string should contain ATK" }
        assert(displayStr.contains("DEF")) { "Display string should contain DEF" }
        assert(displayStr.contains("SPD")) { "Display string should contain SPD" }
        assert(displayStr.contains("SKL")) { "Display string should contain SKL" }
        assert(displayStr.contains("LCK")) { "Display string should contain LCK" }
    }

    @Test
    fun soundManagerEffectsAreDefined() {
        // Verify all expected sound effect types exist
        val effects = SoundManager.SoundEffect.values()
        val effectNames = effects.map { it.name }.toSet()

        assertTrue("Should have BUTTON_CLICK effect", "BUTTON_CLICK" in effectNames)
        assertTrue("Should have MOVE_UNIT effect", "MOVE_UNIT" in effectNames)
        assertTrue("Should have ATTACK_HIT effect", "ATTACK_HIT" in effectNames)
        assertTrue("Should have CRITICAL_HIT effect", "CRITICAL_HIT" in effectNames)
        assertTrue("Should have HEAL effect", "HEAL" in effectNames)
        assertTrue("Should have LEVEL_UP effect", "LEVEL_UP" in effectNames)
        assertTrue("Should have VICTORY effect", "VICTORY" in effectNames)
        assertTrue("Should have DEFEAT effect", "DEFEAT" in effectNames)
        assertTrue("Should have TURN_START effect", "TURN_START" in effectNames)
    }

    @Test
    fun difficultyModesHaveDisplayNames() {
        // Verify all difficulty modes have display names
        com.gameaday.opentactics.data.DifficultyMode.values().forEach { mode ->
            assertNotNull(
                "DifficultyMode ${mode.name} should have a display name",
                mode.displayName,
            )
            assertFalse(
                "DifficultyMode ${mode.name} displayName should not be empty",
                mode.displayName.isEmpty(),
            )
        }
    }
}
