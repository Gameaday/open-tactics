package com.gameaday.opentactics.factory

import com.gameaday.opentactics.model.ItemType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class ItemFactoryTest {
    @Test
    fun testCreateItemById() {
        val item = ItemFactory.createItem("vulnerary")
        assertNotNull(item)
        assertEquals("Vulnerary", item?.name)
        assertEquals(ItemType.HEALING, item?.type)
    }

    @Test
    fun testCreateItemByIdReturnsNull() {
        val item = ItemFactory.createItem("nonexistent_item")
        assertNull(item)
    }

    @Test
    fun testCreateAllItems() {
        val itemIds = ItemFactory.getAllItemIds()

        for (id in itemIds) {
            val item = ItemFactory.createItem(id)
            assertNotNull("Item $id should not be null", item)
            assertEquals(id, item?.id)
        }
    }

    @Test
    fun testCreateVulnerary() {
        val item = ItemFactory.createVulnerary()

        assertEquals("vulnerary", item.id)
        assertEquals("Vulnerary", item.name)
        assertEquals(ItemType.HEALING, item.type)
        assertEquals(10, item.healAmount)
        assertEquals(3, item.maxUses)
    }

    @Test
    fun testCreateConcoction() {
        val item = ItemFactory.createConcoction()

        assertEquals("concoction", item.id)
        assertEquals("Concoction", item.name)
        assertEquals(ItemType.HEALING, item.type)
        assertEquals(20, item.healAmount)
        assertEquals(3, item.maxUses)
    }

    @Test
    fun testCreateElixir() {
        val item = ItemFactory.createElixir()

        assertEquals("elixir", item.id)
        assertEquals("Elixir", item.name)
        assertEquals(ItemType.HEALING, item.type)
        assertEquals(999, item.healAmount)
        assertEquals(1, item.maxUses)
    }

    @Test
    fun testCreateTonic() {
        val item = ItemFactory.createTonic()

        assertEquals("tonic", item.id)
        assertEquals("Tonic", item.name)
        assertEquals(ItemType.MANA, item.type)
        assertEquals(10, item.manaAmount)
        assertEquals(3, item.maxUses)
    }

    @Test
    fun testCreateElixirPlus() {
        val item = ItemFactory.createElixirPlus()

        assertEquals("elixir_plus", item.id)
        assertEquals("Elixir+", item.name)
        assertEquals(ItemType.HEALING, item.type)
        assertEquals(999, item.healAmount)
        assertEquals(999, item.manaAmount)
        assertEquals(1, item.maxUses)
    }

    @Test
    fun testAllItemsHaveUniqueIds() {
        val itemIds = ItemFactory.getAllItemIds()
        val uniqueIds = itemIds.toSet()

        assertEquals(itemIds.size, uniqueIds.size)
    }

    @Test
    fun testAllItemsHavePositiveMaxUses() {
        val itemIds = ItemFactory.getAllItemIds()

        for (id in itemIds) {
            val item = ItemFactory.createItem(id)!!
            assert(item.maxUses > 0) { "$id should have positive maxUses" }
        }
    }

    @Test
    fun testHealingItemsHavePositiveHealAmount() {
        val healingIds = listOf("vulnerary", "concoction", "elixir", "elixir_plus")

        for (id in healingIds) {
            val item = ItemFactory.createItem(id)!!
            assert(item.healAmount > 0) { "$id should have positive healAmount" }
        }
    }
}
