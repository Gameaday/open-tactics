package com.gameaday.opentactics.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PositionTest {
    @Test
    fun testPositionCreation() {
        val position = Position(5, 3)
        assertEquals(5, position.x)
        assertEquals(3, position.y)
    }

    @Test
    fun testDistanceCalculation() {
        val pos1 = Position(0, 0)
        val pos2 = Position(3, 4)
        val pos3 = Position(-2, 1)

        // Manhattan distance
        assertEquals(7, pos1.distanceTo(pos2)) // |0-3| + |0-4| = 3 + 4 = 7
        assertEquals(3, pos1.distanceTo(pos3)) // |0-(-2)| + |0-1| = 2 + 1 = 3
        assertEquals(8, pos2.distanceTo(pos3)) // |3-(-2)| + |4-1| = 5 + 3 = 8
    }

    @Test
    fun testDistanceToSelf() {
        val position = Position(5, 5)
        assertEquals(0, position.distanceTo(position))
    }

    @Test
    fun testAdjacency() {
        val center = Position(5, 5)
        val north = Position(5, 4)
        val south = Position(5, 6)
        val east = Position(6, 5)
        val west = Position(4, 5)
        val diagonal = Position(6, 6)
        val farAway = Position(8, 8)

        // Adjacent positions (distance = 1)
        assertTrue(center.isAdjacentTo(north))
        assertTrue(center.isAdjacentTo(south))
        assertTrue(center.isAdjacentTo(east))
        assertTrue(center.isAdjacentTo(west))

        // Non-adjacent positions
        assertFalse(center.isAdjacentTo(diagonal)) // Distance = 2
        assertFalse(center.isAdjacentTo(farAway)) // Distance > 2
        assertFalse(center.isAdjacentTo(center)) // Distance = 0 (self)
    }

    @Test
    fun testGetNeighbors() {
        val position = Position(5, 5)
        val neighbors = position.getNeighbors()

        assertEquals(4, neighbors.size)
        assertTrue(neighbors.contains(Position(4, 5))) // West
        assertTrue(neighbors.contains(Position(6, 5))) // East
        assertTrue(neighbors.contains(Position(5, 4))) // North
        assertTrue(neighbors.contains(Position(5, 6))) // South
    }

    @Test
    fun testGetNeighborsAtOrigin() {
        val origin = Position(0, 0)
        val neighbors = origin.getNeighbors()

        assertEquals(4, neighbors.size)
        assertTrue(neighbors.contains(Position(-1, 0))) // West
        assertTrue(neighbors.contains(Position(1, 0))) // East
        assertTrue(neighbors.contains(Position(0, -1))) // North
        assertTrue(neighbors.contains(Position(0, 1))) // South
    }

    @Test
    fun testGetNeighborsWithNegativeCoordinates() {
        val position = Position(-2, -3)
        val neighbors = position.getNeighbors()

        assertEquals(4, neighbors.size)
        assertTrue(neighbors.contains(Position(-3, -3))) // West
        assertTrue(neighbors.contains(Position(-1, -3))) // East
        assertTrue(neighbors.contains(Position(-2, -4))) // North
        assertTrue(neighbors.contains(Position(-2, -2))) // South
    }

    @Test
    fun testEquality() {
        val pos1 = Position(3, 4)
        val pos2 = Position(3, 4)
        val pos3 = Position(4, 3)

        assertEquals(pos1, pos2)
        assertNotEquals(pos1, pos3)
    }

    @Test
    fun testHashCode() {
        val pos1 = Position(3, 4)
        val pos2 = Position(3, 4)
        val pos3 = Position(4, 3)

        assertEquals(pos1.hashCode(), pos2.hashCode())
        assertNotEquals(pos1.hashCode(), pos3.hashCode())
    }

    @Test
    fun testToString() {
        val position = Position(3, 4)
        val string = position.toString()

        assertTrue(string.contains("3"))
        assertTrue(string.contains("4"))
    }
}
