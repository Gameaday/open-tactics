package com.gameaday.opentactics.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests for EnemyUnitSpawn.toCharacter() which is used by
 * GameActivity when loading saves into new chapters.
 * Validates that enemy creation preserves all properties.
 */
class EnemySpawnTest {
    @Test
    fun testToCharacterPreservesProperties() {
        val spawn =
            EnemyUnitSpawn(
                id = "enemy_test",
                name = "Test Soldier",
                characterClass = CharacterClass.KNIGHT,
                level = 3,
                position = Position(5, 5),
                equipment = listOf("iron_sword", "iron_lance"),
                isBoss = false,
                aiType = AIBehavior.AGGRESSIVE,
            )

        val character = spawn.toCharacter(Team.ENEMY)

        assertEquals("enemy_test", character.id)
        assertEquals("Test Soldier", character.name)
        assertEquals(CharacterClass.KNIGHT, character.characterClass)
        assertEquals(Team.ENEMY, character.team)
        assertEquals(Position(5, 5), character.position)
        assertTrue(character.isAlive)
    }

    @Test
    fun testBossSpawnSetsBossFlag() {
        val spawn =
            EnemyUnitSpawn(
                id = "boss_1",
                name = "Evil Boss",
                characterClass = CharacterClass.MAGE,
                level = 5,
                position = Position(7, 0),
                equipment = listOf("fire_tome"),
                isBoss = true,
                aiType = AIBehavior.DEFENSIVE,
            )

        val character = spawn.toCharacter(Team.ENEMY)

        assertTrue(character.isBoss)
        assertEquals(AIBehavior.DEFENSIVE, character.aiType)
    }

    @Test
    fun testToCharacterLevelsUpToTarget() {
        val spawn =
            EnemyUnitSpawn(
                id = "strong_enemy",
                name = "Veteran",
                characterClass = CharacterClass.ARCHER,
                level = 5,
                position = Position(3, 3),
                equipment = listOf("iron_bow"),
            )

        val character = spawn.toCharacter()

        // Character should be leveled up to target
        assertEquals(5, character.level)
    }

    @Test
    fun testChapterHasEnemyUnits() {
        val chapter = ChapterRepository.getChapter(1)
        assertNotNull(chapter)
        assertTrue(
            "Chapter 1 must have enemy units",
            chapter!!.enemyUnits.isNotEmpty(),
        )
    }

    @Test
    fun testChapterHasPlayerStartPositions() {
        val chapter = ChapterRepository.getChapter(1)
        assertNotNull(chapter)
        assertTrue(
            "Chapter 1 must have player start positions",
            chapter!!.playerStartPositions.isNotEmpty(),
        )
    }

    @Test
    fun testAllChaptersHaveEnemies() {
        // Validate all 20 chapters have enemies (critical for save/load)
        for (num in 1..20) {
            val chapter = ChapterRepository.getChapter(num)
            assertNotNull("Chapter $num must exist", chapter)
            assertTrue(
                "Chapter $num must have enemy units",
                chapter!!.enemyUnits.isNotEmpty(),
            )
        }
    }
}
