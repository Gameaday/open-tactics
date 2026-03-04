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

/**
 * Support conversation content for character pairs.
 * Conversations unlock as support rank increases.
 */
@Serializable
data class SupportConversation(
    val characterId1: String,
    val characterId2: String,
    val rank: SupportRank,
    val dialogue: String,
)

/**
 * Repository of support conversations between named characters
 */
object SupportConversationRepository {
    /**
     * Get the conversation for a specific pair and rank
     */
    fun getConversation(
        charId1: String,
        charId2: String,
        rank: SupportRank,
    ): SupportConversation? {
        val key = pairKey(charId1, charId2)
        return conversations[key]?.find { it.rank == rank }
    }

    /**
     * Get all conversations for a character pair
     */
    fun getConversationsForPair(
        charId1: String,
        charId2: String,
    ): List<SupportConversation> {
        val key = pairKey(charId1, charId2)
        return conversations[key] ?: emptyList()
    }

    private fun pairKey(
        id1: String,
        id2: String,
    ): String {
        val sorted = listOf(id1, id2).sorted()
        return "${sorted[0]}|${sorted[1]}"
    }

    private val conversations: Map<String, List<SupportConversation>> =
        mapOf(
            // Sir Garrett & Lyanna
            pairKey("player_knight", "player_archer") to
                listOf(
                    SupportConversation(
                        characterId1 = "player_knight",
                        characterId2 = "player_archer",
                        rank = SupportRank.C,
                        dialogue =
                            "Garrett: You're quite the marksman, Lyanna. " +
                                "I've never seen anyone shoot like that.\n" +
                                "Lyanna: And I've never seen a knight charge headlong " +
                                "into danger with such... enthusiasm.\n" +
                                "Garrett: Ha! Someone has to keep the enemy busy " +
                                "while you line up your shots.\n" +
                                "Lyanna: Just try not to stand in my line of fire, okay?",
                    ),
                    SupportConversation(
                        characterId1 = "player_knight",
                        characterId2 = "player_archer",
                        rank = SupportRank.B,
                        dialogue =
                            "Lyanna: Garrett, why did you become a knight?\n" +
                                "Garrett: My father was a knight. His father before him. " +
                                "It's in my blood.\n" +
                                "Lyanna: That's not really a reason, you know.\n" +
                                "Garrett: ...You're right. I became a knight because " +
                                "I saw what happens when good people don't fight. " +
                                "I lost my village to bandits when I was twelve.\n" +
                                "Lyanna: I'm sorry. I lost my family to the same kind of people.\n" +
                                "Garrett: Then we fight for the same reason.",
                    ),
                    SupportConversation(
                        characterId1 = "player_knight",
                        characterId2 = "player_archer",
                        rank = SupportRank.A,
                        dialogue =
                            "Garrett: Lyanna, after this war is over... " +
                                "what will you do?\n" +
                                "Lyanna: I've thought about opening an archery school. " +
                                "Teaching the next generation to defend themselves.\n" +
                                "Garrett: That's a fine idea. " +
                                "Maybe I could help — teach them swordplay too.\n" +
                                "Lyanna: Sir Garrett, running a school with me? " +
                                "People would talk.\n" +
                                "Garrett: Let them. We make a good team.",
                    ),
                ),
            // Sir Garrett & Aldric
            pairKey("player_knight", "player_mage") to
                listOf(
                    SupportConversation(
                        characterId1 = "player_knight",
                        characterId2 = "player_mage",
                        rank = SupportRank.C,
                        dialogue =
                            "Aldric: Garrett, you should stand behind me sometimes. " +
                                "My spells have range — your sword doesn't.\n" +
                                "Garrett: Where's the honor in hiding behind a mage?\n" +
                                "Aldric: It's called tactics, not hiding.\n" +
                                "Garrett: I'll consider it. After the next charge.",
                    ),
                    SupportConversation(
                        characterId1 = "player_knight",
                        characterId2 = "player_mage",
                        rank = SupportRank.B,
                        dialogue =
                            "Garrett: That spell you cast earlier — " +
                                "I felt the heat from twenty paces away.\n" +
                                "Aldric: Thunder magic is raw power. " +
                                "Hard to control, but devastating.\n" +
                                "Garrett: Must be tempting to use that power freely.\n" +
                                "Aldric: Every day. But power without discipline " +
                                "is just destruction. My mentor taught me that.\n" +
                                "Garrett: Sounds like a wise person.\n" +
                                "Aldric: She was. The Crimson Empire took her from me.",
                    ),
                    SupportConversation(
                        characterId1 = "player_knight",
                        characterId2 = "player_mage",
                        rank = SupportRank.A,
                        dialogue =
                            "Aldric: Garrett, if I fall in the final battle, " +
                                "promise me you'll destroy my grimoire.\n" +
                                "Garrett: Don't talk like that. We'll both make it through.\n" +
                                "Aldric: Promise me. The dark magic inside it " +
                                "must never fall into the wrong hands.\n" +
                                "Garrett: ...I promise. But you're going to burn it yourself, " +
                                "old friend. After we win.\n" +
                                "Aldric: I'll hold you to that.",
                    ),
                ),
            // Lyanna & Aldric
            pairKey("player_archer", "player_mage") to
                listOf(
                    SupportConversation(
                        characterId1 = "player_archer",
                        characterId2 = "player_mage",
                        rank = SupportRank.C,
                        dialogue =
                            "Lyanna: Can you enchant my arrows, Aldric? " +
                                "Fire arrows would be useful.\n" +
                                "Aldric: Enchanting weapons takes considerable energy. " +
                                "I'd be useless in the next battle.\n" +
                                "Lyanna: What good is a mage who can't even enchant arrows?\n" +
                                "Aldric: What good is an archer who can't hit without magic?\n" +
                                "Lyanna: ...Touché.",
                    ),
                    SupportConversation(
                        characterId1 = "player_archer",
                        characterId2 = "player_mage",
                        rank = SupportRank.B,
                        dialogue =
                            "Aldric: Lyanna, I owe you an apology. " +
                                "That comment about your aim was uncalled for.\n" +
                                "Lyanna: Already forgotten. I know you mages " +
                                "get cranky when you're low on mana.\n" +
                                "Aldric: Ha! I suppose I do. " +
                                "Truth is, your precision astounds me. " +
                                "Magic is... imprecise by nature.\n" +
                                "Lyanna: And your power astounds me. " +
                                "We each have what the other lacks.\n" +
                                "Aldric: A perfect partnership, then.",
                    ),
                ),
            // Garrett & Elara
            pairKey("player_knight", "healer_1") to
                listOf(
                    SupportConversation(
                        characterId1 = "player_knight",
                        characterId2 = "healer_1",
                        rank = SupportRank.C,
                        dialogue =
                            "Elara: Sir Garrett, please try not to get wounded so often.\n" +
                                "Garrett: I'm a frontline fighter. It comes with the job.\n" +
                                "Elara: I'm running low on healing supplies " +
                                "just keeping you alive.\n" +
                                "Garrett: ...I'll try to dodge more.",
                    ),
                    SupportConversation(
                        characterId1 = "player_knight",
                        characterId2 = "healer_1",
                        rank = SupportRank.B,
                        dialogue =
                            "Garrett: Elara, why did you join the army? " +
                                "Healers don't usually march to war.\n" +
                                "Elara: Because soldiers die without healers. " +
                                "I'd rather face danger than let others face it alone.\n" +
                                "Garrett: That's braver than any charge I've ever led.\n" +
                                "Elara: Bravery isn't about swords, Sir Garrett. " +
                                "It's about being where you're needed most.",
                    ),
                ),
            // Raven & Lyanna
            pairKey("thief_1", "player_archer") to
                listOf(
                    SupportConversation(
                        characterId1 = "thief_1",
                        characterId2 = "player_archer",
                        rank = SupportRank.C,
                        dialogue =
                            "Lyanna: I keep finding my arrowheads rearranged. " +
                                "Care to explain, Raven?\n" +
                                "Raven: Force of habit. I was sorting them by sharpness.\n" +
                                "Lyanna: ...They're actually in better order now.\n" +
                                "Raven: You're welcome.",
                    ),
                    SupportConversation(
                        characterId1 = "thief_1",
                        characterId2 = "player_archer",
                        rank = SupportRank.B,
                        dialogue =
                            "Raven: You know, Lyanna, we're not so different. " +
                                "Both of us strike from a distance.\n" +
                                "Lyanna: I strike with honor. You strike from the shadows.\n" +
                                "Raven: The shadow is just another kind of distance. " +
                                "And the enemy doesn't care about honor when they're down.\n" +
                                "Lyanna: ...I suppose you have a point. " +
                                "Just don't pick my pockets.\n" +
                                "Raven: Wouldn't dream of it. " +
                                "You don't carry anything worth stealing anyway.",
                    ),
                ),
            // Celeste & Aldric
            pairKey("pegasus_knight_1", "player_mage") to
                listOf(
                    SupportConversation(
                        characterId1 = "pegasus_knight_1",
                        characterId2 = "player_mage",
                        rank = SupportRank.C,
                        dialogue =
                            "Celeste: Aldric, is it true mages can make pegasi fly faster?\n" +
                                "Aldric: Theoretically, yes. A wind enchantment could help.\n" +
                                "Celeste: Could you try it sometime?\n" +
                                "Aldric: The last time I tried wind magic on a horse, " +
                                "it threw its rider into a lake.\n" +
                                "Celeste: ...On second thought, Starwind flies fast enough.",
                    ),
                ),
        )
}
