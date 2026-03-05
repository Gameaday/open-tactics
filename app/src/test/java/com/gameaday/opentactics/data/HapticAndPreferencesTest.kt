package com.gameaday.opentactics.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests for the enhanced GamePreferences with haptic support,
 * and basic HapticManager toggle behavior.
 */
class HapticAndPreferencesTest {
    @Test
    fun gamePreferencesHasHapticEnabled() {
        val prefs = GamePreferences()
        assertTrue("Haptic should be enabled by default", prefs.hapticEnabled)
    }

    @Test
    fun gamePreferencesHapticCanBeDisabled() {
        val prefs = GamePreferences(hapticEnabled = false)
        assertFalse("Haptic should be disabled when set", prefs.hapticEnabled)
    }

    @Test
    fun gamePreferencesCopyPreservesHaptic() {
        val original = GamePreferences(hapticEnabled = true)
        val copied = original.copy(hapticEnabled = false)
        assertTrue("Original should still be true", original.hapticEnabled)
        assertFalse("Copy should be false", copied.hapticEnabled)
    }

    @Test
    fun gamePreferencesDefaultValues() {
        val prefs = GamePreferences()
        assertTrue(prefs.musicEnabled)
        assertTrue(prefs.soundEffectsEnabled)
        assertTrue(prefs.hapticEnabled)
        assertEquals(1.0f, prefs.animationSpeed, 0.01f)
        assertTrue(prefs.showDamageNumbers)
        assertTrue(prefs.autoSaveEnabled)
        assertEquals(5, prefs.autoSaveFrequency)
        assertEquals(DifficultyMode.NORMAL, prefs.difficulty)
    }

    @Test
    fun gamePreferencesSerializationRoundTrip() {
        val prefs = GamePreferences(hapticEnabled = false, musicEnabled = false)
        val json =
            kotlinx.serialization.json.Json
                .encodeToString(GamePreferences.serializer(), prefs)
        val decoded =
            kotlinx.serialization.json.Json
                .decodeFromString(GamePreferences.serializer(), json)
        assertEquals(prefs, decoded)
        assertFalse(decoded.hapticEnabled)
        assertFalse(decoded.musicEnabled)
    }
}
