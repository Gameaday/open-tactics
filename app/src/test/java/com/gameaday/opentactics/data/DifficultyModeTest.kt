package com.gameaday.opentactics.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DifficultyModeTest {
    @Test
    fun `easy mode has reduced enemy stats`() {
        val easy = DifficultyMode.EASY
        assertEquals("Easy", easy.displayName)
        assertTrue("Easy should reduce enemy stats", easy.enemyStatMultiplier < 1.0f)
        assertTrue("Easy should increase EXP gain", easy.expMultiplier > 1.0f)
    }

    @Test
    fun `normal mode has default multipliers`() {
        val normal = DifficultyMode.NORMAL
        assertEquals("Normal", normal.displayName)
        assertEquals(1.0f, normal.enemyStatMultiplier)
        assertEquals(1.0f, normal.expMultiplier)
    }

    @Test
    fun `hard mode has increased enemy stats`() {
        val hard = DifficultyMode.HARD
        assertEquals("Hard", hard.displayName)
        assertTrue("Hard should increase enemy stats", hard.enemyStatMultiplier > 1.0f)
        assertTrue("Hard should reduce EXP gain", hard.expMultiplier < 1.0f)
    }

    @Test
    fun `default preferences use normal difficulty`() {
        val prefs = GamePreferences()
        assertEquals(DifficultyMode.NORMAL, prefs.difficulty)
    }

    @Test
    fun `preferences can be set to hard difficulty`() {
        val prefs = GamePreferences(difficulty = DifficultyMode.HARD)
        assertEquals(DifficultyMode.HARD, prefs.difficulty)
    }
}
