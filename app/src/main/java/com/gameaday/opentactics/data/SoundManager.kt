package com.gameaday.opentactics.data

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool

/**
 * Manages game audio including sound effects and music playback.
 *
 * Sound effects use [SoundPool] for low-latency playback during gameplay.
 * Music support is ready to be connected when audio assets are added to res/raw.
 *
 * Usage:
 * ```
 * val soundManager = SoundManager(context)
 * soundManager.setPreferences(musicEnabled = true, sfxEnabled = true)
 * soundManager.playButtonClick()
 * soundManager.release() // Call in onDestroy
 * ```
 */
class SoundManager(
    context: Context,
) {
    private val appContext: Context = context.applicationContext
    private var musicEnabled: Boolean = true
    private var sfxEnabled: Boolean = true

    private val soundPool: SoundPool =
        SoundPool
            .Builder()
            .setMaxStreams(MAX_STREAMS)
            .setAudioAttributes(
                AudioAttributes
                    .Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build(),
            ).build()

    // Sound effect IDs loaded from resources
    // These will be populated when audio assets are added to res/raw/
    private val loadedSounds = mutableMapOf<SoundEffect, Int>()

    /**
     * Load a sound effect from a raw resource.
     * Call once per sound during initialization.
     *
     * @param effect The sound effect identifier.
     * @param rawResId The raw resource ID (e.g., R.raw.button_click).
     */
    fun loadSound(
        effect: SoundEffect,
        rawResId: Int,
    ) {
        val soundId = soundPool.load(appContext, rawResId, PRIORITY)
        loadedSounds[effect] = soundId
    }

    /** Set audio preferences from player profile. */
    fun setPreferences(
        musicEnabled: Boolean,
        sfxEnabled: Boolean,
    ) {
        this.musicEnabled = musicEnabled
        this.sfxEnabled = sfxEnabled
    }

    /** @return true if music playback is enabled. */
    fun isMusicEnabled(): Boolean = musicEnabled

    /**
     * Play a sound effect if SFX are enabled.
     * Returns the stream ID (0 if not played).
     */
    fun playSfx(effect: SoundEffect): Int {
        if (!sfxEnabled) return 0
        val soundId = loadedSounds[effect] ?: return 0
        return soundPool.play(soundId, SFX_VOLUME, SFX_VOLUME, PRIORITY, NO_LOOP, NORMAL_RATE)
    }

    /** Convenience: play a UI button click sound. */
    fun playButtonClick(): Int = playSfx(SoundEffect.BUTTON_CLICK)

    /** Convenience: play attack sound. */
    fun playAttack(): Int = playSfx(SoundEffect.ATTACK_HIT)

    /** Convenience: play level-up sound. */
    fun playLevelUp(): Int = playSfx(SoundEffect.LEVEL_UP)

    /** Release all audio resources. Call in Activity.onDestroy(). */
    fun release() {
        soundPool.release()
        loadedSounds.clear()
    }

    /** Identifiers for available sound effects. */
    enum class SoundEffect {
        BUTTON_CLICK,
        MOVE_UNIT,
        ATTACK_HIT,
        CRITICAL_HIT,
        HEAL,
        LEVEL_UP,
        VICTORY,
        DEFEAT,
        TURN_START,
    }

    companion object {
        private const val MAX_STREAMS = 4
        private const val SFX_VOLUME = 1.0f
        private const val PRIORITY = 1
        private const val NO_LOOP = 0
        private const val NORMAL_RATE = 1.0f
    }
}
