@file:Suppress("MagicNumber")

package com.gameaday.opentactics

import android.os.Bundle
import android.text.InputType
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.gameaday.opentactics.data.DifficultyMode
import com.gameaday.opentactics.data.PlayerProfile
import com.gameaday.opentactics.data.SaveGameManager
import com.gameaday.opentactics.databinding.ActivitySettingsBinding

/**
 * Dedicated settings screen with proper toggles, sliders and layout.
 *
 * Replaces the former dialog-based settings for a more polished UX.
 * All preferences are persisted immediately via [SaveGameManager].
 */
class SettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding
    private lateinit var saveGameManager: SaveGameManager
    private var playerProfile: PlayerProfile? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        saveGameManager = SaveGameManager(this)
        loadProfile()
        populateUI()
        setupListeners()
    }

    override fun onResume() {
        super.onResume()
        loadProfile()
        populateUI()
    }

    private fun loadProfile() {
        playerProfile = saveGameManager.loadProfile() ?: PlayerProfile(playerName = "Player")
    }

    private fun saveProfile(profile: PlayerProfile) {
        saveGameManager.saveProfile(profile)
        playerProfile = profile
    }

    private fun populateUI() {
        val profile = playerProfile ?: return
        val prefs = profile.preferences

        binding.settingsPlayerName.text = profile.playerName
        binding.settingsDifficulty.text = prefs.difficulty.displayName

        binding.switchMusic.isChecked = prefs.musicEnabled
        binding.switchSoundEffects.isChecked = prefs.soundEffectsEnabled
        binding.switchHaptic.isChecked = prefs.hapticEnabled

        binding.settingsAnimSpeed.text = "${prefs.animationSpeed}x"
        binding.switchDamageNumbers.isChecked = prefs.showDamageNumbers
        binding.switchAutoSave.isChecked = prefs.autoSaveEnabled
        binding.settingsAutoSaveFreq.text = "Every ${prefs.autoSaveFrequency} turns"

        // Show/hide auto-save frequency based on toggle
        binding.autoSaveFrequencyRow.alpha = if (prefs.autoSaveEnabled) 1.0f else 0.5f
    }

    @Suppress("TooManyFunctions")
    private fun setupListeners() {
        binding.btnSettingsBack.setOnClickListener {
            finish()
            applyCloseTransition()
        }

        binding.settingsPlayerName.setOnClickListener { editPlayerName() }
        binding.settingsDifficulty.setOnClickListener { editDifficulty() }
        binding.settingsAnimSpeed.setOnClickListener { editAnimationSpeed() }
        binding.settingsAutoSaveFreq.setOnClickListener { editAutoSaveFrequency() }

        binding.switchMusic.setOnCheckedChangeListener { _, isChecked ->
            updatePreference { it.copy(musicEnabled = isChecked) }
        }

        binding.switchSoundEffects.setOnCheckedChangeListener { _, isChecked ->
            updatePreference { it.copy(soundEffectsEnabled = isChecked) }
        }

        binding.switchHaptic.setOnCheckedChangeListener { _, isChecked ->
            updatePreference { it.copy(hapticEnabled = isChecked) }
        }

        binding.switchDamageNumbers.setOnCheckedChangeListener { _, isChecked ->
            updatePreference { it.copy(showDamageNumbers = isChecked) }
        }

        binding.switchAutoSave.setOnCheckedChangeListener { _, isChecked ->
            updatePreference { it.copy(autoSaveEnabled = isChecked) }
            binding.autoSaveFrequencyRow.alpha = if (isChecked) 1.0f else 0.5f
        }
    }

    private fun updatePreference(
        transform: (com.gameaday.opentactics.data.GamePreferences) -> com.gameaday.opentactics.data.GamePreferences,
    ) {
        val profile = playerProfile ?: return
        val updated = profile.copy(preferences = transform(profile.preferences))
        saveProfile(updated)
    }

    private fun editPlayerName() {
        val profile = playerProfile ?: return
        val input =
            EditText(this).apply {
                inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_WORDS
                setText(profile.playerName)
            }

        AlertDialog
            .Builder(this)
            .setTitle("Edit Player Name")
            .setView(input)
            .setPositiveButton("Save") { _, _ ->
                val newName =
                    input.text
                        .toString()
                        .trim()
                        .ifEmpty { "Player" }
                saveProfile(profile.copy(playerName = newName))
                populateUI()
            }.setNegativeButton("Cancel", null)
            .show()
    }

    private fun editDifficulty() {
        val profile = playerProfile ?: return
        val modes = DifficultyMode.values()
        val modeNames =
            modes
                .map {
                    when (it) {
                        DifficultyMode.EASY -> "Easy — Enemy stats -20%, EXP +20%"
                        DifficultyMode.NORMAL -> "Normal — Standard experience"
                        DifficultyMode.HARD -> "Hard — Enemy stats +20%, EXP -20%"
                    }
                }.toTypedArray()
        val currentIndex = modes.indexOf(profile.preferences.difficulty).takeIf { it >= 0 } ?: 1

        AlertDialog
            .Builder(this)
            .setTitle("Difficulty")
            .setSingleChoiceItems(modeNames, currentIndex) { dialog, which ->
                updatePreference { it.copy(difficulty = modes[which]) }
                populateUI()
                dialog.dismiss()
            }.setNegativeButton("Cancel", null)
            .show()
    }

    private fun editAnimationSpeed() {
        val profile = playerProfile ?: return
        val speeds = arrayOf("Slow (0.5x)", "Normal (1.0x)", "Fast (1.5x)", "Very Fast (2.0x)")
        val values = arrayOf(0.5f, 1.0f, 1.5f, 2.0f)
        val currentIndex = values.indexOf(profile.preferences.animationSpeed).takeIf { it >= 0 } ?: 1

        AlertDialog
            .Builder(this)
            .setTitle("Animation Speed")
            .setSingleChoiceItems(speeds, currentIndex) { dialog, which ->
                updatePreference { it.copy(animationSpeed = values[which]) }
                populateUI()
                dialog.dismiss()
            }.setNegativeButton("Cancel", null)
            .show()
    }

    private fun editAutoSaveFrequency() {
        val profile = playerProfile ?: return
        if (!profile.preferences.autoSaveEnabled) return

        val frequencies = arrayOf("Every turn", "Every 3 turns", "Every 5 turns", "Every 10 turns")
        val values = arrayOf(1, 3, 5, 10)
        val currentIndex = values.indexOf(profile.preferences.autoSaveFrequency).takeIf { it >= 0 } ?: 2

        AlertDialog
            .Builder(this)
            .setTitle("Auto-save Frequency")
            .setSingleChoiceItems(frequencies, currentIndex) { dialog, which ->
                updatePreference { it.copy(autoSaveFrequency = values[which]) }
                populateUI()
                dialog.dismiss()
            }.setNegativeButton("Cancel", null)
            .show()
    }

    private fun applyCloseTransition() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            overrideActivityTransition(OVERRIDE_TRANSITION_CLOSE, R.anim.fade_in, R.anim.fade_out)
        } else {
            @Suppress("DEPRECATION")
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        }
    }
}
