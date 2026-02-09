@file:Suppress("MagicNumber") // Item stats are inherently numeric

package com.gameaday.opentactics.factory

import com.gameaday.opentactics.model.Item
import com.gameaday.opentactics.model.ItemType

/**
 * Factory for creating items for the game.
 * This centralizes item creation and makes it easy to create items by ID.
 */
object ItemFactory {
    /**
     * Create an item by its ID
     * @param id The item ID
     * @return The item instance, or null if the ID is not found
     */
    fun createItem(id: String): Item? =
        when (id) {
            "vulnerary" -> createVulnerary()
            "concoction" -> createConcoction()
            "elixir" -> createElixir()
            "tonic" -> createTonic()
            "elixir_plus" -> createElixirPlus()
            else -> null
        }

    /**
     * Create a Vulnerary - basic healing item
     */
    fun createVulnerary() =
        Item(
            id = "vulnerary",
            name = "Vulnerary",
            type = ItemType.HEALING,
            description = "Restores 10 HP",
            healAmount = 10,
            maxUses = 3,
        )

    /**
     * Create a Concoction - medium healing item
     */
    fun createConcoction() =
        Item(
            id = "concoction",
            name = "Concoction",
            type = ItemType.HEALING,
            description = "Restores 20 HP",
            healAmount = 20,
            maxUses = 3,
        )

    /**
     * Create an Elixir - full HP restoration
     */
    fun createElixir() =
        Item(
            id = "elixir",
            name = "Elixir",
            type = ItemType.HEALING,
            description = "Fully restores HP",
            healAmount = 999, // Large number to represent full heal
            maxUses = 1,
        )

    /**
     * Create a Tonic - mana restoration
     */
    fun createTonic() =
        Item(
            id = "tonic",
            name = "Tonic",
            type = ItemType.MANA,
            description = "Restores 10 MP",
            manaAmount = 10,
            maxUses = 3,
        )

    /**
     * Create an Elixir+ - full HP and MP restoration
     */
    fun createElixirPlus() =
        Item(
            id = "elixir_plus",
            name = "Elixir+",
            type = ItemType.HEALING,
            description = "Fully restores HP and MP",
            healAmount = 999,
            manaAmount = 999,
            maxUses = 1,
        )

    /**
     * Get all available item IDs
     */
    fun getAllItemIds(): List<String> =
        listOf(
            "vulnerary",
            "concoction",
            "elixir",
            "tonic",
            "elixir_plus",
        )
}
