@file:Suppress("MagicNumber")

package com.gameaday.opentactics.data

import android.os.Parcelable
import com.gameaday.opentactics.model.AIBehavior
import com.gameaday.opentactics.model.ChapterObjective
import com.gameaday.opentactics.model.CharacterClass
import com.gameaday.opentactics.model.MapLayout
import com.gameaday.opentactics.model.Position
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import java.util.UUID

/**
 * Configuration for a custom map/encounter.
 * Stores all settings the user selects when creating a custom game.
 */
@Parcelize
@Serializable
data class CustomMapConfig(
    val configId: String = UUID.randomUUID().toString(),
    val name: String = "Custom Battle",
    val mapLayout: MapLayout = MapLayout.PLAINS_BATTLE,
    val objective: ChapterObjective = ChapterObjective.DEFEAT_ALL_ENEMIES,
    val turnLimit: Int? = null,
    val playerUnits: List<CustomUnitConfig> = defaultPlayerUnits(),
    val enemyUnits: List<CustomUnitConfig> = defaultEnemyUnits(),
    val lastModified: Long = System.currentTimeMillis(),
) : Parcelable {
    companion object {
        /** Default player unit configurations */
        fun defaultPlayerUnits(): List<CustomUnitConfig> =
            listOf(
                CustomUnitConfig(
                    name = "Knight",
                    characterClass = CharacterClass.KNIGHT,
                    level = 1,
                    position = Position(1, 6),
                    team = CustomUnitTeam.PLAYER,
                    weaponIds = listOf("iron_sword"),
                    itemIds = listOf("vulnerary"),
                ),
                CustomUnitConfig(
                    name = "Archer",
                    characterClass = CharacterClass.ARCHER,
                    level = 1,
                    position = Position(2, 7),
                    team = CustomUnitTeam.PLAYER,
                    weaponIds = listOf("iron_bow"),
                    itemIds = listOf("vulnerary"),
                ),
                CustomUnitConfig(
                    name = "Mage",
                    characterClass = CharacterClass.MAGE,
                    level = 1,
                    position = Position(0, 7),
                    team = CustomUnitTeam.PLAYER,
                    weaponIds = listOf("fire"),
                    itemIds = listOf("tonic"),
                ),
            )

        /** Default enemy unit configurations */
        fun defaultEnemyUnits(): List<CustomUnitConfig> =
            listOf(
                CustomUnitConfig(
                    name = "Enemy Knight",
                    characterClass = CharacterClass.KNIGHT,
                    level = 1,
                    position = Position(10, 1),
                    team = CustomUnitTeam.ENEMY,
                    weaponIds = listOf("iron_sword"),
                    aiType = AIBehavior.AGGRESSIVE,
                ),
                CustomUnitConfig(
                    name = "Enemy Archer",
                    characterClass = CharacterClass.ARCHER,
                    level = 1,
                    position = Position(11, 0),
                    team = CustomUnitTeam.ENEMY,
                    weaponIds = listOf("iron_bow"),
                    aiType = AIBehavior.AGGRESSIVE,
                ),
            )

        /** Maximum number of units per side */
        const val MAX_PLAYER_UNITS = 8
        const val MAX_ENEMY_UNITS = 12
        const val MAX_UNIT_LEVEL = 20
    }
}

/**
 * Team designation for custom unit configuration.
 */
@Serializable
enum class CustomUnitTeam {
    PLAYER,
    ENEMY,
}

/**
 * Configuration for a single unit in custom mode.
 */
@Parcelize
@Serializable
data class CustomUnitConfig(
    val name: String,
    val characterClass: CharacterClass,
    val level: Int = 1,
    val position: Position = Position(0, 0),
    val team: CustomUnitTeam = CustomUnitTeam.PLAYER,
    val weaponIds: List<String> = emptyList(),
    val itemIds: List<String> = emptyList(),
    val isBoss: Boolean = false,
    val aiType: AIBehavior = AIBehavior.AGGRESSIVE,
) : Parcelable
