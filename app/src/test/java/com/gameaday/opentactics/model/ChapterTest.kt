package com.gameaday.opentactics.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ChapterTest {
    @Test
    fun `test chapter repository has 5 chapters`() {
        val totalChapters = ChapterRepository.getTotalChapters()
        assertEquals("Should have 5 chapters", 5, totalChapters)
    }

    @Test
    fun `test chapter 1 is tutorial`() {
        val chapter1 = ChapterRepository.getChapter(1)
        assertNotNull("Chapter 1 should exist", chapter1)
        assertEquals("ch1_tutorial", chapter1?.id)
        assertEquals("The First Battle", chapter1?.title)
        assertEquals(ChapterObjective.DEFEAT_ALL_ENEMIES, chapter1?.objective)
    }

    @Test
    fun `test chapter 2 is forest skirmish with boss`() {
        val chapter2 = ChapterRepository.getChapter(2)
        assertNotNull("Chapter 2 should exist", chapter2)
        assertEquals("ch2_challenge", chapter2?.id)
        assertEquals("Forest Skirmish", chapter2?.title)
        assertEquals(ChapterObjective.DEFEAT_BOSS, chapter2?.objective)
        assertNotNull("Chapter 2 should have a boss unit", chapter2?.bossUnit)
        assertTrue("Boss unit should be marked as boss", chapter2?.bossUnit?.isBoss == true)
    }

    @Test
    fun `test chapter 3 is mountain defense with survival objective`() {
        val chapter3 = ChapterRepository.getChapter(3)
        assertNotNull("Chapter 3 should exist", chapter3)
        assertEquals("ch3_mountain_defense", chapter3?.id)
        assertEquals("Mountain Outpost", chapter3?.title)
        assertEquals(ChapterObjective.SURVIVE, chapter3?.objective)
        assertEquals("Chapter 3 should have turn limit of 10", 10, chapter3?.turnLimit)
        assertFalse("Chapter 3 should have reinforcements", chapter3?.reinforcements.isNullOrEmpty())
    }

    @Test
    fun `test chapter 4 is castle siege with throne objective`() {
        val chapter4 = ChapterRepository.getChapter(4)
        assertNotNull("Chapter 4 should exist", chapter4)
        assertEquals("ch4_castle_assault", chapter4?.id)
        assertEquals("Siege of Castle Ironhold", chapter4?.title)
        assertEquals(ChapterObjective.SEIZE_THRONE, chapter4?.objective)
        assertNotNull("Chapter 4 should have throne position", chapter4?.thronePosition)
        assertNotNull("Chapter 4 should have a boss unit", chapter4?.bossUnit)
    }

    @Test
    fun `test chapter 5 is village escape with escape objective`() {
        val chapter5 = ChapterRepository.getChapter(5)
        assertNotNull("Chapter 5 should exist", chapter5)
        assertEquals("ch5_village_escape", chapter5?.id)
        assertEquals("Flight from the Village", chapter5?.title)
        assertEquals(ChapterObjective.ESCAPE, chapter5?.objective)
        assertEquals("Chapter 5 should require 3 escapes", 3, chapter5?.requiredEscapes)
        assertFalse("Chapter 5 should have escape positions", chapter5?.escapePositions.isNullOrEmpty())
        assertFalse("Chapter 5 should have reinforcements", chapter5?.reinforcements.isNullOrEmpty())
    }

    @Test
    fun `test invalid chapter number returns null`() {
        assertNull("Chapter 0 should not exist", ChapterRepository.getChapter(0))
        assertNull("Chapter 6 should not exist", ChapterRepository.getChapter(6))
        assertNull("Chapter 100 should not exist", ChapterRepository.getChapter(100))
    }

    @Test
    fun `test all chapters have required fields`() {
        for (i in 1..ChapterRepository.getTotalChapters()) {
            val chapter = ChapterRepository.getChapter(i)
            assertNotNull("Chapter $i should exist", chapter)
            assertFalse("Chapter $i should have an id", chapter?.id.isNullOrEmpty())
            assertFalse("Chapter $i should have a title", chapter?.title.isNullOrEmpty())
            assertFalse("Chapter $i should have player start positions", chapter?.playerStartPositions.isNullOrEmpty())
            assertFalse("Chapter $i should have enemy units", chapter?.enemyUnits.isNullOrEmpty())
        }
    }

    @Test
    fun `test chapters have escalating difficulty`() {
        val chapter1 = ChapterRepository.getChapter(1)
        val chapter5 = ChapterRepository.getChapter(5)

        assertNotNull(chapter1)
        assertNotNull(chapter5)

        // Chapter 5 should have more or equal enemy units than chapter 1
        val ch1EnemyCount = chapter1!!.enemyUnits.size + (chapter1.bossUnit?.let { 1 } ?: 0)
        val ch5EnemyCount = chapter5!!.enemyUnits.size + (chapter5.bossUnit?.let { 1 } ?: 0)

        assertTrue("Later chapters should have at least as many enemies", ch5EnemyCount >= ch1EnemyCount)

        // Chapter 5 enemies should have higher levels
        val ch1MaxLevel = chapter1.enemyUnits.maxOfOrNull { it.level } ?: 1
        val ch5MaxLevel = chapter5.enemyUnits.maxOfOrNull { it.level } ?: 1

        assertTrue("Later chapters should have higher level enemies", ch5MaxLevel >= ch1MaxLevel)
    }

    @Test
    fun `test chapter reinforcements spawn at correct turns`() {
        val chapter3 = ChapterRepository.getChapter(3)
        assertNotNull(chapter3)

        val reinforcements = chapter3!!.reinforcements
        assertEquals("Chapter 3 should have 2 reinforcements", 2, reinforcements.size)

        val turn5Reinforcements = chapter3.getReinforcementsForTurn(5)
        assertEquals("Should have 1 reinforcement at turn 5", 1, turn5Reinforcements.size)

        val turn7Reinforcements = chapter3.getReinforcementsForTurn(7)
        assertEquals("Should have 1 reinforcement at turn 7", 1, turn7Reinforcements.size)

        val turn1Reinforcements = chapter3.getReinforcementsForTurn(1)
        assertEquals("Should have no reinforcements at turn 1", 0, turn1Reinforcements.size)
    }
}
