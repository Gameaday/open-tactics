package com.gameaday.opentactics.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SupportRelationshipTest {
    @Test
    fun `support relationship involves both characters`() {
        val support =
            SupportRelationship(
                characterId1 = "char1",
                characterId2 = "char2",
                rank = SupportRank.C,
            )

        assertTrue(support.involves("char1"))
        assertTrue(support.involves("char2"))
        assertFalse(support.involves("char3"))
    }

    @Test
    fun `support relationship gets other character`() {
        val support =
            SupportRelationship(
                characterId1 = "char1",
                characterId2 = "char2",
                rank = SupportRank.C,
            )

        assertEquals("char2", support.getOtherCharacter("char1"))
        assertEquals("char1", support.getOtherCharacter("char2"))
        assertNull(support.getOtherCharacter("char3"))
    }

    @Test
    fun `support rank C provides correct bonuses`() {
        val support =
            SupportRelationship(
                characterId1 = "char1",
                characterId2 = "char2",
                rank = SupportRank.C,
            )

        val bonuses = support.getBonuses()
        assertEquals(1, bonuses.attack)
        assertEquals(1, bonuses.skill)
        assertEquals(0, bonuses.defense)
    }

    @Test
    fun `support rank B provides correct bonuses`() {
        val support =
            SupportRelationship(
                characterId1 = "char1",
                characterId2 = "char2",
                rank = SupportRank.B,
            )

        val bonuses = support.getBonuses()
        assertEquals(2, bonuses.attack)
        assertEquals(1, bonuses.defense)
        assertEquals(2, bonuses.skill)
        assertEquals(1, bonuses.luck)
    }

    @Test
    fun `support rank A provides correct bonuses`() {
        val support =
            SupportRelationship(
                characterId1 = "char1",
                characterId2 = "char2",
                rank = SupportRank.A,
            )

        val bonuses = support.getBonuses()
        assertEquals(3, bonuses.attack)
        assertEquals(2, bonuses.defense)
        assertEquals(1, bonuses.speed)
        assertEquals(3, bonuses.skill)
        assertEquals(2, bonuses.luck)
    }

    @Test
    fun `support rank S provides correct bonuses`() {
        val support =
            SupportRelationship(
                characterId1 = "char1",
                characterId2 = "char2",
                rank = SupportRank.S,
            )

        val bonuses = support.getBonuses()
        assertEquals(5, bonuses.attack)
        assertEquals(3, bonuses.defense)
        assertEquals(2, bonuses.speed)
        assertEquals(5, bonuses.skill)
        assertEquals(3, bonuses.luck)
    }

    @Test
    fun `support rank NONE provides no bonuses`() {
        val support =
            SupportRelationship(
                characterId1 = "char1",
                characterId2 = "char2",
                rank = SupportRank.NONE,
            )

        val bonuses = support.getBonuses()
        assertEquals(0, bonuses.attack)
        assertEquals(0, bonuses.defense)
        assertEquals(0, bonuses.speed)
        assertEquals(0, bonuses.skill)
        assertEquals(0, bonuses.luck)
    }
}
