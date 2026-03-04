package com.gameaday.opentactics.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SupportConversationTest {
    @Test
    fun `get conversation for knight and archer at rank C`() {
        val conversation =
            SupportConversationRepository.getConversation(
                "player_knight",
                "player_archer",
                SupportRank.C,
            )
        assertNotNull("Should have C-rank conversation", conversation)
        assertTrue(
            "Conversation should contain dialogue",
            conversation!!.dialogue.isNotEmpty(),
        )
    }

    @Test
    fun `get conversation for knight and archer at rank A`() {
        val conversation =
            SupportConversationRepository.getConversation(
                "player_knight",
                "player_archer",
                SupportRank.A,
            )
        assertNotNull("Should have A-rank conversation", conversation)
    }

    @Test
    fun `conversations work with reversed character order`() {
        val conversation =
            SupportConversationRepository.getConversation(
                "player_archer",
                "player_knight",
                SupportRank.C,
            )
        assertNotNull("Should find conversation regardless of ID order", conversation)
    }

    @Test
    fun `get all conversations for a pair`() {
        val conversations =
            SupportConversationRepository.getConversationsForPair(
                "player_knight",
                "player_archer",
            )
        assertEquals("Knight-Archer should have 3 conversations (C, B, A)", 3, conversations.size)
    }

    @Test
    fun `no conversation for nonexistent pair`() {
        val conversation =
            SupportConversationRepository.getConversation(
                "player_knight",
                "nonexistent",
                SupportRank.C,
            )
        assertNull("Should return null for nonexistent pair", conversation)
    }

    @Test
    fun `no conversation for pair at unavailable rank`() {
        val conversation =
            SupportConversationRepository.getConversation(
                "player_knight",
                "player_archer",
                SupportRank.S,
            )
        assertNull("Should return null for S rank (not defined)", conversation)
    }

    @Test
    fun `knight and mage have conversations`() {
        val conversations =
            SupportConversationRepository.getConversationsForPair(
                "player_knight",
                "player_mage",
            )
        assertTrue("Knight-Mage should have conversations", conversations.isNotEmpty())
    }

    @Test
    fun `knight and healer have conversations`() {
        val conversations =
            SupportConversationRepository.getConversationsForPair(
                "player_knight",
                "healer_1",
            )
        assertTrue("Knight-Healer should have conversations", conversations.isNotEmpty())
    }
}
