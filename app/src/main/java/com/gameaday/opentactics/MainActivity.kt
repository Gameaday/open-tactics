@file:Suppress("MagicNumber")

package com.gameaday.opentactics

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.gameaday.opentactics.data.PlayerProfile
import com.gameaday.opentactics.data.SaveGameManager
import com.gameaday.opentactics.databinding.ActivityMainBinding
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var saveGameManager: SaveGameManager
    private var playerProfile: PlayerProfile? = null

    private val dateFormat = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        saveGameManager = SaveGameManager(this)
        loadPlayerProfile()
        setupClickListeners()
        updateContinueButton()
    }

    override fun onResume() {
        super.onResume()
        loadPlayerProfile()
        updateContinueButton()
    }

    private fun loadPlayerProfile() {
        playerProfile = saveGameManager.loadProfile()

        // Update player name display if we have a profile
        playerProfile?.let { profile ->
            binding.playerWelcome?.text = "Welcome back, ${profile.playerName}!"
        }
    }

    private fun setupClickListeners() {
        binding.btnNewGame.setOnClickListener {
            showNewGameDialog()
        }

        binding.btnContinue.setOnClickListener {
            showContinueGameDialog()
        }

        binding.btnSettings.setOnClickListener {
            showSettingsDialog()
        }

        binding.btnAbout.setOnClickListener {
            showAboutDialog()
        }

        binding.btnExit.setOnClickListener {
            finish()
        }
    }

    private fun showNewGameDialog() {
        val input =
            EditText(this).apply {
                inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_WORDS
                hint = "Enter your name"
                setText(playerProfile?.playerName ?: "Player")
            }

        AlertDialog
            .Builder(this)
            .setTitle("New Game")
            .setMessage("Enter your character name:")
            .setView(input)
            .setPositiveButton("Start Game") { _, _ ->
                val playerName =
                    input.text
                        .toString()
                        .trim()
                        .ifEmpty { "Player" }

                // Create or update player profile
                playerProfile = playerProfile?.copy(playerName = playerName)
                    ?: PlayerProfile(playerName = playerName)
                saveGameManager.saveProfile(playerProfile!!)

                startGame(playerName = playerName, isNewGame = true)
            }.setNegativeButton("Cancel", null)
            .show()
    }

    private fun showContinueGameDialog() {
        lifecycleScope.launch {
            val saveFiles = saveGameManager.listSaveFiles()
            if (saveFiles.isEmpty()) {
                AlertDialog
                    .Builder(this@MainActivity)
                    .setTitle("No Saved Games")
                    .setMessage("No saved games found. Start a new game?")
                    .setPositiveButton("New Game") { _, _ -> showNewGameDialog() }
                    .setNegativeButton("Cancel", null)
                    .show()
                return@launch
            }

            // Group by auto-save vs manual save
            val manualSaves = saveFiles.filter { !it.isAutoSave }
            val autoSaves = saveFiles.filter { it.isAutoSave }

            val items = mutableListOf<String>()
            val saveIds = mutableListOf<String>()

            if (manualSaves.isNotEmpty()) {
                items.add("--- Saved Games ---")
                saveIds.add("")
                manualSaves.forEach { save ->
                    val saveDate = dateFormat.format(Date(save.lastSaved))
                    items.add("${save.playerName} - Chapter ${save.campaignLevel}\n$saveDate")
                    saveIds.add(save.saveId)
                }
            }

            if (autoSaves.isNotEmpty()) {
                items.add("--- Auto-Saves ---")
                saveIds.add("")
                autoSaves.forEach { save ->
                    val saveDate = dateFormat.format(Date(save.lastSaved))
                    items.add("${save.playerName} - Chapter ${save.campaignLevel} (Auto)\n$saveDate")
                    saveIds.add(save.saveId)
                }
            }

            AlertDialog
                .Builder(this@MainActivity)
                .setTitle("Continue Game")
                .setItems(items.toTypedArray()) { _, which ->
                    val saveId = saveIds.getOrNull(which)
                    if (!saveId.isNullOrEmpty()) {
                        val selectedSave = saveFiles.find { it.saveId == saveId }
                        selectedSave?.let {
                            startGame(saveId = saveId, playerName = it.playerName, isNewGame = false)
                        }
                    }
                }.setNegativeButton("Cancel", null)
                .setNeutralButton("Manage Saves") { _, _ -> showManageSavesDialog() }
                .show()
        }
    }

    private fun showManageSavesDialog() {
        lifecycleScope.launch {
            // Only show manual saves for management
            val saveFiles = saveGameManager.listSaveFiles().filter { !it.isAutoSave }
            if (saveFiles.isEmpty()) {
                AlertDialog
                    .Builder(this@MainActivity)
                    .setTitle("No Saved Games")
                    .setMessage("No saved games to manage.")
                    .setPositiveButton("OK", null)
                    .show()
                return@launch
            }

            val fileNames =
                saveFiles
                    .map {
                        "${it.playerName} - Chapter ${it.campaignLevel}\n${dateFormat.format(Date(it.lastSaved))}"
                    }.toTypedArray()

            AlertDialog
                .Builder(this@MainActivity)
                .setTitle("Manage Save Games")
                .setItems(fileNames) { _, which ->
                    val selectedSave = saveFiles[which]
                    showSaveOptionsDialog(selectedSave)
                }.setNegativeButton("Back", null)
                .show()
        }
    }

    private fun showSaveOptionsDialog(saveFile: SaveGameManager.SaveFileInfo) {
        val options = arrayOf("Load Game", "Delete Save")

        AlertDialog
            .Builder(this)
            .setTitle("${saveFile.playerName} - Chapter ${saveFile.campaignLevel}")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> startGame(saveId = saveFile.saveId, playerName = saveFile.playerName, isNewGame = false)
                    1 -> confirmDeleteSave(saveFile)
                }
            }.setNegativeButton("Cancel", null)
            .show()
    }

    private fun confirmDeleteSave(saveFile: SaveGameManager.SaveFileInfo) {
        AlertDialog
            .Builder(this)
            .setTitle("Delete Save Game")
            .setMessage(
                "Are you sure you want to delete this save?\n\n${saveFile.playerName} - Chapter ${saveFile.campaignLevel}\n${
                    dateFormat.format(Date(saveFile.lastSaved))
                }",
            ).setPositiveButton("Delete") { _, _ ->
                lifecycleScope.launch {
                    saveGameManager.deleteSave(saveFile.saveId).fold(
                        onSuccess = {
                            updateContinueButton()
                            showManageSavesDialog()
                        },
                        onFailure = { error ->
                            AlertDialog
                                .Builder(this@MainActivity)
                                .setTitle("Error")
                                .setMessage("Failed to delete save: ${error.message}")
                                .setPositiveButton("OK", null)
                                .show()
                        },
                    )
                }
            }.setNegativeButton("Cancel", null)
            .show()
    }

    private fun showSettingsDialog() {
        val profile = playerProfile ?: PlayerProfile(playerName = "Player")
        val preferences = profile.preferences

        val items =
            arrayOf(
                "Player Name: ${profile.playerName}",
                "Music: ${if (preferences.musicEnabled) "On" else "Off"}",
                "Sound Effects: ${if (preferences.soundEffectsEnabled) "On" else "Off"}",
                "Auto-save: ${if (preferences.autoSaveEnabled) "On" else "Off"}",
                "Auto-save Frequency: Every ${preferences.autoSaveFrequency} turns",
                "Animation Speed: ${preferences.animationSpeed}x",
                "Show Damage Numbers: ${if (preferences.showDamageNumbers) "On" else "Off"}",
            )

        AlertDialog
            .Builder(this)
            .setTitle("Settings")
            .setItems(items) { _, which ->
                when (which) {
                    0 -> editPlayerName(profile)
                    1 -> toggleSetting(profile, "music")
                    2 -> toggleSetting(profile, "soundEffects")
                    3 -> toggleSetting(profile, "autoSave")
                    4 -> editAutoSaveFrequency(profile)
                    5 -> editAnimationSpeed(profile)
                    6 -> toggleSetting(profile, "damageNumbers")
                }
            }.setNegativeButton("Close", null)
            .show()
    }

    private fun editPlayerName(profile: PlayerProfile) {
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
                val updatedProfile = profile.copy(playerName = newName)
                saveGameManager.saveProfile(updatedProfile)
                playerProfile = updatedProfile
                loadPlayerProfile() // Refresh UI
            }.setNegativeButton("Cancel", null)
            .show()
    }

    private fun toggleSetting(
        profile: PlayerProfile,
        setting: String,
    ) {
        val preferences = profile.preferences
        val updatedPreferences =
            when (setting) {
                "music" -> preferences.copy(musicEnabled = !preferences.musicEnabled)
                "soundEffects" -> preferences.copy(soundEffectsEnabled = !preferences.soundEffectsEnabled)
                "autoSave" -> preferences.copy(autoSaveEnabled = !preferences.autoSaveEnabled)
                "damageNumbers" -> preferences.copy(showDamageNumbers = !preferences.showDamageNumbers)
                else -> preferences
            }

        val updatedProfile = profile.copy(preferences = updatedPreferences)
        saveGameManager.saveProfile(updatedProfile)
        playerProfile = updatedProfile
        showSettingsDialog() // Refresh the dialog
    }

    private fun editAutoSaveFrequency(profile: PlayerProfile) {
        val frequencies = arrayOf("Every turn", "Every 3 turns", "Every 5 turns", "Every 10 turns")
        val values = arrayOf(1, 3, 5, 10)
        val currentIndex = values.indexOf(profile.preferences.autoSaveFrequency).takeIf { it >= 0 } ?: 2

        AlertDialog
            .Builder(this)
            .setTitle("Auto-save Frequency")
            .setSingleChoiceItems(frequencies, currentIndex) { dialog, which ->
                val preferences = profile.preferences.copy(autoSaveFrequency = values[which])
                val updatedProfile = profile.copy(preferences = preferences)
                saveGameManager.saveProfile(updatedProfile)
                playerProfile = updatedProfile
                dialog.dismiss()
                showSettingsDialog()
            }.setNegativeButton("Cancel", null)
            .show()
    }

    private fun editAnimationSpeed(profile: PlayerProfile) {
        val speeds = arrayOf("Slow (0.5x)", "Normal (1.0x)", "Fast (1.5x)", "Very Fast (2.0x)")
        val values = arrayOf(0.5f, 1.0f, 1.5f, 2.0f)
        val currentIndex = values.indexOf(profile.preferences.animationSpeed).takeIf { it >= 0 } ?: 1

        AlertDialog
            .Builder(this)
            .setTitle("Animation Speed")
            .setSingleChoiceItems(speeds, currentIndex) { dialog, which ->
                val preferences = profile.preferences.copy(animationSpeed = values[which])
                val updatedProfile = profile.copy(preferences = preferences)
                saveGameManager.saveProfile(updatedProfile)
                playerProfile = updatedProfile
                dialog.dismiss()
                showSettingsDialog()
            }.setNegativeButton("Cancel", null)
            .show()
    }

    private fun showAboutDialog() {
        val profile = playerProfile
        val statsText =
            if (profile != null) {
                """
                Player Statistics:
                
                Total Play Time: ${formatPlayTime(profile.totalPlayTime)}
                Campaigns Completed: ${profile.campaignsCompleted}
                Total Battles Won: ${profile.totalBattlesWon}
                Highest Character Level: ${profile.highestLevel}
                
                
                """.trimIndent()
            } else {
                ""
            }

        val aboutText =
            """
            ${statsText}Open Tactics v1.0
            
            A tactical RPG inspired by classic Fire Emblem games.
            
            Features:
            • Grid-based tactical combat
            • 5 character classes with unique abilities
            • Turn-based strategy gameplay  
            • Save/Load system with auto-save
            • Campaign progression
            • Character leveling and growth
            
            Developed with modern Android architecture
            Built with Kotlin and Material Design
            """.trimIndent()

        AlertDialog
            .Builder(this)
            .setTitle("About Open Tactics")
            .setMessage(aboutText)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun formatPlayTime(milliseconds: Long): String {
        val hours = milliseconds / (1000 * 60 * 60)
        val minutes = (milliseconds % (1000 * 60 * 60)) / (1000 * 60)
        return if (hours > 0) {
            "${hours}h ${minutes}m"
        } else {
            "${minutes}m"
        }
    }

    private fun startGame(
        saveId: String? = null,
        playerName: String,
        isNewGame: Boolean,
    ) {
        if (isNewGame && saveId == null) {
            // For new games, show chapter selection
            val intent =
                Intent(this, ChapterSelectActivity::class.java).apply {
                    putExtra(ChapterSelectActivity.EXTRA_PLAYER_NAME, playerName)
                    putExtra(ChapterSelectActivity.EXTRA_UNLOCKED_CHAPTER, 1) // Start with chapter 1
                }
            startActivity(intent)
        } else {
            // For loading saved games, go directly to game
            val intent =
                Intent(this, GameActivity::class.java).apply {
                    putExtra(GameActivity.EXTRA_PLAYER_NAME, playerName)
                    putExtra(GameActivity.EXTRA_IS_NEW_GAME, isNewGame)
                    saveId?.let { putExtra(GameActivity.EXTRA_LOAD_SAVE_ID, it) }
                }
            startActivity(intent)
        }
    }

    private fun updateContinueButton() {
        lifecycleScope.launch {
            val hasSaveFiles = saveGameManager.listSaveFiles().isNotEmpty()
            binding.btnContinue.isEnabled = hasSaveFiles
        }
    }
}
