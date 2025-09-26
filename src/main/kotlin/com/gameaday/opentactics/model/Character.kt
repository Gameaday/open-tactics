package com.gameaday.opentactics.model

enum class Team {
    PLAYER,
    ENEMY,
    NEUTRAL
}

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
    var hasMovedThisTurn: Boolean = false
) {
    val currentStats: Stats
        get() {
            val levelBonus = Stats(
                hp = level - 1,
                mp = (level - 1) / 2,
                attack = (level - 1) / 2,
                defense = (level - 1) / 2,
                speed = (level - 1) / 3,
                skill = (level - 1) / 2,
                luck = (level - 1) / 3
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
        while (experience >= experienceToNextLevel() && level < 20) {
            levelUp()
        }
    }
    
    private fun experienceToNextLevel(): Int {
        return level * 100
    }
    
    private fun levelUp() {
        experience -= experienceToNextLevel()
        level++
        // Heal to full on level up
        currentHp = maxHp
        currentMp = maxMp
    }
}