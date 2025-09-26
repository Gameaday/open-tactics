package com.gameaday.opentactics.model

data class Stats(
    val hp: Int,
    val mp: Int,
    val attack: Int,
    val defense: Int,
    val speed: Int,
    val skill: Int,
    val luck: Int
) {
    operator fun plus(other: Stats): Stats {
        return Stats(
            hp + other.hp,
            mp + other.mp,
            attack + other.attack,
            defense + other.defense,
            speed + other.speed,
            skill + other.skill,
            luck + other.luck
        )
    }
    
    fun toDisplayString(): String {
        return "HP: $hp  MP: $mp  ATK: $attack\nDEF: $defense  SPD: $speed  SKL: $skill\nLCK: $luck"
    }
}