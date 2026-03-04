package com.gameaday.opentactics.data

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

/**
 * Manages haptic feedback for gameplay events.
 *
 * Provides tactile responses on attacks, level-ups, victories, and UI interactions.
 * Respects user preference toggling and gracefully degrades on unsupported devices.
 *
 * Usage:
 * ```
 * val haptics = HapticManager(context)
 * haptics.setEnabled(true)
 * haptics.vibrateAttack()
 * ```
 */
class HapticManager(
    context: Context,
) {
    private val vibrator: Vibrator? = getVibrator(context)
    private var enabled: Boolean = true

    /** Enable or disable haptic feedback globally. */
    fun setEnabled(enabled: Boolean) {
        this.enabled = enabled
    }

    /** @return true if haptic feedback is currently enabled. */
    fun isEnabled(): Boolean = enabled

    /** Light tap for UI button presses. */
    fun vibrateClick() {
        vibrate(CLICK_DURATION)
    }

    /** Short burst for attack hits. */
    fun vibrateAttack() {
        vibrate(ATTACK_DURATION)
    }

    /** Strong burst for critical hits. */
    fun vibrateCritical() {
        vibrate(CRITICAL_DURATION, CRITICAL_AMPLITUDE)
    }

    /** Ascending pattern for level-up celebrations. */
    fun vibrateLevelUp() {
        vibratePattern(LEVEL_UP_PATTERN, NO_REPEAT)
    }

    /** Triumphant pattern for chapter victory. */
    fun vibrateVictory() {
        vibratePattern(VICTORY_PATTERN, NO_REPEAT)
    }

    /** Brief pulse for unit selection. */
    fun vibrateSelect() {
        vibrate(SELECT_DURATION)
    }

    private fun vibrate(
        durationMs: Long,
        amplitude: Int = DEFAULT_AMPLITUDE,
    ) {
        if (!enabled || vibrator == null) return
        if (!vibrator.hasVibrator()) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(durationMs, amplitude))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(durationMs)
        }
    }

    private fun vibratePattern(
        pattern: LongArray,
        repeat: Int,
    ) {
        if (!enabled || vibrator == null) return
        if (!vibrator.hasVibrator()) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, repeat))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(pattern, repeat)
        }
    }

    companion object {
        private const val CLICK_DURATION = 20L
        private const val ATTACK_DURATION = 40L
        private const val CRITICAL_DURATION = 80L
        private const val CRITICAL_AMPLITUDE = 255
        private const val SELECT_DURATION = 15L
        private const val DEFAULT_AMPLITUDE = VibrationEffect.DEFAULT_AMPLITUDE
        private const val NO_REPEAT = -1

        // Level-up pattern: delay, buzz, delay, buzz, delay, buzz (ascending feel)
        private val LEVEL_UP_PATTERN = longArrayOf(0, 30, 60, 40, 60, 50)

        // Victory: celebratory burst pattern
        private val VICTORY_PATTERN = longArrayOf(0, 60, 80, 60, 80, 100)

        private fun getVibrator(context: Context): Vibrator? =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                manager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
    }
}
