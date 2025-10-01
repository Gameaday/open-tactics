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
data class Character(
    val id: String,
    val name: String,
    val characterClass: CharacterClass,
    val team: Team,
    var position: Position,
    var level: Int = 1,
    var experience: Int = 0,
    var currentHp: Int = characterClass.baseStats.hp,
    var currentMp: Int = characterClass.baseStats.mp,
    var hasActedThisTurn: Boolean = false,
    var hasMovedThisTurn: Boolean = false,
) : Parcelable {
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

    private fun experienceToNextLevel(): Int = level * EXPERIENCE_PER_LEVEL

    private fun levelUp() {
        experience -= experienceToNextLevel()
        level++
        // Heal to full on level up
        currentHp = maxHp
        currentMp = maxMp
    }
}
