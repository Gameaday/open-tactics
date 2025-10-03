package com.gameaday.opentactics.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

// Support bonus constants for different ranks
private const val RANK_C_ATK_BONUS = 1
private const val RANK_C_SKL_BONUS = 1

private const val RANK_B_ATK_BONUS = 2
private const val RANK_B_DEF_BONUS = 1
private const val RANK_B_SKL_BONUS = 2
private const val RANK_B_LCK_BONUS = 1

private const val RANK_A_ATK_BONUS = 3
private const val RANK_A_DEF_BONUS = 2
private const val RANK_A_SPD_BONUS = 1
private const val RANK_A_SKL_BONUS = 3
private const val RANK_A_LCK_BONUS = 2

private const val RANK_S_ATK_BONUS = 5
private const val RANK_S_DEF_BONUS = 3
private const val RANK_S_SPD_BONUS = 2
private const val RANK_S_SKL_BONUS = 5
private const val RANK_S_LCK_BONUS = 3

/**
 * Support rank between two characters
 */
@Serializable
enum class SupportRank {
    NONE,
    C,
    B,
    A,
    S, // Special rank for paired endings
}

/**
 * Represents a support relationship between two characters
 * Support provides combat bonuses when units are adjacent
 */
@Parcelize
@Serializable
data class SupportRelationship(
    val characterId1: String,
    val characterId2: String,
    var rank: SupportRank = SupportRank.NONE,
    var conversationsSeen: Int = 0, // Number of support conversations unlocked
) : Parcelable {
    /**
     * Get the bonus stats provided by this support when characters are adjacent
     */
    fun getBonuses(): Stats =
        when (rank) {
            SupportRank.NONE -> Stats(0, 0, 0, 0, 0, 0, 0)
            SupportRank.C -> Stats(0, 0, RANK_C_ATK_BONUS, 0, 0, RANK_C_SKL_BONUS, 0)
            SupportRank.B ->
                Stats(
                    0,
                    0,
                    RANK_B_ATK_BONUS,
                    RANK_B_DEF_BONUS,
                    0,
                    RANK_B_SKL_BONUS,
                    RANK_B_LCK_BONUS,
                )
            SupportRank.A ->
                Stats(
                    0,
                    0,
                    RANK_A_ATK_BONUS,
                    RANK_A_DEF_BONUS,
                    RANK_A_SPD_BONUS,
                    RANK_A_SKL_BONUS,
                    RANK_A_LCK_BONUS,
                )
            SupportRank.S ->
                Stats(
                    0,
                    0,
                    RANK_S_ATK_BONUS,
                    RANK_S_DEF_BONUS,
                    RANK_S_SPD_BONUS,
                    RANK_S_SKL_BONUS,
                    RANK_S_LCK_BONUS,
                )
        }

    /**
     * Check if this relationship involves the given character
     */
    fun involves(characterId: String): Boolean = characterId1 == characterId || characterId2 == characterId

    /**
     * Get the other character in this relationship
     */
    fun getOtherCharacter(characterId: String): String? =
        when (characterId) {
            characterId1 -> characterId2
            characterId2 -> characterId1
            else -> null
        }
}
