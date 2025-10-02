package com.gameaday.opentactics.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

// Character progression constants
private const val MAX_LEVEL = 20
private const val EXPERIENCE_PER_LEVEL = 100

@Serializable
enum class Team {
    PLAYER,
    ENEMY,
    NEUTRAL,
}

@Parcelize
@Serializable
@Suppress("TooManyFunctions") // Character class requires many functions for game mechanics
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
    var equippedWeaponIndex: Int = -1, // -1 means no weapon equipped
    var items: MutableList<Item> = mutableListOf(), // Consumable items
    // Action history for undo functionality
    var previousPosition: Position? = null,
    var canStillMoveAfterAttack: Boolean = false, // Tracks Canto state
    // Enemy AI properties
    var isBoss: Boolean = false,
    var aiType: AIBehavior = AIBehavior.AGGRESSIVE,
    // Stat bonuses from level ups (accumulated random growths)
    var statBonuses: Stats = Stats(0, 0, 0, 0, 0, 0, 0),
) : Parcelable {
    val currentStats: Stats
        get() = characterClass.baseStats + statBonuses

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

    // Aliases for test compatibility
    var hasActed: Boolean
        get() = hasActedThisTurn
        set(value) {
            hasActedThisTurn = value
        }

    var hasMoved: Boolean
        get() = hasMovedThisTurn
        set(value) {
            hasMovedThisTurn = value
        }

    fun resetActions() {
        resetTurn()
    }

    fun resetTurn() {
        hasActedThisTurn = false
        hasMovedThisTurn = false
        previousPosition = null
        canStillMoveAfterAttack = false
    }

    /**
     * Mark that the character has moved
     * Stores previous position for undo functionality
     */
    fun commitMove(from: Position) {
        previousPosition = from
        hasMovedThisTurn = true
    }

    /**
     * Mark that the character has performed an action (attack, item use, etc.)
     */
    fun commitAction() {
        hasActedThisTurn = true

        // If character has Canto, they can move after attack
        if (characterClass.hasCanto) {
            canStillMoveAfterAttack = true
        } else {
            // Non-Canto units cannot move after acting
            canStillMoveAfterAttack = false
            hasMovedThisTurn = true // Mark as moved to prevent further movement
        }
    }

    /**
     * Undo the last move (returns to previous position)
     */
    fun undoMove(): Position? {
        val prev = previousPosition
        if (prev != null && hasMovedThisTurn && !hasActedThisTurn) {
            hasMovedThisTurn = false
            previousPosition = null
            canStillMoveAfterAttack = false
            return prev
        }
        return null
    }

    /**
     * Check if character can still move (including Canto movement after attack)
     */
    fun canMoveNow(): Boolean =
        when {
            !isAlive -> false
            !hasMovedThisTurn -> true // Haven't moved yet
            canStillMoveAfterAttack -> true // Has Canto and can move again
            else -> false
        }

    /**
     * Wait - ends all possible actions for this turn
     */
    fun commitWait() {
        hasActedThisTurn = true
        hasMovedThisTurn = true
        canStillMoveAfterAttack = false
        previousPosition = null
    }

    fun takeDamage(damage: Int) {
        currentHp = maxOf(0, currentHp - damage)
    }

    fun heal(amount: Int) {
        currentHp = minOf(maxHp, currentHp + amount)
    }

    fun useMana(amount: Int) {
        currentMp = maxOf(0, currentMp - amount)
    }

    fun restoreMana(amount: Int) {
        currentMp = minOf(maxMp, currentMp + amount)
    }

    fun gainExperience(exp: Int) {
        experience += exp
        while (experience >= experienceToNextLevel() && level < MAX_LEVEL) {
            levelUp()
        }
    }

    private fun experienceToNextLevel(): Int = EXPERIENCE_PER_LEVEL

    /**
     * Level up the character with random stat increases based on growth rates
     * @return Stats object showing which stats increased (0 or 1 for each stat)
     */
    private fun levelUp(): Stats {
        experience -= experienceToNextLevel()
        level++

        // Store old max HP/MP
        val oldMaxHp = maxHp
        val oldMaxMp = maxMp

        // Roll for stat increases based on growth rates
        val growthRates = characterClass.growthRates
        var statGains =
            Stats(
                hp = if ((1..100).random() <= growthRates.hp) 1 else 0,
                mp = if ((1..100).random() <= growthRates.mp) 1 else 0,
                attack = if ((1..100).random() <= growthRates.attack) 1 else 0,
                defense = if ((1..100).random() <= growthRates.defense) 1 else 0,
                speed = if ((1..100).random() <= growthRates.speed) 1 else 0,
                skill = if ((1..100).random() <= growthRates.skill) 1 else 0,
                luck = if ((1..100).random() <= growthRates.luck) 1 else 0,
            )

        // Guarantee at least one stat increase
        val totalGains =
            statGains.hp + statGains.mp + statGains.attack +
                statGains.defense + statGains.speed + statGains.skill + statGains.luck
        if (totalGains == 0) {
            // Pick a random stat to increase based on highest growth rates
            val stats =
                listOf(
                    "hp" to growthRates.hp,
                    "mp" to growthRates.mp,
                    "attack" to growthRates.attack,
                    "defense" to growthRates.defense,
                    "speed" to growthRates.speed,
                    "skill" to growthRates.skill,
                    "luck" to growthRates.luck,
                ).sortedByDescending { it.second }

            // Increase the stat with highest growth rate
            statGains =
                when (stats[0].first) {
                    "hp" -> statGains.copy(hp = 1)
                    "mp" -> statGains.copy(mp = 1)
                    "attack" -> statGains.copy(attack = 1)
                    "defense" -> statGains.copy(defense = 1)
                    "speed" -> statGains.copy(speed = 1)
                    "skill" -> statGains.copy(skill = 1)
                    "luck" -> statGains.copy(luck = 1)
                    else -> statGains.copy(hp = 1)
                }
        }

        // Apply stat bonuses
        statBonuses = statBonuses + statGains

        // Add the HP/MP increases to current values (not full heal)
        val newMaxHp = maxHp
        val newMaxMp = maxMp
        currentHp += (newMaxHp - oldMaxHp)
        currentMp += (newMaxMp - oldMaxMp)

        return statGains
    }

    /**
     * Get the stat gains from the last level up
     * This should be called right after gainExperience to get the gains
     */
    var lastLevelUpGains: Stats? = null

    fun gainExperienceWithTracking(exp: Int): Stats? {
        val oldLevel = level
        experience += exp
        var totalGains: Stats? = null
        while (experience >= experienceToNextLevel() && level < MAX_LEVEL) {
            val gains = levelUp()
            totalGains = if (totalGains == null) gains else totalGains + gains
        }
        lastLevelUpGains = if (level > oldLevel) totalGains else null
        return lastLevelUpGains
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
     * Add an item to the character's inventory
     */
    fun addItem(item: Item) {
        items.add(item)
    }

    /**
     * Remove an item from the character's inventory
     */
    fun removeItem(item: Item) {
        items.remove(item)
    }

    /**
     * Use an item on a target (can be self or ally)
     * @return true if item was used successfully
     */
    fun useItem(
        item: Item,
        target: Character,
    ): Boolean {
        if (!items.contains(item)) return false
        if (item.isUsedUp) return false

        // Apply item effects
        if (item.healAmount > 0) {
            target.heal(item.healAmount)
        }
        if (item.manaAmount > 0) {
            target.restoreMana(item.manaAmount)
        }

        // Use the item
        val usedUp = item.use()
        if (usedUp) {
            removeItem(item)
        }

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

    /**
     * Get the currently equipped weapon, or null if none equipped
     */
    val equippedWeapon: Weapon?
        get() =
            if (equippedWeaponIndex >= 0 && equippedWeaponIndex < inventory.size) {
                inventory[equippedWeaponIndex]
            } else {
                null
            }

    /**
     * Add a weapon to inventory
     * @return true if added successfully, false if inventory is full
     */
    fun addWeapon(weapon: Weapon): Boolean {
        if (inventory.size >= MAX_INVENTORY_SIZE) return false
        inventory.add(weapon)
        // Auto-equip if no weapon equipped
        if (equippedWeaponIndex < 0) {
            equippedWeaponIndex = inventory.size - 1
        }
        return true
    }

    /**
     * Remove a weapon from inventory
     * @return the removed weapon, or null if index invalid
     */
    fun removeWeapon(index: Int): Weapon? {
        if (index < 0 || index >= inventory.size) return null

        val weapon = inventory.removeAt(index)

        // Adjust equipped index
        when {
            equippedWeaponIndex == index -> {
                // Unequip if removing equipped weapon
                equippedWeaponIndex = if (inventory.isNotEmpty()) 0 else -1
            }
            equippedWeaponIndex > index -> {
                // Shift down if removing before equipped
                equippedWeaponIndex--
            }
        }

        return weapon
    }

    /**
     * Equip a weapon by inventory index
     * @return true if equipped successfully
     */
    fun equipWeapon(index: Int): Boolean {
        if (index < 0 || index >= inventory.size) return false
        if (inventory[index].isBroken) return false

        equippedWeaponIndex = index
        return true
    }

    /**
     * Equip weapon by reference
     */
    fun equipWeapon(weapon: Weapon): Boolean {
        val index = inventory.indexOf(weapon)
        if (index >= 0) {
            return equipWeapon(index)
        }
        return false
    }

    /**
     * Remove weapon by reference
     */
    fun removeWeapon(weapon: Weapon): Boolean {
        val index = inventory.indexOf(weapon)
        if (index >= 0) {
            removeWeapon(index)
            return true
        }
        return false
    }

    /**
     * Get effective attack range considering equipped weapon
     */
    fun getAttackRange(): IntRange = equippedWeapon?.range ?: characterClass.attackRange..characterClass.attackRange

    /**
     * Check if character can attack a position
     */
    fun canAttackPosition(targetPos: Position): Boolean {
        val distance = position.distanceTo(targetPos)
        val range = getAttackRange()
        return distance in range
    }

    /**
     * Use equipped weapon (reduces durability)
     * @return true if weapon broke from use
     */
    fun useEquippedWeapon(): Boolean {
        val weapon = equippedWeapon ?: return false
        val broke = weapon.use()

        if (broke) {
            // Remove broken weapon and try to equip another
            removeWeapon(equippedWeaponIndex)
        }

        return broke
    }

    companion object {
        const val MAX_INVENTORY_SIZE = 5
    }
}
