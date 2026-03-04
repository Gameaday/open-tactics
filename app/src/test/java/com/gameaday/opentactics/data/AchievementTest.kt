package com.gameaday.opentactics.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AchievementTest {
    @Test
    fun `all achievements have valid definitions`() {
        val achievements = AchievementRepository.getAllAchievements()
        assertTrue("Should have achievements defined", achievements.isNotEmpty())
        achievements.forEach { achievement ->
            assertTrue("Achievement ${achievement.id} should have a name", achievement.name.isNotEmpty())
            assertTrue(
                "Achievement ${achievement.id} should have a description",
                achievement.description.isNotEmpty(),
            )
        }
    }

    @Test
    fun `getAchievement returns correct achievement`() {
        val achievement = AchievementRepository.getAchievement("first_victory")
        assertNotNull("Should find first_victory achievement", achievement)
        assertEquals("First Victory", achievement?.name)
    }

    @Test
    fun `getAchievement returns null for unknown id`() {
        val achievement = AchievementRepository.getAchievement("nonexistent")
        assertEquals(null, achievement)
    }

    @Test
    fun `first victory unlocks after 1 battle won`() {
        val profile =
            PlayerProfile(
                playerName = "Test",
                totalBattlesWon = 1,
            )
        val earned = AchievementRepository.checkNewAchievements(profile)
        assertTrue("Should earn first_victory", earned.contains("first_victory"))
    }

    @Test
    fun `veteran unlocks after 10 battles won`() {
        val profile =
            PlayerProfile(
                playerName = "Test",
                totalBattlesWon = 10,
            )
        val earned = AchievementRepository.checkNewAchievements(profile)
        assertTrue("Should earn veteran", earned.contains("veteran"))
    }

    @Test
    fun `campaign complete unlocks after completing campaign`() {
        val profile =
            PlayerProfile(
                playerName = "Test",
                campaignsCompleted = 1,
            )
        val earned = AchievementRepository.checkNewAchievements(profile)
        assertTrue("Should earn campaign_complete", earned.contains("campaign_complete"))
    }

    @Test
    fun `already unlocked achievements are not re-earned`() {
        val profile =
            PlayerProfile(
                playerName = "Test",
                totalBattlesWon = 1,
                achievementsUnlocked = listOf("first_victory"),
            )
        val earned = AchievementRepository.checkNewAchievements(profile)
        assertTrue("Should not re-earn first_victory", !earned.contains("first_victory"))
    }

    @Test
    fun `max level achievement unlocks at level 20`() {
        val profile =
            PlayerProfile(
                playerName = "Test",
                highestLevel = 20,
            )
        val earned = AchievementRepository.checkNewAchievements(profile)
        assertTrue("Should earn max_level", earned.contains("max_level"))
    }
}
