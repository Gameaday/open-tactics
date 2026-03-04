package com.gameaday.opentactics.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DifficultyScalingTest {
    @Test
    fun `stats scale correctly with multiplier`() {
        val stats = Stats(hp = 20, mp = 10, attack = 15, defense = 12, speed = 10, skill = 8, luck = 6)
        val scaled = stats.scale(1.2f)
        assertEquals(24, scaled.hp)
        assertEquals(12, scaled.mp)
        assertEquals(18, scaled.attack)
        assertEquals(14, scaled.defense)
        assertEquals(12, scaled.speed)
        assertEquals(9, scaled.skill)
        assertEquals(7, scaled.luck)
    }

    @Test
    fun `stats scale down correctly`() {
        val stats = Stats(hp = 20, mp = 10, attack = 15, defense = 12, speed = 10, skill = 8, luck = 6)
        val scaled = stats.scale(0.8f)
        assertEquals(16, scaled.hp)
        assertEquals(8, scaled.mp)
        assertEquals(12, scaled.attack)
        assertEquals(9, scaled.defense)
        assertEquals(8, scaled.speed)
        assertEquals(6, scaled.skill)
        assertEquals(4, scaled.luck)
    }

    @Test
    fun `stats scale minimum is 1`() {
        val stats = Stats(hp = 1, mp = 1, attack = 1, defense = 1, speed = 1, skill = 1, luck = 1)
        val scaled = stats.scale(0.5f)
        assertEquals(1, scaled.hp)
        assertEquals(1, scaled.mp)
        assertEquals(1, scaled.attack)
        assertEquals(1, scaled.defense)
        assertEquals(1, scaled.speed)
        assertEquals(1, scaled.skill)
        assertEquals(1, scaled.luck)
    }

    @Test
    fun `difficulty scaling only applies to enemy units`() {
        val player =
            Character(
                id = "player1",
                name = "Hero",
                characterClass = CharacterClass.KNIGHT,
                team = Team.PLAYER,
                position = Position(0, 0),
                statBonuses = Stats(5, 5, 5, 5, 5, 5, 5),
            )
        val originalStats = player.statBonuses
        player.applyDifficultyScaling(1.5f)
        // Player stats should remain unchanged
        assertEquals(originalStats, player.statBonuses)
    }

    @Test
    fun `difficulty scaling applies to enemy units`() {
        val enemy =
            Character(
                id = "enemy1",
                name = "Bandit",
                characterClass = CharacterClass.KNIGHT,
                team = Team.ENEMY,
                position = Position(0, 0),
                statBonuses = Stats(10, 5, 8, 6, 4, 3, 2),
            )
        enemy.applyDifficultyScaling(1.2f)
        // Stats should be scaled up
        assertTrue("HP should increase", enemy.statBonuses.hp > 10)
        assertTrue("Attack should increase", enemy.statBonuses.attack > 8)
    }

    @Test
    fun `difficulty scaling with 1 multiplier is no-op`() {
        val enemy =
            Character(
                id = "enemy1",
                name = "Bandit",
                characterClass = CharacterClass.KNIGHT,
                team = Team.ENEMY,
                position = Position(0, 0),
                statBonuses = Stats(10, 5, 8, 6, 4, 3, 2),
            )
        val originalBonuses = enemy.statBonuses
        enemy.applyDifficultyScaling(1.0f)
        assertEquals(originalBonuses, enemy.statBonuses)
    }
}
