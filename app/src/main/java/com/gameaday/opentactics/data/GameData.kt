package com.gameaday.opentactics.data

import android.os.Parcelable
import com.gameaday.opentactics.model.Character
import com.gameaday.opentactics.model.Team
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import java.util.*

@Parcelize
@Serializable
data class GameSave(
    val saveId: String = UUID.randomUUID().toString(),
    val playerName: String,
    val campaignLevel: Int,
    val totalPlayTime: Long,
    val lastSaved: Long = System.currentTimeMillis(),
    val gameState: SavedGameState,
    val isAutoSave: Boolean = false,
) : Parcelable

@Parcelize
@Serializable
data class SavedGameState(
    val boardWidth: Int,
    val boardHeight: Int,
    val playerCharacters: List<Character>,
    val enemyCharacters: List<Character>,
    val currentTurn: Team,
    val turnCount: Int,
    val campaignProgress: CampaignProgress,
) : Parcelable

@Parcelize
@Serializable
data class CampaignProgress(
    val currentChapter: Int = 1,
    val chaptersCompleted: Int = 0,
    val totalBattlesWon: Int = 0,
    val totalExperienceGained: Int = 0,
    val unitsLost: Int = 0,
) : Parcelable

@Parcelize
@Serializable
data class PlayerProfile(
    val profileId: String = UUID.randomUUID().toString(),
    val playerName: String,
    val totalPlayTime: Long = 0,
    val campaignsCompleted: Int = 0,
    val highestLevel: Int = 1,
    val totalBattlesWon: Int = 0,
    val achievementsUnlocked: List<String> = emptyList(),
    val statisticsUnlocked: Map<String, Int> = emptyMap(),
    val preferences: GamePreferences = GamePreferences(),
) : Parcelable

@Parcelize
@Serializable
data class GamePreferences(
    val musicEnabled: Boolean = true,
    val soundEffectsEnabled: Boolean = true,
    val animationSpeed: Float = 1.0f,
    val showDamageNumbers: Boolean = true,
    val autoSaveEnabled: Boolean = true,
    val autoSaveFrequency: Int = 5, // turns
) : Parcelable
