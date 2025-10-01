@file:Suppress("MagicNumber") // Weapon stats are inherently numeric

package com.gameaday.opentactics.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import kotlinx.serialization.Serializer
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.KSerializer

/**
 * Custom serializer for IntRange
 */
@Serializer(forClass = IntRange::class)
object IntRangeSerializer : KSerializer<IntRange> {
    override val descriptor: SerialDescriptor = 
        PrimitiveSerialDescriptor("IntRange", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: IntRange) {
        encoder.encodeString("${value.first}..${value.last}")
    }

    override fun deserialize(decoder: Decoder): IntRange {
        val string = decoder.decodeString()
        val parts = string.split("..")
        return IntRange(parts[0].toInt(), parts[1].toInt())
    }
}

/**
 * Weapon types follow Fire Emblem triangle system:
 * Sword > Axe > Lance > Sword
 * Bow, Tome, and Staff are neutral
 */
@Serializable
enum class WeaponType {
    SWORD,
    LANCE,
    AXE,
    BOW,
    TOME,
    STAFF,
}

@Serializable
enum class WeaponRank {
    E, // Beginner
    D, // Basic
    C, // Intermediate
    B, // Advanced
    A, // Expert
    S, // Master
}

@Parcelize
@Serializable
data class Weapon(
    val id: String,
    val name: String,
    val type: WeaponType,
    val rank: WeaponRank,
    val might: Int, // Attack power bonus
    val hit: Int, // Hit rate bonus (0-100)
    val critical: Int, // Critical hit bonus (0-100)
    val weight: Int, // Affects speed penalty
    @Serializable(with = IntRangeSerializer::class)
    val range: IntRange, // Attack range (e.g., 1..1 for melee, 2..2 for bow)
    val maxUses: Int, // Durability
    var currentUses: Int = maxUses, // Remaining uses
    val description: String = "",
    val canHeal: Boolean = false, // For healing staves
    val effectiveAgainst: List<CharacterClass> = emptyList(), // Effective damage
) : Parcelable {
    
    val isBroken: Boolean
        get() = currentUses <= 0
    
    val isLowDurability: Boolean
        get() = currentUses <= maxUses / 4
    
    /**
     * Use the weapon once, reducing durability
     * @return true if weapon broke from this use
     */
    fun use(): Boolean {
        if (currentUses > 0) {
            currentUses--
        }
        return isBroken
    }
    
    /**
     * Repair the weapon to full durability
     */
    fun repair() {
        currentUses = maxUses
    }
    
    /**
     * Get weapon triangle advantage against another weapon type
     * @return Advantage multiplier: 1.2 for advantage, 0.8 for disadvantage, 1.0 for neutral
     */
    fun getTriangleBonus(otherType: WeaponType): Double {
        return when {
            // Sword beats Axe
            type == WeaponType.SWORD && otherType == WeaponType.AXE -> 1.2
            // Axe beats Lance
            type == WeaponType.AXE && otherType == WeaponType.LANCE -> 1.2
            // Lance beats Sword
            type == WeaponType.LANCE && otherType == WeaponType.SWORD -> 1.2
            // Reverse disadvantages
            type == WeaponType.AXE && otherType == WeaponType.SWORD -> 0.8
            type == WeaponType.LANCE && otherType == WeaponType.AXE -> 0.8
            type == WeaponType.SWORD && otherType == WeaponType.LANCE -> 0.8
            // All other combinations are neutral
            else -> 1.0
        }
    }
    
    /**
     * Check if this weapon is effective against a character class
     */
    fun isEffectiveAgainst(characterClass: CharacterClass): Boolean {
        return characterClass in effectiveAgainst
    }
    
    companion object {
        // Standard weapon definitions
        
        // Swords
        fun ironSword() = Weapon(
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
        
        fun steelSword() = Weapon(
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
        
        fun silverSword() = Weapon(
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
        fun ironLance() = Weapon(
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
        
        fun steelLance() = Weapon(
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
        fun ironAxe() = Weapon(
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
        
        fun steelAxe() = Weapon(
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
        fun ironBow() = Weapon(
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
        
        fun steelBow() = Weapon(
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
        fun fire() = Weapon(
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
        
        fun thunder() = Weapon(
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
        fun heal() = Weapon(
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
        
        fun mend() = Weapon(
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
        
        // Get default weapon for a character class
        fun getDefaultWeapon(characterClass: CharacterClass): Weapon {
            return when (characterClass) {
                CharacterClass.KNIGHT -> ironSword()
                CharacterClass.ARCHER -> ironBow()
                CharacterClass.MAGE -> fire()
                CharacterClass.HEALER -> heal()
                CharacterClass.THIEF -> ironSword()
                CharacterClass.PEGASUS_KNIGHT -> ironLance()
                CharacterClass.WYVERN_RIDER -> ironAxe()
                CharacterClass.MANAKETE -> ironSword() // Placeholder
                CharacterClass.DRAGON -> ironSword() // Dragons don't use weapons typically
            }
        }
    }
}
