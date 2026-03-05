package com.gameaday.opentactics.data

import com.gameaday.opentactics.model.AIBehavior
import com.gameaday.opentactics.model.ChapterObjective
import com.gameaday.opentactics.model.CharacterClass
import com.gameaday.opentactics.model.MapLayout
import com.gameaday.opentactics.model.Position
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CustomMapConfigTest {
    @Test
    fun `default config has sensible defaults`() {
        val config = CustomMapConfig()
        assertEquals("Custom Battle", config.name)
        assertEquals(MapLayout.PLAINS_BATTLE, config.mapLayout)
        assertEquals(ChapterObjective.DEFEAT_ALL_ENEMIES, config.objective)
        assertNull(config.turnLimit)
        assertTrue(config.playerUnits.isNotEmpty())
        assertTrue(config.enemyUnits.isNotEmpty())
        assertNotNull(config.configId)
    }

    @Test
    fun `default player units have correct configuration`() {
        val playerUnits = CustomMapConfig.defaultPlayerUnits()
        assertEquals(3, playerUnits.size)

        // Knight
        assertEquals("Knight", playerUnits[0].name)
        assertEquals(CharacterClass.KNIGHT, playerUnits[0].characterClass)
        assertEquals(CustomUnitTeam.PLAYER, playerUnits[0].team)
        assertEquals(1, playerUnits[0].level)
        assertTrue(playerUnits[0].weaponIds.contains("iron_sword"))

        // Archer
        assertEquals("Archer", playerUnits[1].name)
        assertEquals(CharacterClass.ARCHER, playerUnits[1].characterClass)
        assertTrue(playerUnits[1].weaponIds.contains("iron_bow"))

        // Mage
        assertEquals("Mage", playerUnits[2].name)
        assertEquals(CharacterClass.MAGE, playerUnits[2].characterClass)
        assertTrue(playerUnits[2].weaponIds.contains("fire"))
    }

    @Test
    fun `default enemy units have correct configuration`() {
        val enemyUnits = CustomMapConfig.defaultEnemyUnits()
        assertEquals(2, enemyUnits.size)

        assertEquals(CustomUnitTeam.ENEMY, enemyUnits[0].team)
        assertEquals(AIBehavior.AGGRESSIVE, enemyUnits[0].aiType)
        assertEquals(CustomUnitTeam.ENEMY, enemyUnits[1].team)
    }

    @Test
    fun `config can be customized with copy`() {
        val config = CustomMapConfig()
        val modified =
            config.copy(
                name = "My Battle",
                mapLayout = MapLayout.FOREST_AMBUSH,
                objective = ChapterObjective.DEFEAT_BOSS,
                turnLimit = 25,
            )

        assertEquals("My Battle", modified.name)
        assertEquals(MapLayout.FOREST_AMBUSH, modified.mapLayout)
        assertEquals(ChapterObjective.DEFEAT_BOSS, modified.objective)
        assertEquals(25, modified.turnLimit)
    }

    @Test
    fun `each config gets unique ID`() {
        val config1 = CustomMapConfig()
        val config2 = CustomMapConfig()
        assertNotEquals(config1.configId, config2.configId)
    }

    @Test
    fun `custom unit config has correct defaults`() {
        val unit =
            CustomUnitConfig(
                name = "Test Knight",
                characterClass = CharacterClass.KNIGHT,
            )
        assertEquals("Test Knight", unit.name)
        assertEquals(CharacterClass.KNIGHT, unit.characterClass)
        assertEquals(1, unit.level)
        assertEquals(Position(0, 0), unit.position)
        assertEquals(CustomUnitTeam.PLAYER, unit.team)
        assertTrue(unit.weaponIds.isEmpty())
        assertTrue(unit.itemIds.isEmpty())
        assertEquals(false, unit.isBoss)
        assertEquals(AIBehavior.AGGRESSIVE, unit.aiType)
    }

    @Test
    fun `custom unit config supports all character classes`() {
        val classes = CharacterClass.values()
        classes.forEach { charClass ->
            val unit =
                CustomUnitConfig(
                    name = "Test ${charClass.displayName}",
                    characterClass = charClass,
                )
            assertEquals(charClass, unit.characterClass)
        }
    }

    @Test
    fun `max constants are reasonable`() {
        assertTrue(CustomMapConfig.MAX_PLAYER_UNITS > 0)
        assertTrue(CustomMapConfig.MAX_ENEMY_UNITS > 0)
        assertTrue(CustomMapConfig.MAX_UNIT_LEVEL > 0)
        assertEquals(20, CustomMapConfig.MAX_UNIT_LEVEL)
    }

    @Test
    fun `config with empty units can be created`() {
        val config =
            CustomMapConfig(
                playerUnits = emptyList(),
                enemyUnits = emptyList(),
            )
        assertTrue(config.playerUnits.isEmpty())
        assertTrue(config.enemyUnits.isEmpty())
    }

    @Test
    fun `config supports all map layouts`() {
        MapLayout.values().forEach { layout ->
            val config = CustomMapConfig(mapLayout = layout)
            assertEquals(layout, config.mapLayout)
        }
    }

    @Test
    fun `config supports all chapter objectives`() {
        ChapterObjective.values().forEach { objective ->
            val config = CustomMapConfig(objective = objective)
            assertEquals(objective, config.objective)
        }
    }

    @Test
    fun `custom unit supports boss flag and AI types`() {
        val bossUnit =
            CustomUnitConfig(
                name = "Boss",
                characterClass = CharacterClass.WYVERN_RIDER,
                level = 10,
                isBoss = true,
                aiType = AIBehavior.STATIONARY,
                team = CustomUnitTeam.ENEMY,
            )
        assertEquals(true, bossUnit.isBoss)
        assertEquals(AIBehavior.STATIONARY, bossUnit.aiType)
        assertEquals(10, bossUnit.level)
    }
}
