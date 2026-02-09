@file:Suppress("MagicNumber") // Weapon stats are inherently numeric

package com.gameaday.opentactics.factory

import com.gameaday.opentactics.model.CharacterClass
import com.gameaday.opentactics.model.Weapon
import com.gameaday.opentactics.model.WeaponRank
import com.gameaday.opentactics.model.WeaponType

/**
 * Factory for creating weapons for the game.
 * This centralizes weapon creation and makes it easy to create weapons by ID.
 */
object WeaponFactory {
    /**
     * Create a weapon by its ID
     * @param id The weapon ID
     * @return The weapon instance, or null if the ID is not found
     */
    fun createWeapon(id: String): Weapon? =
        when (id) {
            "iron_sword" -> createIronSword()
            "steel_sword" -> createSteelSword()
            "silver_sword" -> createSilverSword()
            "iron_lance" -> createIronLance()
            "steel_lance" -> createSteelLance()
            "iron_axe" -> createIronAxe()
            "steel_axe" -> createSteelAxe()
            "iron_bow" -> createIronBow()
            "steel_bow" -> createSteelBow()
            "fire" -> createFire()
            "thunder" -> createThunder()
            "heal" -> createHeal()
            "mend" -> createMend()
            else -> null
        }

    /**
     * Get default weapon for a character class
     */
    fun getDefaultWeapon(characterClass: CharacterClass): Weapon =
        when (characterClass) {
            CharacterClass.KNIGHT -> createIronSword()
            CharacterClass.ARCHER -> createIronBow()
            CharacterClass.MAGE -> createFire()
            CharacterClass.HEALER -> createHeal()
            CharacterClass.THIEF -> createIronSword()
            CharacterClass.PEGASUS_KNIGHT -> createIronLance()
            CharacterClass.WYVERN_RIDER -> createIronAxe()
            CharacterClass.MANAKETE -> createIronSword() // Placeholder
            CharacterClass.DRAGON -> createIronSword() // Dragons don't use weapons typically
        }

    // Swords
    fun createIronSword() =
        Weapon(
            id = "iron_sword",
            name = "Iron Sword",
            type = WeaponType.SWORD,
            rank = WeaponRank.E,
            might = 5,
            hit = 90,
            critical = 0,
            weight = 5,
            range = 1..1,
            maxUses = 46,
        )

    fun createSteelSword() =
        Weapon(
            id = "steel_sword",
            name = "Steel Sword",
            type = WeaponType.SWORD,
            rank = WeaponRank.D,
            might = 8,
            hit = 85,
            critical = 0,
            weight = 8,
            range = 1..1,
            maxUses = 30,
        )

    fun createSilverSword() =
        Weapon(
            id = "silver_sword",
            name = "Silver Sword",
            type = WeaponType.SWORD,
            rank = WeaponRank.B,
            might = 13,
            hit = 80,
            critical = 0,
            weight = 10,
            range = 1..1,
            maxUses = 20,
        )

    // Lances
    fun createIronLance() =
        Weapon(
            id = "iron_lance",
            name = "Iron Lance",
            type = WeaponType.LANCE,
            rank = WeaponRank.E,
            might = 7,
            hit = 80,
            critical = 0,
            weight = 8,
            range = 1..1,
            maxUses = 45,
        )

    fun createSteelLance() =
        Weapon(
            id = "steel_lance",
            name = "Steel Lance",
            type = WeaponType.LANCE,
            rank = WeaponRank.D,
            might = 10,
            hit = 75,
            critical = 0,
            weight = 11,
            range = 1..1,
            maxUses = 30,
        )

    // Axes
    fun createIronAxe() =
        Weapon(
            id = "iron_axe",
            name = "Iron Axe",
            type = WeaponType.AXE,
            rank = WeaponRank.E,
            might = 8,
            hit = 75,
            critical = 0,
            weight = 10,
            range = 1..1,
            maxUses = 45,
        )

    fun createSteelAxe() =
        Weapon(
            id = "steel_axe",
            name = "Steel Axe",
            type = WeaponType.AXE,
            rank = WeaponRank.D,
            might = 11,
            hit = 70,
            critical = 0,
            weight = 13,
            range = 1..1,
            maxUses = 30,
        )

    // Bows
    fun createIronBow() =
        Weapon(
            id = "iron_bow",
            name = "Iron Bow",
            type = WeaponType.BOW,
            rank = WeaponRank.E,
            might = 6,
            hit = 85,
            critical = 0,
            weight = 6,
            range = 2..2,
            maxUses = 45,
            effectiveAgainst = listOf(CharacterClass.PEGASUS_KNIGHT, CharacterClass.WYVERN_RIDER),
        )

    fun createSteelBow() =
        Weapon(
            id = "steel_bow",
            name = "Steel Bow",
            type = WeaponType.BOW,
            rank = WeaponRank.D,
            might = 9,
            hit = 80,
            critical = 0,
            weight = 9,
            range = 2..2,
            maxUses = 30,
            effectiveAgainst = listOf(CharacterClass.PEGASUS_KNIGHT, CharacterClass.WYVERN_RIDER),
        )

    // Tomes
    fun createFire() =
        Weapon(
            id = "fire",
            name = "Fire",
            type = WeaponType.TOME,
            rank = WeaponRank.E,
            might = 5,
            hit = 90,
            critical = 0,
            weight = 4,
            range = 1..2,
            maxUses = 40,
        )

    fun createThunder() =
        Weapon(
            id = "thunder",
            name = "Thunder",
            type = WeaponType.TOME,
            rank = WeaponRank.D,
            might = 9,
            hit = 75,
            critical = 5,
            weight = 6,
            range = 1..2,
            maxUses = 35,
        )

    // Staves
    fun createHeal() =
        Weapon(
            id = "heal",
            name = "Heal",
            type = WeaponType.STAFF,
            rank = WeaponRank.E,
            might = 0,
            hit = 100,
            critical = 0,
            weight = 2,
            range = 1..1,
            maxUses = 30,
            canHeal = true,
            description = "Restores 10 HP",
        )

    fun createMend() =
        Weapon(
            id = "mend",
            name = "Mend",
            type = WeaponType.STAFF,
            rank = WeaponRank.C,
            might = 0,
            hit = 100,
            critical = 0,
            weight = 3,
            range = 1..1,
            maxUses = 20,
            canHeal = true,
            description = "Restores 20 HP",
        )

    /**
     * Get all available weapon IDs
     */
    fun getAllWeaponIds(): List<String> =
        listOf(
            "iron_sword",
            "steel_sword",
            "silver_sword",
            "iron_lance",
            "steel_lance",
            "iron_axe",
            "steel_axe",
            "iron_bow",
            "steel_bow",
            "fire",
            "thunder",
            "heal",
            "mend",
        )
}
