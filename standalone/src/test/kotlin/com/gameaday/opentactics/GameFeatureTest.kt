package com.gameaday.opentactics

import com.gameaday.opentactics.game.GameState
import com.gameaday.opentactics.model.ChapterRepository
import com.gameaday.opentactics.model.Character
import com.gameaday.opentactics.model.CharacterClass
import com.gameaday.opentactics.model.GameBoard
import com.gameaday.opentactics.model.Position
import com.gameaday.opentactics.model.Team
import com.gameaday.opentactics.model.Weapon
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Tests for new features: healing, critical hits, chapters, EXP scaling.
 */
class GameFeatureTest {
    @Test
    fun testHealingStaffCreation() {
        val healStaff = Weapon.heal()
        assertTrue(healStaff.canHeal, "Heal staff should have canHeal = true")
        assertEquals("Heal", healStaff.name)

        val mendStaff = Weapon.mend()
        assertTrue(mendStaff.canHeal, "Mend staff should have canHeal = true")
        assertEquals("Mend", mendStaff.name)
    }

    @Test
    fun testCharacterHealing() {
        val character =
            Character(
                id = "test_healer",
                name = "Test",
                characterClass = CharacterClass.HEALER,
                team = Team.PLAYER,
                position = Position(0, 0),
            )

        val maxHp = character.maxHp
        character.takeDamage(15)
        assertEquals(maxHp - 15, character.currentHp)

        character.heal(10)
        assertEquals(maxHp - 5, character.currentHp)

        // Heal shouldn't exceed max HP
        character.heal(100)
        assertEquals(maxHp, character.currentHp)
    }

    @Test
    fun testPerformHeal() {
        val board = GameBoard(12, 8)

        val healer =
            Character(
                id = "healer",
                name = "Healer",
                characterClass = CharacterClass.HEALER,
                team = Team.PLAYER,
                position = Position(0, 0),
            )
        healer.addWeapon(Weapon.heal())

        val target =
            Character(
                id = "target",
                name = "Target",
                characterClass = CharacterClass.KNIGHT,
                team = Team.PLAYER,
                position = Position(1, 0),
            )
        target.takeDamage(15) // Wound the target

        board.placeCharacter(healer, Position(0, 0))
        board.placeCharacter(target, Position(1, 0))

        val gameState = GameState(board)
        gameState.addPlayerCharacter(healer)
        gameState.addPlayerCharacter(target)

        val previousHp = target.currentHp
        val result = gameState.performHeal(healer, target)

        assertTrue(result.healAmount > 0, "Heal amount should be positive")
        assertTrue(target.currentHp > previousHp, "Target should have more HP after healing")
        assertTrue(result.expGained > 0, "Healer should gain EXP")
    }

    @Test
    fun testCalculateHealTargets() {
        val board = GameBoard(12, 8)

        val healer =
            Character(
                id = "healer",
                name = "Healer",
                characterClass = CharacterClass.HEALER,
                team = Team.PLAYER,
                position = Position(0, 0),
            )
        healer.addWeapon(Weapon.heal())

        val woundedAlly =
            Character(
                id = "ally",
                name = "Ally",
                characterClass = CharacterClass.KNIGHT,
                team = Team.PLAYER,
                position = Position(1, 0),
            )
        woundedAlly.takeDamage(10)

        val healthyAlly =
            Character(
                id = "ally2",
                name = "Ally2",
                characterClass = CharacterClass.ARCHER,
                team = Team.PLAYER,
                position = Position(0, 1),
            )

        board.placeCharacter(healer, Position(0, 0))
        board.placeCharacter(woundedAlly, Position(1, 0))
        board.placeCharacter(healthyAlly, Position(0, 1))

        val gameState = GameState(board)
        gameState.addPlayerCharacter(healer)
        gameState.addPlayerCharacter(woundedAlly)
        gameState.addPlayerCharacter(healthyAlly)

        val targets = gameState.calculateHealTargets(healer)
        assertTrue(targets.contains(woundedAlly), "Wounded ally should be a heal target")
        assertTrue(!targets.contains(healthyAlly), "Healthy ally should not be a heal target")
    }

    @Test
    fun testExpMultiplierScaling() {
        val board = GameBoard(12, 8)
        val gameState = GameState(board)

        // Default multiplier
        assertEquals(1.0f, gameState.expMultiplier)

        // Set difficulty scaling
        gameState.expMultiplier = 1.2f
        assertEquals(1.2f, gameState.expMultiplier)
    }

    @Test
    fun testBattleResultIncludesCritical() {
        val board = GameBoard(12, 8)

        val attacker =
            Character(
                id = "attacker",
                name = "Attacker",
                characterClass = CharacterClass.KNIGHT,
                team = Team.PLAYER,
                position = Position(0, 0),
            )
        attacker.addWeapon(Weapon.ironSword())

        val target =
            Character(
                id = "target",
                name = "Target",
                characterClass = CharacterClass.KNIGHT,
                team = Team.ENEMY,
                position = Position(1, 0),
            )
        target.addWeapon(Weapon.ironSword())

        board.placeCharacter(attacker, Position(0, 0))
        board.placeCharacter(target, Position(1, 0))

        val gameState = GameState(board)
        gameState.addPlayerCharacter(attacker)
        gameState.addEnemyCharacter(target)

        val result = gameState.performAttack(attacker, target)
        assertTrue(result.damage > 0, "Attack should deal damage")
        assertTrue(result.expGained > 0, "Attacker should gain EXP")
        // wasCritical is boolean - just check it doesn't crash
        assertNotNull(result.wasCritical)
    }

    @Test
    fun testCampaignHas20Chapters() {
        assertEquals(20, ChapterRepository.getTotalChapters(), "Campaign should have 20 chapters")
    }

    @Test
    fun testAllChaptersValid() {
        for (i in 1..20) {
            val chapter = ChapterRepository.getChapter(i)
            assertNotNull(chapter, "Chapter $i should exist")
            assertEquals(i, chapter.number, "Chapter $i should have correct number")
            assertTrue(chapter.title.isNotEmpty(), "Chapter $i should have a title")
            assertTrue(chapter.enemyUnits.isNotEmpty(), "Chapter $i should have enemy units")
        }
    }

    @Test
    fun testChapterDifficultyProgression() {
        // Later chapters should generally have higher-level enemies
        val ch1 = ChapterRepository.getChapter(1)!!
        val ch10 = ChapterRepository.getChapter(10)!!
        val ch20 = ChapterRepository.getChapter(20)!!

        val ch1MaxLevel = ch1.enemyUnits.maxOf { it.level }
        val ch10MaxLevel = ch10.enemyUnits.maxOf { it.level }
        val ch20MaxLevel = ch20.enemyUnits.maxOf { it.level }

        assertTrue(ch10MaxLevel > ch1MaxLevel, "Chapter 10 enemies should be higher level than chapter 1")
        assertTrue(ch20MaxLevel > ch10MaxLevel, "Chapter 20 enemies should be higher level than chapter 10")
    }
}
