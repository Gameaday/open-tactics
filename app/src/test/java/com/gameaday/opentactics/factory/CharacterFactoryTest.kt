package com.gameaday.opentactics.factory

import com.gameaday.opentactics.model.AIBehavior
import com.gameaday.opentactics.model.CharacterClass
import com.gameaday.opentactics.model.Position
import com.gameaday.opentactics.model.Team
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CharacterFactoryTest {
    @Test
    fun testCreatePlayerCharacter() {
        val character =
            CharacterFactory.createPlayerCharacter(
                id = "test_knight",
                name = "Test Knight",
                characterClass = CharacterClass.KNIGHT,
                position = Position(0, 0),
                level = 1,
            )

        assertEquals("test_knight", character.id)
        assertEquals("Test Knight", character.name)
        assertEquals(CharacterClass.KNIGHT, character.characterClass)
        assertEquals(Team.PLAYER, character.team)
        assertEquals(Position(0, 0), character.position)
        assertEquals(1, character.level)

        // Should have default weapon equipped
        assertTrue(character.inventory.isNotEmpty())
        assertTrue(character.equippedWeaponIndex >= 0)
    }

    @Test
    fun testCreatePlayerCharacterAtHigherLevel() {
        val character =
            CharacterFactory.createPlayerCharacter(
                id = "test_knight",
                name = "Test Knight",
                characterClass = CharacterClass.KNIGHT,
                position = Position(0, 0),
                level = 5,
            )

        assertEquals(5, character.level)
        // Should have grown stats
        assertTrue(character.statBonuses.hp > 0 || character.statBonuses.attack > 0)
    }

    @Test
    fun testCreateEnemyCharacter() {
        val character =
            CharacterFactory.createEnemyCharacter(
                id = "enemy_soldier",
                name = "Enemy Soldier",
                characterClass = CharacterClass.KNIGHT,
                position = Position(5, 5),
                level = 3,
                weaponIds = listOf("iron_sword", "steel_sword"),
                isBoss = false,
                aiType = AIBehavior.DEFENSIVE,
            )

        assertEquals("enemy_soldier", character.id)
        assertEquals("Enemy Soldier", character.name)
        assertEquals(Team.ENEMY, character.team)
        assertEquals(3, character.level)
        assertEquals(false, character.isBoss)
        assertEquals(AIBehavior.DEFENSIVE, character.aiType)

        // Should have two weapons
        assertEquals(2, character.inventory.size)
        assertTrue(character.equippedWeaponIndex >= 0)
    }

    @Test
    fun testCreateEnemyCharacterWithDefaultWeapon() {
        val character =
            CharacterFactory.createEnemyCharacter(
                id = "enemy_archer",
                name = "Enemy Archer",
                characterClass = CharacterClass.ARCHER,
                position = Position(10, 10),
                level = 1,
            )

        // Should have default bow
        assertEquals(1, character.inventory.size)
        assertEquals("iron_bow", character.inventory[0].id)
    }

    @Test
    fun testCreateNeutralCharacter() {
        val character =
            CharacterFactory.createNeutralCharacter(
                id = "npc_villager",
                name = "Villager",
                characterClass = CharacterClass.THIEF,
                position = Position(3, 3),
                level = 2,
            )

        assertEquals(Team.NEUTRAL, character.team)
        assertEquals(2, character.level)
    }

    @Test
    fun testCreatePlayerParty() {
        val specs =
            listOf(
                CharacterFactory.CharacterSpec(
                    id = "knight1",
                    name = "Knight",
                    characterClass = CharacterClass.KNIGHT,
                    position = Position(0, 0),
                    level = 1,
                    additionalWeaponIds = listOf("steel_sword"),
                    itemIds = listOf("vulnerary"),
                ),
                CharacterFactory.CharacterSpec(
                    id = "archer1",
                    name = "Archer",
                    characterClass = CharacterClass.ARCHER,
                    position = Position(1, 0),
                    level = 1,
                ),
            )

        val party = CharacterFactory.createPlayerParty(specs)

        assertEquals(2, party.size)

        val knight = party[0]
        assertEquals("knight1", knight.id)
        assertEquals(Team.PLAYER, knight.team)
        assertEquals(2, knight.inventory.size) // Default + steel sword
        assertEquals(1, knight.items.size) // Vulnerary

        val archer = party[1]
        assertEquals("archer1", archer.id)
        assertEquals(1, archer.inventory.size) // Just default bow
    }

    @Test
    fun testCreateBossCharacter() {
        val character =
            CharacterFactory.createEnemyCharacter(
                id = "boss_knight",
                name = "Boss Knight",
                characterClass = CharacterClass.KNIGHT,
                position = Position(11, 5),
                level = 10,
                weaponIds = listOf("silver_sword"),
                isBoss = true,
                aiType = AIBehavior.AGGRESSIVE,
            )

        assertEquals(true, character.isBoss)
        assertEquals(AIBehavior.AGGRESSIVE, character.aiType)
        assertEquals(10, character.level)
    }

    @Test
    fun testAllCreatedCharactersAreAlive() {
        val playerChar =
            CharacterFactory.createPlayerCharacter(
                "test1",
                "Test",
                CharacterClass.KNIGHT,
                Position(0, 0),
            )
        val enemyChar =
            CharacterFactory.createEnemyCharacter(
                "test2",
                "Enemy",
                CharacterClass.KNIGHT,
                Position(1, 1),
            )
        val neutralChar =
            CharacterFactory.createNeutralCharacter(
                "test3",
                "NPC",
                CharacterClass.KNIGHT,
                Position(2, 2),
            )

        assertTrue(playerChar.isAlive)
        assertTrue(enemyChar.isAlive)
        assertTrue(neutralChar.isAlive)

        assertTrue(playerChar.currentHp > 0)
        assertTrue(enemyChar.currentHp > 0)
        assertTrue(neutralChar.currentHp > 0)
    }

    @Test
    fun testCharacterHasValidStats() {
        val character =
            CharacterFactory.createPlayerCharacter(
                "test",
                "Test",
                CharacterClass.MAGE,
                Position(0, 0),
                level = 3,
            )

        assertNotNull(character.currentStats)
        assertTrue(character.maxHp > 0)
        assertTrue(character.maxMp >= 0)
        assertTrue(character.currentHp > 0)
    }
}
