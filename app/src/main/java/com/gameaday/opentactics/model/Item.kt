@file:Suppress("MagicNumber") // Item stats are inherently numeric

package com.gameaday.opentactics.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

/**
 * Types of consumable items
 */
@Serializable
enum class ItemType {
    HEALING, // Restores HP
    MANA, // Restores MP
    STAT_BOOST, // Temporarily boosts stats
    CONSUMABLE, // General consumables
}

/**
 * Consumable items that can be used in battle
 */
@Parcelize
@Serializable
data class Item(
    val id: String,
    val name: String,
    val type: ItemType,
    val description: String,
    val healAmount: Int = 0, // HP restored
    val manaAmount: Int = 0, // MP restored
    val maxUses: Int = 1, // Number of uses (usually 1 for consumables)
    var currentUses: Int = maxUses, // Remaining uses
    val canUseInBattle: Boolean = true, // Can be used during battle
    val canUseOnAllies: Boolean = true, // Can be used on allies
    val canUseOnSelf: Boolean = true, // Can be used on self
    val range: Int = 1, // Usage range (adjacent for most items)
) : Parcelable {
    val isUsedUp: Boolean
        get() = currentUses <= 0

    /**
     * Use the item once, reducing uses
     * @return true if item is used up
     */
    fun use(): Boolean {
        if (currentUses > 0) {
            currentUses--
        }
        return isUsedUp
    }

    companion object {
        // Common healing items
        fun vulnerary() =
            Item(
                id = "vulnerary",
                name = "Vulnerary",
                type = ItemType.HEALING,
                description = "Restores 10 HP",
                healAmount = 10,
                maxUses = 3,
            )

        fun concoction() =
            Item(
                id = "concoction",
                name = "Concoction",
                type = ItemType.HEALING,
                description = "Restores 20 HP",
                healAmount = 20,
                maxUses = 3,
            )

        fun elixir() =
            Item(
                id = "elixir",
                name = "Elixir",
                type = ItemType.HEALING,
                description = "Fully restores HP",
                healAmount = 999, // Large number to represent full heal
                maxUses = 1,
            )

        fun tonic() =
            Item(
                id = "tonic",
                name = "Tonic",
                type = ItemType.MANA,
                description = "Restores 10 MP",
                manaAmount = 10,
                maxUses = 3,
            )

        fun elixirPlus() =
            Item(
                id = "elixir_plus",
                name = "Elixir+",
                type = ItemType.HEALING,
                description = "Fully restores HP and MP",
                healAmount = 999,
                manaAmount = 999,
                maxUses = 1,
            )

        // Get item by ID
        fun getItemById(id: String): Item? =
            when (id) {
                "vulnerary" -> vulnerary()
                "concoction" -> concoction()
                "elixir" -> elixir()
                "tonic" -> tonic()
                "elixir_plus" -> elixirPlus()
                else -> null
            }
    }
}
