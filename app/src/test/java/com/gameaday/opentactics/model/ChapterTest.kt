package com.gameaday.opentactics.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ChapterTest {
    @Test
    fun `test chapter repository has 20 chapters`() {
        val totalChapters = ChapterRepository.getTotalChapters()
        assertEquals("Should have 20 chapters", 20, totalChapters)
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
    fun `test chapter 6 is river crossing`() {
        val chapter6 = ChapterRepository.getChapter(6)
        assertNotNull("Chapter 6 should exist", chapter6)
        assertEquals("ch6_river_crossing", chapter6?.id)
        assertEquals("The River Crossing", chapter6?.title)
        assertEquals(ChapterObjective.DEFEAT_ALL_ENEMIES, chapter6?.objective)
        // Chapter 6 should have an enemy healer for AI healing
        val hasHealer = chapter6?.enemyUnits?.any { it.characterClass == CharacterClass.HEALER }
        assertTrue("Chapter 6 should have an enemy healer", hasHealer == true)
    }

    @Test
    fun `test chapter 7 is village liberation with boss`() {
        val chapter7 = ChapterRepository.getChapter(7)
        assertNotNull("Chapter 7 should exist", chapter7)
        assertEquals("ch7_village_liberation", chapter7?.id)
        assertEquals(ChapterObjective.DEFEAT_BOSS, chapter7?.objective)
        assertNotNull("Chapter 7 should have a boss unit", chapter7?.bossUnit)
    }

    @Test
    fun `test chapter 9 is border fort siege`() {
        val chapter9 = ChapterRepository.getChapter(9)
        assertNotNull("Chapter 9 should exist", chapter9)
        assertEquals("ch9_border_fort", chapter9?.id)
        assertEquals(ChapterObjective.SEIZE_THRONE, chapter9?.objective)
        assertNotNull("Chapter 9 should have throne position", chapter9?.thronePosition)
        assertFalse("Chapter 9 should have reinforcements", chapter9?.reinforcements.isNullOrEmpty())
    }

    @Test
    fun `test chapter 10 is boss fight with sorceress`() {
        val chapter10 = ChapterRepository.getChapter(10)
        assertNotNull("Chapter 10 should exist", chapter10)
        assertEquals("ch10_sorceress", chapter10?.id)
        assertEquals(ChapterObjective.DEFEAT_BOSS, chapter10?.objective)
        assertNotNull("Chapter 10 should have a boss", chapter10?.bossUnit)
        assertTrue("Boss should be marked as boss", chapter10?.bossUnit?.isBoss == true)
    }

    @Test
    fun `test chapter 12 is frozen pass survival`() {
        val chapter12 = ChapterRepository.getChapter(12)
        assertNotNull("Chapter 12 should exist", chapter12)
        assertEquals("ch12_frozen_pass", chapter12?.id)
        assertEquals(ChapterObjective.SURVIVE, chapter12?.objective)
        assertEquals(12, chapter12?.turnLimit)
        assertFalse("Chapter 12 should have reinforcements", chapter12?.reinforcements.isNullOrEmpty())
    }

    @Test
    fun `test chapter 14 is warlord boss fight`() {
        val chapter14 = ChapterRepository.getChapter(14)
        assertNotNull("Chapter 14 should exist", chapter14)
        assertEquals("ch14_warlord", chapter14?.id)
        assertEquals(ChapterObjective.DEFEAT_BOSS, chapter14?.objective)
        assertNotNull("Chapter 14 should have boss", chapter14?.bossUnit)
        assertEquals("boss_warlord_kael", chapter14?.bossUnit?.namedUnitId)
    }

    @Test
    fun `test chapter 18 is throne room seize`() {
        val chapter18 = ChapterRepository.getChapter(18)
        assertNotNull("Chapter 18 should exist", chapter18)
        assertEquals("ch18_throne_room", chapter18?.id)
        assertEquals(ChapterObjective.SEIZE_THRONE, chapter18?.objective)
        assertNotNull("Chapter 18 should have throne position", chapter18?.thronePosition)
        assertNotNull("Chapter 18 should have boss (Emperor Darius)", chapter18?.bossUnit)
    }

    @Test
    fun `test chapter 20 is final battle`() {
        val chapter20 = ChapterRepository.getChapter(20)
        assertNotNull("Chapter 20 should exist", chapter20)
        assertEquals("ch20_final_battle", chapter20?.id)
        assertEquals("Dawn of Victory", chapter20?.title)
        assertEquals(ChapterObjective.DEFEAT_BOSS, chapter20?.objective)
        assertNotNull("Final chapter should have boss", chapter20?.bossUnit)
        assertTrue("Final boss should be marked as boss", chapter20?.bossUnit?.isBoss == true)
        assertEquals("boss_emperor_darius", chapter20?.bossUnit?.namedUnitId)
        assertTrue(
            "Final chapter should have victory epilogue",
            chapter20?.postVictoryDialogue?.contains("THE END") == true,
        )
    }

    @Test
    fun `test act 3 chapters have enemy healers`() {
        // Most Act 3 chapters (11, 13-15) should have enemy healers
        val chaptersWithHealers = listOf(11, 13, 14, 15)
        for (chapterNum in chaptersWithHealers) {
            val chapter = ChapterRepository.getChapter(chapterNum)
            assertNotNull("Chapter $chapterNum should exist", chapter)
            val hasHealer = chapter?.enemyUnits?.any { it.characterClass == CharacterClass.HEALER }
            assertTrue("Chapter $chapterNum should have an enemy healer", hasHealer == true)
        }
    }

    @Test
    fun `test invalid chapter number returns null`() {
        assertNull("Chapter 0 should not exist", ChapterRepository.getChapter(0))
        assertNull("Chapter 21 should not exist", ChapterRepository.getChapter(21))
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
        val chapter20 = ChapterRepository.getChapter(20)

        assertNotNull(chapter1)
        assertNotNull(chapter20)

        // Chapter 20 should have more or equal enemy units than chapter 1
        val ch1EnemyCount = chapter1!!.enemyUnits.size + (chapter1.bossUnit?.let { 1 } ?: 0)
        val ch20EnemyCount = chapter20!!.enemyUnits.size + (chapter20.bossUnit?.let { 1 } ?: 0)

        assertTrue("Later chapters should have at least as many enemies", ch20EnemyCount >= ch1EnemyCount)

        // Chapter 20 enemies should have higher levels
        val ch1MaxLevel = chapter1.enemyUnits.maxOfOrNull { it.level } ?: 1
        val ch20MaxLevel = chapter20.enemyUnits.maxOfOrNull { it.level } ?: 1

        assertTrue("Later chapters should have higher level enemies", ch20MaxLevel >= ch1MaxLevel)
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
