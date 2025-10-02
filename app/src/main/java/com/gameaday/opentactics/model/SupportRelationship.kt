package com.gameaday.opentactics.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

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
            SupportRank.C -> Stats(0, 0, 1, 0, 0, 1, 0) // +1 ATK, +1 SKL
            SupportRank.B -> Stats(0, 0, 2, 1, 0, 2, 1) // +2 ATK, +1 DEF, +2 SKL, +1 LCK
            SupportRank.A -> Stats(0, 0, 3, 2, 1, 3, 2) // +3 ATK, +2 DEF, +1 SPD, +3 SKL, +2 LCK
            SupportRank.S -> Stats(0, 0, 5, 3, 2, 5, 3) // +5 ATK, +3 DEF, +2 SPD, +5 SKL, +3 LCK
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
