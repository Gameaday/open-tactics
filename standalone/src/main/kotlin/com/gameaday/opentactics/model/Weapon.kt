package com.gameaday.opentactics.model

// Simplified weapon system for standalone (no serialization needed for console demo)

enum class WeaponType {
    SWORD,
    LANCE,
    AXE,
    BOW,
    TOME,
    STAFF,
}

enum class WeaponRank {
    E, D, C, B, A, S
}

data class Weapon(
    val id: String,
    val name: String,
    val type: WeaponType,
    val rank: WeaponRank,
    val might: Int,
    val hit: Int,
    val critical: Int,
    val weight: Int,
    val range: IntRange,
    val maxUses: Int,
    var currentUses: Int = maxUses,
    val description: String = "",
    val canHeal: Boolean = false,
    val effectiveAgainst: List<CharacterClass> = emptyList(),
) {
    
    val isBroken: Boolean
        get() = currentUses <= 0
    
    val isLowDurability: Boolean
        get() = currentUses <= maxUses / 4
    
    fun use(): Boolean {
        if (currentUses > 0) {
            currentUses--
        }
        return isBroken
    }
    
    fun repair() {
        currentUses = maxUses
    }
    
    fun getTriangleBonus(otherType: WeaponType): Double {
        return when {
            type == WeaponType.SWORD && otherType == WeaponType.AXE -> 1.2
            type == WeaponType.AXE && otherType == WeaponType.LANCE -> 1.2
            type == WeaponType.LANCE && otherType == WeaponType.SWORD -> 1.2
            type == WeaponType.AXE && otherType == WeaponType.SWORD -> 0.8
            type == WeaponType.LANCE && otherType == WeaponType.AXE -> 0.8
            type == WeaponType.SWORD && otherType == WeaponType.LANCE -> 0.8
            else -> 1.0
        }
    }
    
    fun isEffectiveAgainst(characterClass: CharacterClass): Boolean {
        return characterClass in effectiveAgainst
    }
    
    companion object {
        fun ironSword() = Weapon("iron_sword", "Iron Sword", WeaponType.SWORD, WeaponRank.E, 5, 90, 0, 5, 1..1, 46)
        fun steelSword() = Weapon("steel_sword", "Steel Sword", WeaponType.SWORD, WeaponRank.D, 8, 85, 0, 8, 1..1, 30)
        fun ironLance() = Weapon("iron_lance", "Iron Lance", WeaponType.LANCE, WeaponRank.E, 7, 80, 0, 8, 1..1, 45)
        fun ironAxe() = Weapon("iron_axe", "Iron Axe", WeaponType.AXE, WeaponRank.E, 8, 75, 0, 10, 1..1, 45)
        fun ironBow() = Weapon("iron_bow", "Iron Bow", WeaponType.BOW, WeaponRank.E, 6, 85, 0, 6, 2..2, 45, 
            effectiveAgainst = listOf(CharacterClass.PEGASUS_KNIGHT, CharacterClass.WYVERN_RIDER))
        fun steelBow() = Weapon("steel_bow", "Steel Bow", WeaponType.BOW, WeaponRank.D, 9, 80, 0, 9, 2..2, 30,
            effectiveAgainst = listOf(CharacterClass.PEGASUS_KNIGHT, CharacterClass.WYVERN_RIDER))
        fun fire() = Weapon("fire", "Fire", WeaponType.TOME, WeaponRank.E, 5, 90, 0, 4, 1..2, 40)
        fun thunder() = Weapon("thunder", "Thunder", WeaponType.TOME, WeaponRank.D, 9, 75, 5, 6, 1..2, 35)
    }
}
