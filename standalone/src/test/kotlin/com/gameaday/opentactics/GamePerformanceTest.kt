package com.gameaday.opentactics

import com.gameaday.opentactics.model.*
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.time.measureTime

/**
 * Performance tests for core game operations.
 * These tests ensure game operations complete within reasonable time bounds.
 */
class GamePerformanceTest {
    
    @Test
    fun testGameBoardPerformance() {
        val timeElapsed = measureTime {
            val board = GameBoard(50, 50) // Large board
            
            // Test placing many characters
            repeat(100) { i ->
                val character = Character(
                    id = "perf_test_$i",
                    name = "Performance Test $i",
                    characterClass = CharacterClass.KNIGHT,
                    team = Team.PLAYER,
                    position = Position(0, 0),
                )
                board.placeCharacter(character, Position(i % 50, i / 50))
            }
        }
        
        // Should complete within 1 second
        assertTrue(timeElapsed.inWholeMilliseconds < 1000, "GameBoard operations took too long: ${timeElapsed.inWholeMilliseconds}ms")
    }
    
    @Test
    fun testPositionCalculationPerformance() {
        val timeElapsed = measureTime {
            val pos1 = Position(0, 0)
            
            // Test many distance calculations
            repeat(10000) { i ->
                val pos2 = Position(i % 100, i / 100)
                pos1.distanceTo(pos2)
            }
        }
        
        // Should complete within 500ms
        assertTrue(timeElapsed.inWholeMilliseconds < 500, "Position calculations took too long: ${timeElapsed.inWholeMilliseconds}ms")
    }
}