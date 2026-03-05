package com.gameaday.opentactics.model

import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BattleQuotesTest {
    @Test
    fun `empty battle quotes return null`() {
        val quotes = BattleQuotes()
        assertNull(quotes.randomAttack())
        assertNull(quotes.randomCritical())
        assertNull(quotes.randomDefeat())
        assertNull(quotes.randomVictory())
    }

    @Test
    fun `quotes return random entry from list`() {
        val quotes =
            BattleQuotes(
                attack = listOf("Attack!"),
                critical = listOf("Critical!"),
                defeat = listOf("Defeated..."),
                victory = listOf("Victory!"),
            )
        assertNotNull(quotes.randomAttack())
        assertNotNull(quotes.randomCritical())
        assertNotNull(quotes.randomDefeat())
        assertNotNull(quotes.randomVictory())
    }

    @Test
    fun `protagonist units have battle quotes`() {
        val protagonists = NamedUnitRepository.getAllProtagonists()
        protagonists.forEach { unit ->
            assertTrue(
                "${unit.name} should have attack quotes",
                unit.battleQuotes.attack.isNotEmpty(),
            )
            assertTrue(
                "${unit.name} should have victory quotes",
                unit.battleQuotes.victory.isNotEmpty(),
            )
        }
    }

    @Test
    fun `boss units have battle quotes`() {
        val bossIds = listOf("boss_captain_voss", "boss_sorceress_mira", "boss_warlord_kael", "boss_emperor_darius")
        bossIds.forEach { bossId ->
            val boss = EnemyRepository.getEnemy(bossId)
            assertNotNull("Boss $bossId should exist", boss)
            assertTrue(
                "Boss ${boss?.name} should have defeat quotes",
                boss?.battleQuotes?.defeat?.isNotEmpty() == true,
            )
        }
    }
}
