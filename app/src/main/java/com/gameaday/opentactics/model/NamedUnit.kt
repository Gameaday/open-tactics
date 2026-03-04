package com.gameaday.opentactics.model

import kotlinx.serialization.Serializable

/**
 * Battle quotes for named characters — shown during combat, criticals, victory, and defeat.
 */
@Serializable
data class BattleQuotes(
    val attack: List<String> = emptyList(),
    val critical: List<String> = emptyList(),
    val defeat: List<String> = emptyList(),
    val victory: List<String> = emptyList(),
) {
    fun randomAttack(): String? = attack.randomOrNull()

    fun randomCritical(): String? = critical.randomOrNull()

    fun randomDefeat(): String? = defeat.randomOrNull()

    fun randomVictory(): String? = victory.randomOrNull()
}

/**
 * Represents a named unit definition with unique characteristics
 * Named units have their own growth rates which override the default class growth rates
 * This allows for unique character progression (e.g., Est, Nino, Marcus type units)
 */
@Serializable
data class NamedUnit(
    val id: String,
    val name: String,
    val characterClass: CharacterClass,
    val customGrowthRates: GrowthRates? = null, // If null, uses class default
    val description: String = "",
    val battleQuotes: BattleQuotes = BattleQuotes(),
) {
    /**
     * Get the growth rates for this named unit
     * Returns custom growth rates if defined, otherwise uses class default
     */
    fun getGrowthRates(): GrowthRates = customGrowthRates ?: characterClass.growthRates

    /**
     * Get the base stats for this named unit at level 1
     * Uses the character class's base stats
     */
    fun getBaseStats(): Stats = characterClass.baseStats
}

/**
 * Repository for protagonist/player character definitions
 * These are the recurring named characters that join the player's army
 */
object NamedUnitRepository {
    /**
     * Get a named protagonist by ID
     */
    fun getProtagonist(id: String): NamedUnit? = protagonists[id]

    /**
     * Get all protagonists
     */
    fun getAllProtagonists(): List<NamedUnit> = protagonists.values.toList()

    private val protagonists =
        mapOf(
            // Main player characters - balanced growths
            "player_knight" to
                NamedUnit(
                    id = "player_knight",
                    name = "Sir Garrett",
                    characterClass = CharacterClass.KNIGHT,
                    customGrowthRates =
                        GrowthRates(
                            hp = 85, // Better HP growth than standard knight
                            mp = 20,
                            attack = 65, // Better attack
                            defense = 70,
                            speed = 45, // Better speed
                            skill = 55,
                            luck = 50,
                        ),
                    description = "A veteran knight with exceptional combat prowess",
                    battleQuotes =
                        BattleQuotes(
                            attack = listOf("For Valdris!", "Stand and fight!"),
                            critical = listOf("This ends NOW!", "Feel my blade!"),
                            defeat = listOf("I... won't fall here... not yet..."),
                            victory = listOf("Another battle won. Press onward!", "Well fought, everyone."),
                        ),
                ),
            "player_archer" to
                NamedUnit(
                    id = "player_archer",
                    name = "Lyanna",
                    characterClass = CharacterClass.ARCHER,
                    customGrowthRates =
                        GrowthRates(
                            hp = 65,
                            mp = 30,
                            attack = 75, // Excellent attack
                            defense = 45,
                            speed = 70, // Great speed
                            skill = 85, // Exceptional skill
                            luck = 60,
                        ),
                    description = "A sharpshooter with unparalleled accuracy",
                    battleQuotes =
                        BattleQuotes(
                            attack = listOf("Bullseye!", "You won't escape my aim."),
                            critical = listOf("Right between the eyes!", "Perfect shot!"),
                            defeat = listOf("My aim... was off..."),
                            victory = listOf("Not a single arrow wasted.", "Hah! Too easy."),
                        ),
                ),
            "player_mage" to
                NamedUnit(
                    id = "player_mage",
                    name = "Aldric",
                    characterClass = CharacterClass.MAGE,
                    customGrowthRates =
                        GrowthRates(
                            hp = 55,
                            mp = 80, // Excellent MP growth
                            attack = 80, // Great magic power
                            defense = 35,
                            speed = 60,
                            skill = 75,
                            luck = 65,
                        ),
                    description = "A gifted mage with immense magical potential",
                    battleQuotes =
                        BattleQuotes(
                            attack = listOf("By the ancient arts!", "Taste my magic!"),
                            critical = listOf("Burn to cinders!", "The arcane answers my call!"),
                            defeat = listOf("My power... wasn't enough..."),
                            victory = listOf("Knowledge is the greatest weapon.", "The darkness recedes."),
                        ),
                ),
            // Late-joining units with different stat distributions
            "healer_1" to
                NamedUnit(
                    id = "healer_1",
                    name = "Elara",
                    characterClass = CharacterClass.HEALER,
                    customGrowthRates =
                        GrowthRates(
                            hp = 70,
                            mp = 75, // Great MP for healing
                            attack = 40,
                            defense = 50,
                            speed = 65,
                            skill = 70,
                            luck = 75, // High luck
                        ),
                    description = "A devoted healer with natural talent",
                    battleQuotes =
                        BattleQuotes(
                            attack = listOf("I heal, but I can fight too!"),
                            critical = listOf("By the light!"),
                            defeat = listOf("Please... someone carry on..."),
                            victory = listOf("Is everyone alright? Let me tend to the wounded."),
                        ),
                ),
            "thief_1" to
                NamedUnit(
                    id = "thief_1",
                    name = "Raven",
                    characterClass = CharacterClass.THIEF,
                    customGrowthRates =
                        GrowthRates(
                            hp = 60,
                            mp = 35,
                            attack = 60,
                            defense = 40,
                            speed = 90, // Exceptional speed
                            skill = 75,
                            luck = 85, // Exceptional luck
                        ),
                    description = "A nimble thief with incredible reflexes",
                    battleQuotes =
                        BattleQuotes(
                            attack = listOf("Too slow!", "Now you see me..."),
                            critical = listOf("Didn't even see that coming, did you?"),
                            defeat = listOf("Tch... I let my guard down..."),
                            victory = listOf("Easy pickings.", "Any loot worth taking?"),
                        ),
                ),
            "pegasus_knight_1" to
                NamedUnit(
                    id = "pegasus_knight_1",
                    name = "Celeste",
                    characterClass = CharacterClass.PEGASUS_KNIGHT,
                    customGrowthRates =
                        GrowthRates(
                            hp = 65,
                            mp = 25,
                            attack = 65,
                            defense = 50, // Better defense than typical
                            speed = 80, // Excellent speed
                            skill = 70,
                            luck = 65,
                        ),
                    description = "A seasoned pegasus knight",
                    battleQuotes =
                        BattleQuotes(
                            attack = listOf("From the skies!", "Starwind, charge!"),
                            critical = listOf("A strike from above!"),
                            defeat = listOf("Starwind... fly on without me..."),
                            victory = listOf("The skies are clear.", "Another victory for the wing!"),
                        ),
                ),
        )
}

/**
 * Repository for enemy unit definitions
 * These are generic enemy types that can be reused across chapters
 */
object EnemyRepository {
    /**
     * Get a named enemy by ID
     */
    fun getEnemy(id: String): NamedUnit? = enemies[id]

    /**
     * Get all enemies
     */
    fun getAllEnemies(): List<NamedUnit> = enemies.values.toList()

    private val enemies =
        mapOf(
            // Generic enemy units - use default class growths
            "generic_bandit" to
                NamedUnit(
                    id = "generic_bandit",
                    name = "Bandit",
                    characterClass = CharacterClass.KNIGHT,
                    description = "A common bandit",
                ),
            "generic_brigand" to
                NamedUnit(
                    id = "generic_brigand",
                    name = "Brigand",
                    characterClass = CharacterClass.ARCHER,
                    description = "A brigand archer",
                ),
            "generic_rogue" to
                NamedUnit(
                    id = "generic_rogue",
                    name = "Rogue",
                    characterClass = CharacterClass.THIEF,
                    description = "A common thief",
                ),
            "generic_dark_mage" to
                NamedUnit(
                    id = "generic_dark_mage",
                    name = "Dark Mage",
                    characterClass = CharacterClass.MAGE,
                    description = "An enemy mage",
                ),
            // Boss units - stronger growths for higher stats
            "boss_bandit_leader" to
                NamedUnit(
                    id = "boss_bandit_leader",
                    name = "Bandit Leader",
                    characterClass = CharacterClass.KNIGHT,
                    customGrowthRates =
                        GrowthRates(
                            hp = 90, // Boss-tier HP
                            mp = 25,
                            attack = 70, // Strong attack
                            defense = 80, // Strong defense
                            speed = 50,
                            skill = 60,
                            luck = 50,
                        ),
                    description = "A fearsome bandit leader",
                ),
            "boss_dark_knight" to
                NamedUnit(
                    id = "boss_dark_knight",
                    name = "Dark Knight Commander",
                    characterClass = CharacterClass.KNIGHT,
                    customGrowthRates =
                        GrowthRates(
                            hp = 95,
                            mp = 30,
                            attack = 75,
                            defense = 85,
                            speed = 55,
                            skill = 65,
                            luck = 55,
                        ),
                    description = "A powerful knight commander",
                ),
            "boss_general" to
                NamedUnit(
                    id = "boss_general",
                    name = "General Ironhold",
                    characterClass = CharacterClass.KNIGHT,
                    customGrowthRates =
                        GrowthRates(
                            hp = 100, // Exceptional HP
                            mp = 35,
                            attack = 80, // Exceptional attack
                            defense = 90, // Exceptional defense
                            speed = 60,
                            skill = 70,
                            luck = 60,
                        ),
                    description = "A legendary general",
                ),
            // Additional enemy types for expanded campaign
            "generic_healer" to
                NamedUnit(
                    id = "generic_healer",
                    name = "Enemy Cleric",
                    characterClass = CharacterClass.HEALER,
                    description = "An enemy healer supporting their allies",
                ),
            "generic_pegasus" to
                NamedUnit(
                    id = "generic_pegasus",
                    name = "Sky Rider",
                    characterClass = CharacterClass.PEGASUS_KNIGHT,
                    description = "An airborne enemy soldier",
                ),
            "generic_wyvern" to
                NamedUnit(
                    id = "generic_wyvern",
                    name = "Wyvern Soldier",
                    characterClass = CharacterClass.WYVERN_RIDER,
                    description = "A heavily armored aerial unit",
                ),
            // Act 2+ boss units
            "boss_captain_voss" to
                NamedUnit(
                    id = "boss_captain_voss",
                    name = "Captain Voss",
                    characterClass = CharacterClass.KNIGHT,
                    customGrowthRates =
                        GrowthRates(
                            hp = 90,
                            mp = 25,
                            attack = 75,
                            defense = 85,
                            speed = 55,
                            skill = 65,
                            luck = 50,
                        ),
                    description = "A ruthless border garrison commander",
                    battleQuotes =
                        BattleQuotes(
                            attack = listOf("You'll never pass my garrison!"),
                            critical = listOf("Crush them!"),
                            defeat = listOf("The garrison... falls..."),
                            victory = listOf("None shall cross this border."),
                        ),
                ),
            "boss_sorceress_mira" to
                NamedUnit(
                    id = "boss_sorceress_mira",
                    name = "Sorceress Mira",
                    characterClass = CharacterClass.MAGE,
                    customGrowthRates =
                        GrowthRates(
                            hp = 65,
                            mp = 90,
                            attack = 85,
                            defense = 40,
                            speed = 70,
                            skill = 80,
                            luck = 65,
                        ),
                    description = "A powerful mage in the service of the Crimson Empire",
                    battleQuotes =
                        BattleQuotes(
                            attack = listOf("Feel the wrath of the arcane!"),
                            critical = listOf("Burn in hellfire!"),
                            defeat = listOf("The Emperor's power... is far beyond mine..."),
                            victory = listOf("Magic always prevails."),
                        ),
                ),
            "boss_warlord_kael" to
                NamedUnit(
                    id = "boss_warlord_kael",
                    name = "Warlord Kael",
                    characterClass = CharacterClass.WYVERN_RIDER,
                    customGrowthRates =
                        GrowthRates(
                            hp = 95,
                            mp = 20,
                            attack = 85,
                            defense = 80,
                            speed = 65,
                            skill = 70,
                            luck = 50,
                        ),
                    description = "Commander of the Crimson Empire's aerial forces",
                    battleQuotes =
                        BattleQuotes(
                            attack = listOf("The skies belong to the Empire!"),
                            critical = listOf("Dive! DIVE!"),
                            defeat = listOf("Impossible... my wyverns were undefeated..."),
                            victory = listOf("The ground dwellers never stood a chance."),
                        ),
                ),
            "boss_emperor_darius" to
                NamedUnit(
                    id = "boss_emperor_darius",
                    name = "Emperor Darius",
                    characterClass = CharacterClass.KNIGHT,
                    customGrowthRates =
                        GrowthRates(
                            hp = 100,
                            mp = 40,
                            attack = 90,
                            defense = 95,
                            speed = 65,
                            skill = 80,
                            luck = 70,
                        ),
                    description = "The corrupted ruler of the Crimson Empire",
                    battleQuotes =
                        BattleQuotes(
                            attack = listOf("You dare challenge an Emperor?!", "Kneel before me!"),
                            critical = listOf("The darkness obeys my will!", "Absolute power!"),
                            defeat = listOf("How... how can this be? I was chosen... by the darkness..."),
                            victory = listOf("None can oppose the Crimson Throne."),
                        ),
                ),
        )
}
