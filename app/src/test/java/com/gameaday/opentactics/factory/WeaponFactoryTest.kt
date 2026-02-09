package com.gameaday.opentactics.factory

import com.gameaday.opentactics.model.CharacterClass
import com.gameaday.opentactics.model.WeaponType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class WeaponFactoryTest {
    @Test
    fun testCreateWeaponById() {
        val sword = WeaponFactory.createWeapon("iron_sword")
        assertNotNull(sword)
        assertEquals("Iron Sword", sword?.name)
        assertEquals(WeaponType.SWORD, sword?.type)
    }

    @Test
    fun testCreateWeaponByIdReturnsNull() {
        val weapon = WeaponFactory.createWeapon("nonexistent_weapon")
        assertNull(weapon)
    }

    @Test
    fun testCreateAllWeapons() {
        val weaponIds = WeaponFactory.getAllWeaponIds()

        for (id in weaponIds) {
            val weapon = WeaponFactory.createWeapon(id)
            assertNotNull("Weapon $id should not be null", weapon)
            assertEquals(id, weapon?.id)
        }
    }

    @Test
    fun testCreateIronSword() {
        val weapon = WeaponFactory.createIronSword()

        assertEquals("iron_sword", weapon.id)
        assertEquals("Iron Sword", weapon.name)
        assertEquals(WeaponType.SWORD, weapon.type)
        assertEquals(5, weapon.might)
        assertEquals(90, weapon.hit)
    }

    @Test
    fun testCreateSteelBow() {
        val weapon = WeaponFactory.createSteelBow()

        assertEquals("steel_bow", weapon.id)
        assertEquals("Steel Bow", weapon.name)
        assertEquals(WeaponType.BOW, weapon.type)
        assertEquals(9, weapon.might)
        assertEquals(2..2, weapon.range)
        assertEquals(2, weapon.effectiveAgainst.size)
    }

    @Test
    fun testCreateHealingStaff() {
        val weapon = WeaponFactory.createHeal()

        assertEquals("heal", weapon.id)
        assertEquals("Heal", weapon.name)
        assertEquals(WeaponType.STAFF, weapon.type)
        assertEquals(true, weapon.canHeal)
        assertEquals("Restores 10 HP", weapon.description)
    }

    @Test
    fun testGetDefaultWeaponForKnight() {
        val weapon = WeaponFactory.getDefaultWeapon(CharacterClass.KNIGHT)

        assertEquals(WeaponType.SWORD, weapon.type)
        assertEquals("iron_sword", weapon.id)
    }

    @Test
    fun testGetDefaultWeaponForArcher() {
        val weapon = WeaponFactory.getDefaultWeapon(CharacterClass.ARCHER)

        assertEquals(WeaponType.BOW, weapon.type)
        assertEquals("iron_bow", weapon.id)
    }

    @Test
    fun testGetDefaultWeaponForMage() {
        val weapon = WeaponFactory.getDefaultWeapon(CharacterClass.MAGE)

        assertEquals(WeaponType.TOME, weapon.type)
        assertEquals("fire", weapon.id)
    }

    @Test
    fun testGetDefaultWeaponForHealer() {
        val weapon = WeaponFactory.getDefaultWeapon(CharacterClass.HEALER)

        assertEquals(WeaponType.STAFF, weapon.type)
        assertEquals("heal", weapon.id)
    }

    @Test
    fun testAllWeaponsHaveUniqueIds() {
        val weaponIds = WeaponFactory.getAllWeaponIds()
        val uniqueIds = weaponIds.toSet()

        assertEquals(weaponIds.size, uniqueIds.size)
    }

    @Test
    fun testAllWeaponsHavePositiveStats() {
        val weaponIds = WeaponFactory.getAllWeaponIds()

        for (id in weaponIds) {
            val weapon = WeaponFactory.createWeapon(id)!!
            assert(weapon.maxUses > 0) { "$id should have positive maxUses" }
            assert(weapon.weight >= 0) { "$id should have non-negative weight" }
            assert(weapon.hit >= 0) { "$id should have non-negative hit" }
            assert(weapon.critical >= 0) { "$id should have non-negative critical" }
        }
    }
}
