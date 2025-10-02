package com.gameaday.opentactics.model

data class Stats(
    val hp: Int,
    val mp: Int,
    val attack: Int,
    val defense: Int,
    val speed: Int,
    val skill: Int,
    val luck: Int,
) {
    operator fun plus(other: Stats): Stats =
        Stats(
            hp + other.hp,
            mp + other.mp,
            attack + other.attack,
            defense + other.defense,
            speed + other.speed,
            skill + other.skill,
            luck + other.luck,
        )

    fun toDisplayString(): String =
        "HP: $hp  MP: $mp  ATK: $attack\n" +
            "DEF: $defense  SPD: $speed  SKL: $skill\n" +
            "LCK: $luck"
}
