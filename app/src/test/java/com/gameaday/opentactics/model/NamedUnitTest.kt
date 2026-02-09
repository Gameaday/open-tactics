package com.gameaday.opentactics.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NamedUnitTest {
    @Test
    fun `named unit with custom growth rates returns custom rates`() {
        val customGrowth = GrowthRates(90, 80, 70, 60, 50, 40, 30)
        val namedUnit =
            NamedUnit(
                id = "test_unit",
                name = "Test Unit",
                characterClass = CharacterClass.KNIGHT,
                customGrowthRates = customGrowth,
            )

        assertEquals(customGrowth, namedUnit.getGrowthRates())
    }

    @Test
    fun `named unit without custom growth rates returns class default`() {
        val namedUnit =
            NamedUnit(
                id = "test_unit",
                name = "Test Unit",
                characterClass = CharacterClass.KNIGHT,
                customGrowthRates = null,
            )

        assertEquals(CharacterClass.KNIGHT.growthRates, namedUnit.getGrowthRates())
    }

    @Test
    fun `named unit base stats match character class`() {
        val namedUnit =
            NamedUnit(
                id = "test_unit",
                name = "Test Unit",
                characterClass = CharacterClass.MAGE,
            )

        assertEquals(CharacterClass.MAGE.baseStats, namedUnit.getBaseStats())
    }

    @Test
    fun `protagonist repository can retrieve named units`() {
        val knight = NamedUnitRepository.getProtagonist("player_knight")
        assertNotNull(knight)
        assertEquals("Sir Garrett", knight?.name)
        assertEquals(CharacterClass.KNIGHT, knight?.characterClass)
    }

    @Test
    fun `protagonist repository returns null for invalid id`() {
        val invalid = NamedUnitRepository.getProtagonist("invalid_id")
        assertNull(invalid)
    }

    @Test
    fun `enemy repository can retrieve named units`() {
        val bandit = EnemyRepository.getEnemy("generic_bandit")
        assertNotNull(bandit)
        assertEquals("Bandit", bandit?.name)
        assertEquals(CharacterClass.KNIGHT, bandit?.characterClass)
    }

    @Test
    fun `boss units have enhanced growth rates`() {
        val boss = EnemyRepository.getEnemy("boss_general")
        assertNotNull(boss)
        val growthRates = boss?.getGrowthRates()
        assertNotNull(growthRates)

        // Boss should have better growth rates than default Knight
        val defaultKnightGrowth = CharacterClass.KNIGHT.growthRates
        assertTrue((growthRates?.hp ?: 0) > defaultKnightGrowth.hp)
        assertTrue((growthRates?.attack ?: 0) > defaultKnightGrowth.attack)
        assertTrue((growthRates?.defense ?: 0) > defaultKnightGrowth.defense)
    }

    @Test
    fun `character from named unit has correct properties at level 1`() {
        val namedUnit = NamedUnitRepository.getProtagonist("player_knight")
        assertNotNull(namedUnit)

        val character =
            Character.fromNamedUnit(
                namedUnit = namedUnit!!,
                team = Team.PLAYER,
                position = Position(0, 0),
                targetLevel = 1,
            )

        assertEquals(namedUnit.id, character.id)
        assertEquals(namedUnit.name, character.name)
        assertEquals(namedUnit.characterClass, character.characterClass)
        assertEquals(1, character.level)
        assertEquals(Team.PLAYER, character.team)
        assertEquals(namedUnit.customGrowthRates, character.customGrowthRates)
    }

    @Test
    fun `character from named unit at level 5 has stat bonuses`() {
        val namedUnit = NamedUnitRepository.getProtagonist("player_mage")
        assertNotNull(namedUnit)

        val character =
            Character.fromNamedUnit(
                namedUnit = namedUnit!!,
                team = Team.PLAYER,
                position = Position(0, 0),
                targetLevel = 5,
            )

        assertEquals(5, character.level)
        // Should have gained some stats from 4 level ups
        val totalBonuses =
            character.statBonuses.hp + character.statBonuses.mp +
                character.statBonuses.attack + character.statBonuses.defense +
                character.statBonuses.speed + character.statBonuses.skill +
                character.statBonuses.luck
        assertTrue("Character should have gained stats from leveling up", totalBonuses > 0)
    }

    @Test
    fun `character uses custom growth rates for level ups`() {
        // Create a character with 100% HP growth rate
        val namedUnit =
            NamedUnit(
                id = "test_perfect_growth",
                name = "Perfect Growth",
                characterClass = CharacterClass.KNIGHT,
                customGrowthRates =
                    GrowthRates(
                        hp = 100, // Guaranteed HP gain
                        mp = 0,
                        attack = 0,
                        defense = 0,
                        speed = 0,
                        skill = 0,
                        luck = 0,
                    ),
            )

        val character =
            Character.fromNamedUnit(
                namedUnit = namedUnit,
                team = Team.PLAYER,
                position = Position(0, 0),
                targetLevel = 10,
            )

        // With 100% HP growth and 9 level-ups, should have gained 9 HP
        assertEquals(9, character.statBonuses.hp)
    }

    @Test
    fun `enemy spawn with named unit id uses custom growth rates`() {
        val spawn =
            EnemyUnitSpawn(
                id = "test_enemy",
                name = "Test Enemy",
                characterClass = CharacterClass.KNIGHT,
                level = 3,
                position = Position(5, 5),
                equipment = listOf("iron_sword"),
                namedUnitId = "boss_general",
            )

        val character = spawn.toCharacter()
        assertEquals(3, character.level)
        assertNotNull(character.customGrowthRates)

        // Should use the boss growth rates
        val bossUnit = EnemyRepository.getEnemy("boss_general")
        assertEquals(bossUnit?.customGrowthRates, character.customGrowthRates)
    }

    @Test
    fun `enemy spawn without named unit id uses class defaults`() {
        val spawn =
            EnemyUnitSpawn(
                id = "test_enemy",
                name = "Generic Enemy",
                characterClass = CharacterClass.ARCHER,
                level = 2,
                position = Position(3, 3),
                equipment = listOf(),
                namedUnitId = null,
            )

        val character = spawn.toCharacter()
        assertEquals(2, character.level)
        assertNull(character.customGrowthRates)
    }

    @Test
    fun `all protagonists have unique ids and names`() {
        val protagonists = NamedUnitRepository.getAllProtagonists()
        val ids = protagonists.map { it.id }
        val names = protagonists.map { it.name }

        assertEquals("All protagonist IDs should be unique", ids.size, ids.toSet().size)
        assertEquals("All protagonist names should be unique", names.size, names.toSet().size)
    }

    @Test
    fun `all enemies have valid character classes`() {
        val enemies = EnemyRepository.getAllEnemies()
        enemies.forEach { enemy ->
            assertNotNull("Enemy ${enemy.id} should have a valid character class", enemy.characterClass)
        }
    }
}
