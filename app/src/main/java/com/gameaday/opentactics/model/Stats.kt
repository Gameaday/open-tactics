package com.gameaday.opentactics.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class Stats(
    val hp: Int,
    val mp: Int,
    val attack: Int,
    val defense: Int,
    val speed: Int,
    val skill: Int,
    val luck: Int,
) : Parcelable {
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

    operator fun minus(other: Stats): Stats =
        Stats(
            hp - other.hp,
            mp - other.mp,
            attack - other.attack,
            defense - other.defense,
            speed - other.speed,
            skill - other.skill,
            luck - other.luck,
        )

    /**
     * Scale all stats by a multiplier (used for difficulty scaling).
     * Ensures minimum of 1 for each stat.
     */
    fun scale(multiplier: Float): Stats =
        Stats(
            hp = maxOf(1, (hp * multiplier).toInt()),
            mp = maxOf(1, (mp * multiplier).toInt()),
            attack = maxOf(1, (attack * multiplier).toInt()),
            defense = maxOf(1, (defense * multiplier).toInt()),
            speed = maxOf(1, (speed * multiplier).toInt()),
            skill = maxOf(1, (skill * multiplier).toInt()),
            luck = maxOf(1, (luck * multiplier).toInt()),
        )

    fun toDisplayString(): String =
        "HP: $hp  MP: $mp  ATK: $attack\n" +
            "DEF: $defense  SPD: $speed  SKL: $skill\n" +
            "LCK: $luck"
}

/**
 * Growth rates for stat increases on level up
 * Values are percentages (0-100) representing the chance of that stat increasing
 */
@Parcelize
@Serializable
data class GrowthRates(
    val hp: Int,
    val mp: Int,
    val attack: Int,
    val defense: Int,
    val speed: Int,
    val skill: Int,
    val luck: Int,
) : Parcelable
