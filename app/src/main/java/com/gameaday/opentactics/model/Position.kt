package com.gameaday.opentactics.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class Position(
    val x: Int,
    val y: Int,
) : Parcelable {
    fun distanceTo(other: Position): Int = kotlin.math.abs(x - other.x) + kotlin.math.abs(y - other.y)

    fun isAdjacentTo(other: Position): Boolean = distanceTo(other) == 1

    fun getNeighbors(): List<Position> =
        listOf(
            Position(x - 1, y),
            Position(x + 1, y),
            Position(x, y - 1),
            Position(x, y + 1),
        )
}
