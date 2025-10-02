package com.gameaday.opentactics.game

import com.gameaday.opentactics.model.Character
import com.gameaday.opentactics.model.CharacterClass
import com.gameaday.opentactics.model.GameBoard
import com.gameaday.opentactics.model.Item
import com.gameaday.opentactics.model.Position
import com.gameaday.opentactics.model.SupportRank
import com.gameaday.opentactics.model.SupportRelationship
import com.gameaday.opentactics.model.Team
import com.gameaday.opentactics.model.TerrainType
import com.gameaday.opentactics.model.Weapon
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SupportSystemTest {
    private lateinit var gameState: GameState
    private lateinit var board: GameBoard
    private lateinit var knight: Character
    private lateinit var archer: Character
    private lateinit var mage: Character

    @Before
    fun setup() {
        board = GameBoard(10, 10)

        knight =
            Character(
                id = "knight",
                name = "Knight",
                characterClass = CharacterClass.KNIGHT,
                team = Team.PLAYER,
                position = Position(0, 0),
            )

        archer =
            Character(
                id = "archer",
                name = "Archer",
                characterClass = CharacterClass.ARCHER,
                team = Team.PLAYER,
                position = Position(1, 0),
            )

        mage =
            Character(
                id = "mage",
                name = "Mage",
                characterClass = CharacterClass.MAGE,
                team = Team.PLAYER,
                position = Position(5, 5),
            )

        gameState = GameState(board)
        gameState.addPlayerCharacter(knight)
        gameState.addPlayerCharacter(archer)
        gameState.addPlayerCharacter(mage)

        board.placeCharacter(knight, knight.position)
        board.placeCharacter(archer, archer.position)
        board.placeCharacter(mage, mage.position)
    }

    @Test
    fun `can add support relationship`() {
        val support =
            SupportRelationship(
                characterId1 = "knight",
                characterId2 = "archer",
                rank = SupportRank.C,
            )

        gameState.addSupportRelationship(support)

        val retrieved = gameState.getSupportRelationship("knight", "archer")
        assertNotNull(retrieved)
        assertEquals(SupportRank.C, retrieved?.rank)
    }

    @Test
    fun `can get support relationship in both directions`() {
        val support =
            SupportRelationship(
                characterId1 = "knight",
                characterId2 = "archer",
                rank = SupportRank.B,
            )

        gameState.addSupportRelationship(support)

        val retrieved1 = gameState.getSupportRelationship("knight", "archer")
        val retrieved2 = gameState.getSupportRelationship("archer", "knight")

        assertNotNull(retrieved1)
        assertNotNull(retrieved2)
        assertEquals(retrieved1, retrieved2)
    }

    @Test
    fun `get character supports returns all relationships`() {
        gameState.addSupportRelationship(
            SupportRelationship(
                characterId1 = "knight",
                characterId2 = "archer",
                rank = SupportRank.C,
            ),
        )
        gameState.addSupportRelationship(
            SupportRelationship(
                characterId1 = "knight",
                characterId2 = "mage",
                rank = SupportRank.B,
            ),
        )

        val knightSupports = gameState.getCharacterSupports("knight")
        assertEquals(2, knightSupports.size)

        val archerSupports = gameState.getCharacterSupports("archer")
        assertEquals(1, archerSupports.size)
    }

    @Test
    fun `support bonuses apply when units are adjacent`() {
        gameState.addSupportRelationship(
            SupportRelationship(
                characterId1 = "knight",
                characterId2 = "archer",
                rank = SupportRank.C,
            ),
        )

        // Units are adjacent (positions 0,0 and 1,0)
        val bonuses = gameState.getSupportBonuses(knight, board)

        // C rank provides +1 ATK, +1 SKL
        assertEquals(1, bonuses.attack)
        assertEquals(1, bonuses.skill)
    }

    @Test
    fun `support bonuses do not apply when units are not adjacent`() {
        gameState.addSupportRelationship(
            SupportRelationship(
                characterId1 = "knight",
                characterId2 = "mage",
                rank = SupportRank.A,
            ),
        )

        // Units are not adjacent (positions 0,0 and 5,5)
        val bonuses = gameState.getSupportBonuses(knight, board)

        // No bonuses should apply
        assertEquals(0, bonuses.attack)
        assertEquals(0, bonuses.defense)
        assertEquals(0, bonuses.speed)
        assertEquals(0, bonuses.skill)
        assertEquals(0, bonuses.luck)
    }

    @Test
    fun `multiple support bonuses stack`() {
        // Knight at 0,0 with archer at 1,0 and mage at 0,1
        board.moveCharacter(mage, Position(0, 1))

        gameState.addSupportRelationship(
            SupportRelationship(
                characterId1 = "knight",
                characterId2 = "archer",
                rank = SupportRank.C,
            ),
        )
        gameState.addSupportRelationship(
            SupportRelationship(
                characterId1 = "knight",
                characterId2 = "mage",
                rank = SupportRank.C,
            ),
        )

        val bonuses = gameState.getSupportBonuses(knight, board)

        // Two C rank supports: 2x(+1 ATK, +1 SKL) = +2 ATK, +2 SKL
        assertEquals(2, bonuses.attack)
        assertEquals(2, bonuses.skill)
    }
}

class TradingSystemTest {
    private lateinit var knight: Character
    private lateinit var archer: Character

    @Before
    fun setup() {
        knight =
            Character(
                id = "knight",
                name = "Knight",
                characterClass = CharacterClass.KNIGHT,
                team = Team.PLAYER,
                position = Position(0, 0),
            )

        archer =
            Character(
                id = "archer",
                name = "Archer",
                characterClass = CharacterClass.ARCHER,
                team = Team.PLAYER,
                position = Position(1, 0),
            )

        // Add weapons
        knight.addWeapon(Weapon.ironSword())
        knight.addWeapon(Weapon.steelSword())
        archer.addWeapon(Weapon.ironBow())

        // Add items
        knight.addItem(Item.vulnerary())
        archer.addItem(Item.tonic())
    }

    @Test
    fun `characters can trade weapons`() {
        val knightWeapon = knight.inventory[0]
        val archerWeapon = archer.inventory[0]

        // Swap weapons
        knight.inventory[0] = archerWeapon
        archer.inventory[0] = knightWeapon

        assertEquals(archerWeapon, knight.inventory[0])
        assertEquals(knightWeapon, archer.inventory[0])
    }

    @Test
    fun `characters can trade consumable items`() {
        val knightItem = knight.items[0]
        val archerItem = archer.items[0]

        // Swap items
        knight.items[0] = archerItem
        archer.items[0] = knightItem

        assertEquals(archerItem, knight.items[0])
        assertEquals(knightItem, archer.items[0])
    }

    @Test
    fun `can give weapon to character with empty slot`() {
        val weapon = knight.inventory[0]
        knight.inventory.remove(weapon)
        archer.inventory.add(weapon)

        assertTrue(archer.inventory.contains(weapon))
        assertEquals(2, archer.inventory.size)
    }

    @Test
    fun `can give item to character`() {
        val item = knight.items[0]
        knight.items.remove(item)
        archer.items.add(item)

        assertTrue(archer.items.contains(item))
        assertEquals(2, archer.items.size)
    }

    @Test
    fun `trading respects inventory limits`() {
        // Fill knight's inventory to max
        while (knight.inventory.size < Character.MAX_INVENTORY_SIZE) {
            knight.addWeapon(Weapon.ironSword())
        }

        assertEquals(Character.MAX_INVENTORY_SIZE, knight.inventory.size)

        // Cannot add more weapons
        val result = knight.addWeapon(Weapon.steelSword())
        assertEquals(false, result)
        assertEquals(Character.MAX_INVENTORY_SIZE, knight.inventory.size)
    }
}
