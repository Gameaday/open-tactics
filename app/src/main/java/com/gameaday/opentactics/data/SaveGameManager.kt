package com.gameaday.opentactics.data

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

// Maximum number of auto-save files to keep
private const val MAX_AUTO_SAVES = 3

class SaveGameManager(
    private val context: Context,
) {
    private val json =
        Json {
            prettyPrint = true
            ignoreUnknownKeys = true
        }

    @Suppress("DEPRECATION")
    private val masterKey =
        MasterKey
            .Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

    @Suppress("DEPRECATION")
    private val profilePrefs: SharedPreferences by lazy {
        EncryptedSharedPreferences.create(
            context,
            "player_profile",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
        // TODO: Migrate to new androidx.security API when it reaches stable
    }

    private val saveDirectory by lazy {
        File(context.filesDir, "saves").apply {
            if (!exists()) mkdirs()
        }
    }

    private val autoSaveDirectory by lazy {
        File(context.filesDir, "autosaves").apply {
            if (!exists()) mkdirs()
        }
    }

    private val customMapsDirectory by lazy {
        File(context.filesDir, "custom_maps").apply {
            if (!exists()) mkdirs()
        }
    }

    suspend fun saveGame(
        gameSave: GameSave,
        isAutoSave: Boolean = false,
    ): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val directory = if (isAutoSave) autoSaveDirectory else saveDirectory
                val fileName = if (isAutoSave) "autosave_${gameSave.campaignLevel}.json" else "${gameSave.saveId}.json"
                val file = File(directory, fileName)

                val saveData = gameSave.copy(isAutoSave = isAutoSave, lastSaved = System.currentTimeMillis())
                val jsonString = json.encodeToString(saveData)
                file.writeText(jsonString)

                // Keep only the 3 most recent auto-saves
                if (isAutoSave) {
                    cleanupAutoSaves()
                }

                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun loadGame(saveId: String): Result<GameSave> =
        withContext(Dispatchers.IO) {
            try {
                val file = File(saveDirectory, "$saveId.json")
                if (!file.exists()) {
                    return@withContext Result.failure(Exception("Save file not found"))
                }

                val jsonString = file.readText()
                val gameSave = json.decodeFromString<GameSave>(jsonString)
                Result.success(gameSave)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun loadAutoSave(campaignLevel: Int): Result<GameSave> =
        withContext(Dispatchers.IO) {
            try {
                val file = File(autoSaveDirectory, "autosave_$campaignLevel.json")
                if (!file.exists()) {
                    return@withContext Result.failure(Exception("Auto-save not found"))
                }

                val jsonString = file.readText()
                val gameSave = json.decodeFromString<GameSave>(jsonString)
                Result.success(gameSave)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun listSaveFiles(): List<SaveFileInfo> =
        withContext(Dispatchers.IO) {
            try {
                val saves = mutableListOf<SaveFileInfo>()

                // Regular saves
                saveDirectory.listFiles()?.forEach { file ->
                    if (file.extension == "json") {
                        try {
                            val jsonString = file.readText()
                            val gameSave = json.decodeFromString<GameSave>(jsonString)
                            saves.add(
                                SaveFileInfo(
                                    saveId = gameSave.saveId,
                                    playerName = gameSave.playerName,
                                    campaignLevel = gameSave.campaignLevel,
                                    lastSaved = gameSave.lastSaved,
                                    isAutoSave = false,
                                ),
                            )
                        } catch (e: Exception) {
                            // Skip corrupted save files
                        }
                    }
                }

                // Auto-saves
                autoSaveDirectory.listFiles()?.forEach { file ->
                    if (file.extension == "json") {
                        try {
                            val jsonString = file.readText()
                            val gameSave = json.decodeFromString<GameSave>(jsonString)
                            saves.add(
                                SaveFileInfo(
                                    saveId = gameSave.saveId,
                                    playerName = gameSave.playerName,
                                    campaignLevel = gameSave.campaignLevel,
                                    lastSaved = gameSave.lastSaved,
                                    isAutoSave = true,
                                ),
                            )
                        } catch (e: Exception) {
                            // Skip corrupted save files
                        }
                    }
                }

                saves.sortedByDescending { it.lastSaved }
            } catch (e: Exception) {
                emptyList()
            }
        }

    suspend fun deleteSave(saveId: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val file = File(saveDirectory, "$saveId.json")
                if (file.exists() && file.delete()) {
                    Result.success(Unit)
                } else {
                    Result.failure(Exception("Failed to delete save file"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    fun saveProfile(profile: PlayerProfile) {
        val jsonString = json.encodeToString(profile)
        profilePrefs
            .edit()
            .putString("player_profile", jsonString)
            .apply()
    }

    fun loadProfile(): PlayerProfile? =
        try {
            val jsonString = profilePrefs.getString("player_profile", null)
            if (jsonString != null) {
                json.decodeFromString<PlayerProfile>(jsonString)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }

    suspend fun saveCustomMap(config: CustomMapConfig): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val updatedConfig = config.copy(lastModified = System.currentTimeMillis())
                val file = File(customMapsDirectory, "${updatedConfig.configId}.json")
                val jsonString = json.encodeToString(updatedConfig)
                file.writeText(jsonString)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun loadCustomMap(configId: String): Result<CustomMapConfig> =
        withContext(Dispatchers.IO) {
            try {
                val file = File(customMapsDirectory, "$configId.json")
                if (!file.exists()) {
                    return@withContext Result.failure(Exception("Custom map not found"))
                }
                val jsonString = file.readText()
                val config = json.decodeFromString<CustomMapConfig>(jsonString)
                Result.success(config)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun listCustomMaps(): List<CustomMapConfig> =
        withContext(Dispatchers.IO) {
            try {
                customMapsDirectory
                    .listFiles()
                    ?.filter { it.extension == "json" }
                    ?.mapNotNull { file ->
                        try {
                            json.decodeFromString<CustomMapConfig>(file.readText())
                        } catch (e: Exception) {
                            null // Skip corrupted files
                        }
                    }?.sortedByDescending { it.lastModified }
                    ?: emptyList()
            } catch (e: Exception) {
                emptyList()
            }
        }

    suspend fun deleteCustomMap(configId: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val file = File(customMapsDirectory, "$configId.json")
                if (file.exists() && file.delete()) {
                    Result.success(Unit)
                } else {
                    Result.failure(Exception("Failed to delete custom map"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    private fun cleanupAutoSaves() {
        val autoSaves =
            autoSaveDirectory
                .listFiles()
                ?.filter { it.extension == "json" }
                ?.sortedByDescending { it.lastModified() }
                ?: return

        // Keep only the most recent auto-saves
        autoSaves.drop(MAX_AUTO_SAVES).forEach { it.delete() }
    }

    data class SaveFileInfo(
        val saveId: String,
        val playerName: String,
        val campaignLevel: Int,
        val lastSaved: Long,
        val isAutoSave: Boolean,
    )
}
