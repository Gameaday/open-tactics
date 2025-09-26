package com.gameaday.opentactics.model

import org.junit.Test
import org.junit.Assert.*

class CharacterClassTest {

    @Test
    fun testKnightStats() {
        val knight = CharacterClass.KNIGHT
        assertEquals("Knight", knight.displayName)
        assertEquals(25, knight.baseStats.hp)
        assertEquals(5, knight.baseStats.mp)
        assertEquals(12, knight.baseStats.attack)
        assertEquals(14, knight.baseStats.defense)
        assertEquals(8, knight.baseStats.speed)
        assertEquals(10, knight.baseStats.skill)
        assertEquals(8, knight.baseStats.luck)
        assertEquals(3, knight.movementRange)
        assertEquals(1, knight.attackRange)
    }

    @Test
    fun testArcherStats() {
        val archer = CharacterClass.ARCHER
        assertEquals("Archer", archer.displayName)
        assertEquals(18, archer.baseStats.hp)
        assertEquals(8, archer.baseStats.mp)
        assertEquals(14, archer.baseStats.attack)
        assertEquals(8, archer.baseStats.defense)
        assertEquals(12, archer.baseStats.speed)
        assertEquals(16, archer.baseStats.skill)
        assertEquals(10, archer.baseStats.luck)
        assertEquals(4, archer.movementRange)
        assertEquals(3, archer.attackRange)
    }

    @Test
    fun testMageStats() {
        val mage = CharacterClass.MAGE
        assertEquals("Mage", mage.displayName)
        assertEquals(15, mage.baseStats.hp)
        assertEquals(20, mage.baseStats.mp)
        assertEquals(16, mage.baseStats.attack)
        assertEquals(6, mage.baseStats.defense)
        assertEquals(10, mage.baseStats.speed)
        assertEquals(18, mage.baseStats.skill)
        assertEquals(12, mage.baseStats.luck)
        assertEquals(3, mage.movementRange)
        assertEquals(2, mage.attackRange)
    }

    @Test
    fun testHealerStats() {
        val healer = CharacterClass.HEALER
        assertEquals("Healer", healer.displayName)
        assertEquals(20, healer.baseStats.hp)
        assertEquals(18, healer.baseStats.mp)
        assertEquals(8, healer.baseStats.attack)
        assertEquals(10, healer.baseStats.defense)
        assertEquals(11, healer.baseStats.speed)
        assertEquals(15, healer.baseStats.skill)
        assertEquals(14, healer.baseStats.luck)
        assertEquals(3, healer.movementRange)
        assertEquals(2, healer.attackRange)
    }

    @Test
    fun testThiefStats() {
        val thief = CharacterClass.THIEF
        assertEquals("Thief", thief.displayName)
        assertEquals(16, thief.baseStats.hp)
        assertEquals(10, thief.baseStats.mp)
        assertEquals(11, thief.baseStats.attack)
        assertEquals(7, thief.baseStats.defense)
        assertEquals(18, thief.baseStats.speed)
        assertEquals(14, thief.baseStats.skill)
        assertEquals(16, thief.baseStats.luck)
        assertEquals(5, thief.movementRange)
        assertEquals(1, thief.attackRange)
    }

    @Test
    fun testAllClassesHaveValidStats() {
        for (characterClass in CharacterClass.values()) {
            assertTrue("${characterClass.displayName} should have positive HP", characterClass.baseStats.hp > 0)
            assertTrue("${characterClass.displayName} should have non-negative MP", characterClass.baseStats.mp >= 0)
            assertTrue("${characterClass.displayName} should have positive attack", characterClass.baseStats.attack > 0)
            assertTrue("${characterClass.displayName} should have non-negative defense", characterClass.baseStats.defense >= 0)
            assertTrue("${characterClass.displayName} should have positive speed", characterClass.baseStats.speed > 0)
            assertTrue("${characterClass.displayName} should have positive skill", characterClass.baseStats.skill > 0)
            assertTrue("${characterClass.displayName} should have positive luck", characterClass.baseStats.luck > 0)
            assertTrue("${characterClass.displayName} should have positive movement range", characterClass.movementRange > 0)
            assertTrue("${characterClass.displayName} should have positive attack range", characterClass.attackRange > 0)
        }
    }
}