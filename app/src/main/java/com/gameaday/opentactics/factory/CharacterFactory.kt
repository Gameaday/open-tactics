@file:Suppress("LongParameterList") // Character creation inherently requires many parameters

package com.gameaday.opentactics.factory

import com.gameaday.opentactics.model.AIBehavior
import com.gameaday.opentactics.model.Character
import com.gameaday.opentactics.model.CharacterClass
import com.gameaday.opentactics.model.NamedUnit
import com.gameaday.opentactics.model.Position
import com.gameaday.opentactics.model.Stats
import com.gameaday.opentactics.model.Team

/**
 * Factory for creating characters for the game.
 * This centralizes character creation and provides convenience methods for different scenarios.
 */
object CharacterFactory {
    private const val EXPERIENCE_PER_LEVEL = 100

    /**
     * Create a basic player character with default equipment
     * @param id Character identifier
     * @param name Character name
     * @param characterClass The character's class
     * @param position Starting position
     * @param level Starting level (default 1)
     * @return A new player character with default weapon equipped
     */
    fun createPlayerCharacter(
        id: String,
        name: String,
        characterClass: CharacterClass,
        position: Position,
        level: Int = 1,
    ): Character {
        val character =
            Character(
                id = id,
                name = name,
                characterClass = characterClass,
                team = Team.PLAYER,
                position = position,
                level = 1,
                statBonuses = Stats(0, 0, 0, 0, 0, 0, 0),
            )

        // Add default weapon
        val defaultWeapon = WeaponFactory.getDefaultWeapon(characterClass)
        character.addWeapon(defaultWeapon)
        character.equipWeapon(0)

        // Level up to target level if needed
        if (level > 1) {
            val expNeeded = EXPERIENCE_PER_LEVEL * (level - 1)
            character.gainExperience(expNeeded)
        }

        return character
    }

    /**
     * Create an enemy character
     * @param id Character identifier
     * @param name Character name
     * @param characterClass The character's class
     * @param position Starting position
     * @param level Character level (default 1)
     * @param weaponIds List of weapon IDs to equip (first will be equipped)
     * @param isBoss Whether this is a boss character
     * @param aiType AI behavior pattern
     * @return A new enemy character
     */
    fun createEnemyCharacter(
        id: String,
        name: String,
        characterClass: CharacterClass,
        position: Position,
        level: Int = 1,
        weaponIds: List<String> = emptyList(),
        isBoss: Boolean = false,
        aiType: AIBehavior = AIBehavior.AGGRESSIVE,
    ): Character {
        val character =
            Character(
                id = id,
                name = name,
                characterClass = characterClass,
                team = Team.ENEMY,
                position = position,
                level = 1,
                isBoss = isBoss,
                aiType = aiType,
                statBonuses = Stats(0, 0, 0, 0, 0, 0, 0),
            )

        // Add weapons by ID
        weaponIds.forEach { weaponId ->
            val weapon = WeaponFactory.createWeapon(weaponId)
            if (weapon != null) {
                character.addWeapon(weapon)
            }
        }

        // If no weapons provided, add default weapon
        if (weaponIds.isEmpty()) {
            val defaultWeapon = WeaponFactory.getDefaultWeapon(characterClass)
            character.addWeapon(defaultWeapon)
        }

        // Equip first weapon if any
        if (character.inventory.isNotEmpty()) {
            character.equipWeapon(0)
        }

        // Level up to target level if needed
        if (level > 1) {
            val expNeeded = EXPERIENCE_PER_LEVEL * (level - 1)
            character.gainExperience(expNeeded)
        }

        return character
    }

    /**
     * Create a character from a NamedUnit with custom growth rates
     * @param namedUnit The named unit template
     * @param team Which team the character is on
     * @param position Starting position
     * @param targetLevel Target level to reach
     * @param isBoss Whether this is a boss character
     * @param aiType AI behavior pattern
     * @return A new character with custom growth rates
     */
    fun createFromNamedUnit(
        namedUnit: NamedUnit,
        team: Team,
        position: Position,
        targetLevel: Int = 1,
        isBoss: Boolean = false,
        aiType: AIBehavior = AIBehavior.AGGRESSIVE,
    ): Character = Character.fromNamedUnit(namedUnit, team, position, targetLevel, isBoss, aiType)

    /**
     * Create a neutral character (for NPCs)
     * @param id Character identifier
     * @param name Character name
     * @param characterClass The character's class
     * @param position Starting position
     * @param level Starting level
     * @return A new neutral character
     */
    fun createNeutralCharacter(
        id: String,
        name: String,
        characterClass: CharacterClass,
        position: Position,
        level: Int = 1,
    ): Character {
        val character =
            Character(
                id = id,
                name = name,
                characterClass = characterClass,
                team = Team.NEUTRAL,
                position = position,
                level = 1,
                statBonuses = Stats(0, 0, 0, 0, 0, 0, 0),
            )

        // Level up to target level if needed
        if (level > 1) {
            val expNeeded = EXPERIENCE_PER_LEVEL * (level - 1)
            character.gainExperience(expNeeded)
        }

        return character
    }

    /**
     * Create multiple player characters from a list of specifications
     * Useful for creating a starting party
     */
    fun createPlayerParty(specs: List<CharacterSpec>): List<Character> =
        specs.map { spec ->
            createPlayerCharacter(
                id = spec.id,
                name = spec.name,
                characterClass = spec.characterClass,
                position = spec.position,
                level = spec.level,
            ).apply {
                // Add any additional weapons
                spec.additionalWeaponIds.forEach { weaponId ->
                    WeaponFactory.createWeapon(weaponId)?.let { addWeapon(it) }
                }
                // Add items
                spec.itemIds.forEach { itemId ->
                    ItemFactory.createItem(itemId)?.let { addItem(it) }
                }
            }
        }

    /**
     * Specification for creating a character
     */
    data class CharacterSpec(
        val id: String,
        val name: String,
        val characterClass: CharacterClass,
        val position: Position,
        val level: Int = 1,
        val additionalWeaponIds: List<String> = emptyList(),
        val itemIds: List<String> = emptyList(),
    )
}
