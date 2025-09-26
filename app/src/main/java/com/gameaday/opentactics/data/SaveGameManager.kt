package com.gameaday.opentactics.data

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import java.io.File

class SaveGameManager(private val context: Context) {
    
    private val json = Json { 
        prettyPrint = true
        ignoreUnknownKeys = true
    }
    
    private val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
    
    private val profilePrefs: SharedPreferences by lazy {
        EncryptedSharedPreferences.create(
            "player_profile",
            masterKeyAlias,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
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
    
    suspend fun saveGame(gameSave: GameSave, isAutoSave: Boolean = false): Result<Unit> = withContext(Dispatchers.IO) {
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
    
    suspend fun loadGame(saveId: String): Result<GameSave> = withContext(Dispatchers.IO) {
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
    
    suspend fun loadAutoSave(campaignLevel: Int): Result<GameSave> = withContext(Dispatchers.IO) {
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
    
    suspend fun listSaveFiles(): List<SaveFileInfo> = withContext(Dispatchers.IO) {
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
                                isAutoSave = false
                            )
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
                                isAutoSave = true
                            )
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
    
    suspend fun deleteSave(saveId: String): Result<Unit> = withContext(Dispatchers.IO) {
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
        profilePrefs.edit()
            .putString("player_profile", jsonString)
            .apply()
    }
    
    fun loadProfile(): PlayerProfile? {
        return try {
            val jsonString = profilePrefs.getString("player_profile", null)
            if (jsonString != null) {
                json.decodeFromString<PlayerProfile>(jsonString)
            } else null
        } catch (e: Exception) {
            null
        }
    }
    
    private fun cleanupAutoSaves() {
        val autoSaves = autoSaveDirectory.listFiles()
            ?.filter { it.extension == "json" }
            ?.sortedByDescending { it.lastModified() }
            ?: return
            
        // Keep only the 3 most recent
        autoSaves.drop(3).forEach { it.delete() }
    }
    
    data class SaveFileInfo(
        val saveId: String,
        val playerName: String,
        val campaignLevel: Int,
        val lastSaved: Long,
        val isAutoSave: Boolean
    )
}