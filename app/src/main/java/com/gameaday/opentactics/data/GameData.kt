package com.gameaday.opentactics.data

import android.os.Parcelable
import com.gameaday.opentactics.model.Character
import com.gameaday.opentactics.model.Team
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import java.util.UUID

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
    val difficulty: DifficultyMode = DifficultyMode.NORMAL,
) : Parcelable

/**
 * Difficulty modes that scale enemy stats and experience gain.
 * - Easy: enemies have -20% stats, EXP gain +20%
 * - Normal: default stats and EXP
 * - Hard: enemies have +20% stats, EXP gain -20%
 */
@Serializable
@Suppress("MagicNumber") // Difficulty tuning constants are self-explanatory in enum context
enum class DifficultyMode(
    val displayName: String,
    val enemyStatMultiplier: Float,
    val expMultiplier: Float,
) {
    EASY("Easy", 0.8f, 1.2f),
    NORMAL("Normal", 1.0f, 1.0f),
    HARD("Hard", 1.2f, 0.8f),
}

/**
 * Achievement definitions for tracking player milestones.
 * Each achievement has an id, display name, description, and check condition category.
 */
@Serializable
data class Achievement(
    val id: String,
    val name: String,
    val description: String,
)

/**
 * Repository of all available achievements
 */
object AchievementRepository {
    // Achievement threshold constants
    private const val VETERAN_BATTLES = 10
    private const val MAX_LEVEL_THRESHOLD = 20
    private const val SEASONED_WARRIOR_BATTLES = 5
    private const val CHAPTER_5_BATTLES = 5
    private const val CHAPTER_10_BATTLES = 10
    private const val CHAPTER_15_BATTLES = 15
    private const val CHAPTER_20_BATTLES = 20

    fun getAchievement(id: String): Achievement? = achievements[id]

    fun getAllAchievements(): List<Achievement> = achievements.values.toList()

    /**
     * Check which achievements a player has newly earned based on their profile.
     * Returns list of achievement IDs that are newly unlocked.
     */
    fun checkNewAchievements(profile: PlayerProfile): List<String> {
        val already = profile.achievementsUnlocked.toSet()
        val earned = mutableListOf<String>()
        if ("first_victory" !in already && profile.totalBattlesWon >= 1) earned.add("first_victory")
        if ("veteran" !in already && profile.totalBattlesWon >= VETERAN_BATTLES) earned.add("veteran")
        if ("campaign_complete" !in already && profile.campaignsCompleted >= 1) earned.add("campaign_complete")
        if ("max_level" !in already && profile.highestLevel >= MAX_LEVEL_THRESHOLD) earned.add("max_level")
        if ("hard_mode" !in already &&
            profile.campaignsCompleted >= 1 &&
            profile.preferences.difficulty == DifficultyMode.HARD
        ) {
            earned.add("hard_mode")
        }
        if ("seasoned_warrior" !in already && profile.totalBattlesWon >= SEASONED_WARRIOR_BATTLES) {
            earned.add("seasoned_warrior")
        }
        if ("chapter_5" !in already && profile.totalBattlesWon >= CHAPTER_5_BATTLES) earned.add("chapter_5")
        if ("chapter_10" !in already && profile.totalBattlesWon >= CHAPTER_10_BATTLES) earned.add("chapter_10")
        if ("chapter_15" !in already && profile.totalBattlesWon >= CHAPTER_15_BATTLES) earned.add("chapter_15")
        if ("chapter_20" !in already && profile.totalBattlesWon >= CHAPTER_20_BATTLES) earned.add("chapter_20")
        return earned
    }

    private val achievements =
        mapOf(
            "first_victory" to
                Achievement(
                    id = "first_victory",
                    name = "First Victory",
                    description = "Win your first battle",
                ),
            "veteran" to
                Achievement(
                    id = "veteran",
                    name = "Veteran Commander",
                    description = "Win 10 battles",
                ),
            "campaign_complete" to
                Achievement(
                    id = "campaign_complete",
                    name = "Liberator",
                    description = "Complete the full campaign",
                ),
            "max_level" to
                Achievement(
                    id = "max_level",
                    name = "Legendary Hero",
                    description = "Reach level 20 with any unit",
                ),
            "hard_mode" to
                Achievement(
                    id = "hard_mode",
                    name = "True Tactician",
                    description = "Complete the campaign on Hard difficulty",
                ),
            "chapter_5" to
                Achievement(
                    id = "chapter_5",
                    name = "Act I Complete",
                    description = "Complete Act 1: Defense",
                ),
            "chapter_10" to
                Achievement(
                    id = "chapter_10",
                    name = "Act II Complete",
                    description = "Complete Act 2: Counterattack",
                ),
            "chapter_15" to
                Achievement(
                    id = "chapter_15",
                    name = "Act III Complete",
                    description = "Complete Act 3: Invasion",
                ),
            "chapter_20" to
                Achievement(
                    id = "chapter_20",
                    name = "Dawn of Victory",
                    description = "Complete the Finale",
                ),
            "seasoned_warrior" to
                Achievement(
                    id = "seasoned_warrior",
                    name = "Seasoned Warrior",
                    description = "Win 5 battles",
                ),
        )
}
