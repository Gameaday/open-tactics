package com.gameaday.opentactics.model

// Character progression constants
private const val MAX_LEVEL = 20
private const val EXPERIENCE_PER_LEVEL = 100

enum class Team {
    PLAYER,
    ENEMY,
    NEUTRAL,
}

data class Character(
    val id: String,
    val name: String,
    var characterClass: CharacterClass,
    val team: Team,
    var position: Position,
    var level: Int = 1,
    var experience: Int = 0,
    var currentHp: Int = characterClass.baseStats.hp,
    var currentMp: Int = characterClass.baseStats.mp,
    var hasActedThisTurn: Boolean = false,
    var hasMovedThisTurn: Boolean = false,
    var isTransformed: Boolean = false,
    var originalClass: CharacterClass? = null,
    var inventory: MutableList<Weapon> = mutableListOf(),
    var equippedWeaponIndex: Int = -1,
) {
    val currentStats: Stats
        get() {
            val levelBonus =
                Stats(
                    hp = level - 1,
                    mp = (level - 1) / 2,
                    attack = (level - 1) / 2,
                    defense = (level - 1) / 2,
                    speed = (level - 1) / 3,
                    skill = (level - 1) / 2,
                    luck = (level - 1) / 3,
                )
            return characterClass.baseStats + levelBonus
        }

    val maxHp: Int
        get() = currentStats.hp

    val maxMp: Int
        get() = currentStats.mp

    val isAlive: Boolean
        get() = currentHp > 0

    val canAct: Boolean
        get() = !hasActedThisTurn && isAlive

    val canMove: Boolean
        get() = !hasMovedThisTurn && isAlive

    fun resetTurn() {
        hasActedThisTurn = false
        hasMovedThisTurn = false
    }

    fun takeDamage(damage: Int) {
        currentHp = maxOf(0, currentHp - damage)
    }

    fun heal(amount: Int) {
        currentHp = minOf(maxHp, currentHp + amount)
    }

    fun gainExperience(exp: Int) {
        experience += exp
        while (experience >= experienceToNextLevel() && level < MAX_LEVEL) {
            levelUp()
        }
    }

    private fun experienceToNextLevel(): Int = level * EXPERIENCE_PER_LEVEL

    private fun levelUp() {
        experience -= experienceToNextLevel()
        level++
        // Heal to full on level up
        currentHp = maxHp
        currentMp = maxMp
    }

    /**
     * Transform the character to their transformed class (e.g., Manakete -> Dragon)
     * @return true if transformation was successful, false otherwise
     */
    fun transform(): Boolean {
        if (!characterClass.canTransform) return false
        val transformTo = characterClass.transformsTo ?: return false
        
        // Store original class
        originalClass = characterClass
        
        // Transform
        characterClass = transformTo
        isTransformed = true
        
        // Restore HP/MP to maximum of new form
        currentHp = maxHp
        currentMp = maxMp
        
        return true
    }

    /**
     * Revert transformation back to original class
     * @return true if reversion was successful, false otherwise
     */
    fun revertTransform(): Boolean {
        if (!isTransformed) return false
        val original = originalClass ?: return false
        
        // Calculate HP/MP ratio before transformation
        val hpRatio = currentHp.toFloat() / maxHp
        val mpRatio = currentMp.toFloat() / maxMp
        
        // Revert to original class
        characterClass = original
        isTransformed = false
        originalClass = null
        
        // Restore HP/MP proportionally
        currentHp = (maxHp * hpRatio).toInt().coerceIn(1, maxHp)
        currentMp = (maxMp * mpRatio).toInt().coerceIn(0, maxMp)
        
        return true
    }

    /**
     * Check if character can transform (has transformation ability and not already transformed)
     */
    fun canTransformNow(): Boolean = characterClass.canTransform && !isTransformed

    /**
     * Check if character can revert transformation
     */
    fun canRevertTransform(): Boolean = isTransformed && originalClass != null
    
    // Weapon and Inventory Management
    
    val equippedWeapon: Weapon?
        get() = if (equippedWeaponIndex >= 0 && equippedWeaponIndex < inventory.size) {
            inventory[equippedWeaponIndex]
        } else null
    
    fun addWeapon(weapon: Weapon): Boolean {
        if (inventory.size >= MAX_INVENTORY_SIZE) return false
        inventory.add(weapon)
        if (equippedWeaponIndex < 0) {
            equippedWeaponIndex = inventory.size - 1
        }
        return true
    }
    
    fun removeWeapon(index: Int): Weapon? {
        if (index < 0 || index >= inventory.size) return null
        
        val weapon = inventory.removeAt(index)
        
        when {
            equippedWeaponIndex == index -> {
                equippedWeaponIndex = if (inventory.isNotEmpty()) 0 else -1
            }
            equippedWeaponIndex > index -> {
                equippedWeaponIndex--
            }
        }
        
        return weapon
    }
    
    fun equipWeapon(index: Int): Boolean {
        if (index < 0 || index >= inventory.size) return false
        if (inventory[index].isBroken) return false
        
        equippedWeaponIndex = index
        return true
    }
    
    fun getAttackRange(): IntRange {
        return equippedWeapon?.range ?: characterClass.attackRange..characterClass.attackRange
    }
    
    fun canAttackPosition(targetPos: Position): Boolean {
        val distance = position.distanceTo(targetPos)
        val range = getAttackRange()
        return distance in range
    }
    
    fun useEquippedWeapon(): Boolean {
        val weapon = equippedWeapon ?: return false
        val broke = weapon.use()
        
        if (broke) {
            removeWeapon(equippedWeaponIndex)
        }
        
        return broke
    }
    
    companion object {
        const val MAX_INVENTORY_SIZE = 5
    }
}
