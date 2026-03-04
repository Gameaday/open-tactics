@file:Suppress("MagicNumber")

package com.gameaday.opentactics.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

/**
 * Chapter objective types that determine victory conditions
 */
@Serializable
enum class ChapterObjective {
    DEFEAT_ALL_ENEMIES, // Kill all enemy units
    DEFEAT_BOSS, // Kill the boss unit
    SEIZE_THRONE, // Move a unit to the throne tile
    SURVIVE, // Survive for N turns
    ESCAPE, // Get N units to escape tiles
    DEFEND, // Defend an objective for N turns
}

/**
 * Represents a chapter/mission in the campaign
 */
@Parcelize
@Serializable
data class Chapter(
    val id: String,
    val number: Int,
    val title: String,
    val description: String,
    val objective: ChapterObjective,
    val objectiveDetails: String, // e.g., "Defeat the Dark Knight commander"
    val mapLayout: MapLayout,
    val playerStartPositions: List<Position>,
    val enemyUnits: List<EnemyUnitSpawn>,
    val bossUnit: EnemyUnitSpawn? = null,
    val thronePosition: Position? = null, // For SEIZE_THRONE objective
    val escapePositions: List<Position> = emptyList(), // For ESCAPE objective
    val turnLimit: Int? = null, // For SURVIVE/DEFEND objectives
    val requiredEscapes: Int = 1, // Number of units needed to escape
    val preBattleDialogue: String = "",
    val postVictoryDialogue: String = "",
    val postDefeatDialogue: String = "",
    val reinforcements: List<Reinforcement> = emptyList(),
) : Parcelable {
    /**
     * Check if chapter objective is complete
     */
    fun isObjectiveComplete(
        playerCharacters: List<Character>,
        enemyCharacters: List<Character>,
        turnCount: Int,
        unitsOnThrone: List<Character>,
        escapedUnits: Int,
    ): Boolean =
        when (objective) {
            ChapterObjective.DEFEAT_ALL_ENEMIES ->
                enemyCharacters.none { it.isAlive }

            ChapterObjective.DEFEAT_BOSS ->
                bossUnit?.let { boss ->
                    enemyCharacters.none { it.id == boss.id && it.isAlive }
                } ?: false

            ChapterObjective.SEIZE_THRONE ->
                thronePosition?.let { throne ->
                    unitsOnThrone.any { it.team == Team.PLAYER && it.position == throne }
                } ?: false

            ChapterObjective.SURVIVE ->
                turnLimit?.let { limit ->
                    turnCount >= limit && playerCharacters.any { it.isAlive }
                } ?: false

            ChapterObjective.ESCAPE ->
                escapedUnits >= requiredEscapes

            ChapterObjective.DEFEND ->
                turnLimit?.let { limit ->
                    turnCount >= limit && playerCharacters.any { it.isAlive }
                } ?: false
        }

    /**
     * Check if chapter has failed
     */
    fun isObjectiveFailed(
        playerCharacters: List<Character>,
        turnCount: Int,
    ): Boolean {
        // All player units defeated = failure
        if (playerCharacters.none { it.isAlive }) {
            return true
        }

        // Time limit exceeded for timed objectives
        if (objective == ChapterObjective.DEFEND || objective == ChapterObjective.SURVIVE) {
            turnLimit?.let { limit ->
                if (turnCount > limit) {
                    return true
                }
            }
        }

        return false
    }

    /**
     * Get reinforcements that should spawn this turn
     */
    fun getReinforcementsForTurn(turnCount: Int): List<EnemyUnitSpawn> =
        reinforcements
            .filter { it.spawnTurn == turnCount }
            .map { it.unit }
}

/**
 * Defines enemy unit spawn configuration
 * Can reference a named unit from EnemyRepository or use generic stats
 */
@Parcelize
@Serializable
data class EnemyUnitSpawn(
    val id: String,
    val name: String,
    val characterClass: CharacterClass,
    val level: Int,
    val position: Position,
    val equipment: List<String>, // Weapon IDs
    val isBoss: Boolean = false,
    val aiType: AIBehavior = AIBehavior.AGGRESSIVE,
    val namedUnitId: String? = null, // Reference to NamedUnit for custom growth rates
) : Parcelable {
    /**
     * Create a character from this spawn definition
     * If namedUnitId is specified, uses that named unit's growth rates
     */
    fun toCharacter(team: Team = Team.ENEMY): Character {
        val namedUnit = namedUnitId?.let { EnemyRepository.getEnemy(it) }

        return if (namedUnit != null) {
            // Use named unit with custom growth rates
            Character.fromNamedUnit(
                namedUnit = namedUnit,
                team = team,
                position = position,
                targetLevel = level,
                isBoss = isBoss,
                aiType = aiType,
            )
        } else {
            // Create generic character with class default growth rates
            val targetLevel = this.level // Save the target level
            Character(
                id = id,
                name = name,
                characterClass = characterClass,
                team = team,
                position = position,
                level = 1,
                isBoss = isBoss,
                aiType = aiType,
            ).apply {
                // Level up to target level
                if (targetLevel > 1) {
                    val expNeeded = (targetLevel - 1) * 100 // EXPERIENCE_PER_LEVEL
                    gainExperience(expNeeded)
                }
            }
        }
    }
}

/**
 * Enemy AI behavior patterns
 */
@Serializable
enum class AIBehavior {
    AGGRESSIVE, // Always attacks nearest player unit
    DEFENSIVE, // Only attacks when in range
    STATIONARY, // Does not move unless attacked
    SUPPORT, // Prioritizes healing/buffing allies
}

/**
 * Reinforcement spawn configuration
 */
@Parcelize
@Serializable
data class Reinforcement(
    val spawnTurn: Int,
    val unit: EnemyUnitSpawn,
) : Parcelable

/**
 * Map layout configuration
 */
@Serializable
enum class MapLayout {
    TEST_MAP, // Current 12x8 test map
    PLAINS_BATTLE, // Open plains
    FOREST_AMBUSH, // Dense forest
    MOUNTAIN_PASS, // Narrow mountain path
    CASTLE_SIEGE, // Castle with throne
    VILLAGE_DEFENSE, // Defend village tiles
    RIVER_CROSSING, // Map split by river
    BORDER_FORT, // Fortified border outpost
    COASTAL_RUINS, // Ruins along the coast
    DARK_FOREST, // Dense dark forest with hidden paths
    FORTRESS_INTERIOR, // Interior of enemy fortress
    THRONE_ROOM, // Final boss throne room
}

/**
 * Chapter repository for campaign data
 */
object ChapterRepository {
    fun getChapter(chapterNumber: Int): Chapter? = chapters.getOrNull(chapterNumber - 1)

    fun getTotalChapters(): Int = chapters.size

    private val chapters =
        listOf(
            // Chapter 1: Tutorial
            Chapter(
                id = "ch1_tutorial",
                number = 1,
                title = "The First Battle",
                description = "Learn the basics of tactical combat",
                objective = ChapterObjective.DEFEAT_ALL_ENEMIES,
                objectiveDetails = "Defeat all enemy units",
                mapLayout = MapLayout.TEST_MAP,
                playerStartPositions =
                    listOf(
                        Position(1, 6),
                        Position(2, 7),
                        Position(0, 7),
                    ),
                enemyUnits =
                    listOf(
                        EnemyUnitSpawn(
                            id = "enemy_knight_1",
                            name = "Bandit",
                            characterClass = CharacterClass.KNIGHT,
                            level = 1,
                            position = Position(10, 1),
                            equipment = listOf("iron_sword"),
                            namedUnitId = "generic_bandit",
                        ),
                        EnemyUnitSpawn(
                            id = "enemy_archer_1",
                            name = "Brigand",
                            characterClass = CharacterClass.ARCHER,
                            level = 1,
                            position = Position(9, 2),
                            equipment = listOf("iron_bow"),
                            namedUnitId = "generic_brigand",
                        ),
                    ),
                preBattleDialogue =
                    "Commander: Welcome to the battlefield, recruits! " +
                        "Today you'll learn the basics of tactical combat.\n\n" +
                        "TAP a unit to select it, then use MOVE to see where they can go. " +
                        "ATTACK lets you strike enemies in range. " +
                        "Use WAIT when a unit is done.\n\n" +
                        "TIP: Use forests and forts for defensive bonuses. " +
                        "Keep your units together and watch enemy ranges!\n\n" +
                        "Commander: Now show me what you've got!",
                postVictoryDialogue =
                    "Commander: Well done! You've mastered the basics.\n" +
                        "Sir Garrett: The kingdom of Valdris needs warriors like you. " +
                        "Word has reached us that the Crimson Empire is mobilizing at our borders.\n" +
                        "Commander: Rest up. Tomorrow, we march.",
            ),
            // Chapter 2: First Challenge
            Chapter(
                id = "ch2_challenge",
                number = 2,
                title = "Forest Skirmish",
                description = "A bandit ambush in the forest",
                objective = ChapterObjective.DEFEAT_BOSS,
                objectiveDetails = "Defeat the Bandit Leader",
                mapLayout = MapLayout.FOREST_AMBUSH,
                playerStartPositions =
                    listOf(
                        Position(1, 6),
                        Position(2, 7),
                        Position(0, 7),
                    ),
                enemyUnits =
                    listOf(
                        EnemyUnitSpawn(
                            id = "enemy_thief_1",
                            name = "Rogue",
                            characterClass = CharacterClass.THIEF,
                            level = 2,
                            position = Position(11, 0),
                            equipment = listOf("iron_sword"),
                            namedUnitId = "generic_rogue",
                        ),
                        EnemyUnitSpawn(
                            id = "enemy_archer_2",
                            name = "Brigand Archer",
                            characterClass = CharacterClass.ARCHER,
                            level = 2,
                            position = Position(9, 2),
                            equipment = listOf("iron_bow"),
                            namedUnitId = "generic_brigand",
                        ),
                    ),
                bossUnit =
                    EnemyUnitSpawn(
                        id = "boss_dark_knight",
                        name = "Bandit Leader",
                        characterClass = CharacterClass.KNIGHT,
                        level = 3,
                        position = Position(10, 1),
                        equipment = listOf("steel_sword"),
                        isBoss = true,
                        aiType = AIBehavior.AGGRESSIVE,
                        namedUnitId = "boss_bandit_leader",
                    ),
                preBattleDialogue = "Scout: Bandits ahead! Their leader looks tough.",
                postVictoryDialogue = "Commander: The roads are safer now. Good work!",
            ),
            // Chapter 3: Mountain Defense
            Chapter(
                id = "ch3_mountain_defense",
                number = 3,
                title = "Mountain Outpost",
                description = "Defend the mountain pass from enemy reinforcements",
                objective = ChapterObjective.SURVIVE,
                objectiveDetails = "Survive for 10 turns",
                mapLayout = MapLayout.MOUNTAIN_PASS,
                turnLimit = 10,
                playerStartPositions =
                    listOf(
                        Position(1, 6),
                        Position(2, 7),
                        Position(0, 7),
                    ),
                enemyUnits =
                    listOf(
                        EnemyUnitSpawn(
                            id = "enemy_knight_2",
                            name = "Mountain Raider",
                            characterClass = CharacterClass.KNIGHT,
                            level = 3,
                            position = Position(10, 1),
                            equipment = listOf("iron_sword"),
                            aiType = AIBehavior.DEFENSIVE,
                        ),
                        EnemyUnitSpawn(
                            id = "enemy_archer_3",
                            name = "Highland Archer",
                            characterClass = CharacterClass.ARCHER,
                            level = 3,
                            position = Position(9, 2),
                            equipment = listOf("iron_bow"),
                            aiType = AIBehavior.DEFENSIVE,
                        ),
                        EnemyUnitSpawn(
                            id = "enemy_mage_1",
                            name = "Dark Mage",
                            characterClass = CharacterClass.MAGE,
                            level = 3,
                            position = Position(11, 0),
                            equipment = listOf("fire_tome"),
                            aiType = AIBehavior.AGGRESSIVE,
                        ),
                    ),
                reinforcements =
                    listOf(
                        Reinforcement(
                            spawnTurn = 5,
                            unit =
                                EnemyUnitSpawn(
                                    id = "reinforce_thief_1",
                                    name = "Assassin",
                                    characterClass = CharacterClass.THIEF,
                                    level = 4,
                                    position = Position(11, 7),
                                    equipment = listOf("iron_sword"),
                                    aiType = AIBehavior.AGGRESSIVE,
                                ),
                        ),
                        Reinforcement(
                            spawnTurn = 7,
                            unit =
                                EnemyUnitSpawn(
                                    id = "reinforce_knight_1",
                                    name = "Elite Knight",
                                    characterClass = CharacterClass.KNIGHT,
                                    level = 4,
                                    position = Position(11, 0),
                                    equipment = listOf("steel_sword"),
                                    aiType = AIBehavior.AGGRESSIVE,
                                ),
                        ),
                    ),
                preBattleDialogue = "Commander: Hold this position! Reinforcements are coming, but we must survive!",
                postVictoryDialogue = "Commander: We held them off! Well fought!",
                postDefeatDialogue = "The outpost has fallen...",
            ),
            // Chapter 4: Castle Assault
            Chapter(
                id = "ch4_castle_assault",
                number = 4,
                title = "Siege of Castle Ironhold",
                description = "Seize the throne room of the enemy castle",
                objective = ChapterObjective.SEIZE_THRONE,
                objectiveDetails = "Move a unit to the throne",
                mapLayout = MapLayout.CASTLE_SIEGE,
                thronePosition = Position(11, 0),
                playerStartPositions =
                    listOf(
                        Position(0, 6),
                        Position(0, 7),
                        Position(1, 7),
                    ),
                enemyUnits =
                    listOf(
                        EnemyUnitSpawn(
                            id = "enemy_knight_3",
                            name = "Castle Guard",
                            characterClass = CharacterClass.KNIGHT,
                            level = 4,
                            position = Position(8, 1),
                            equipment = listOf("steel_sword"),
                            aiType = AIBehavior.DEFENSIVE,
                        ),
                        EnemyUnitSpawn(
                            id = "enemy_knight_4",
                            name = "Royal Guard",
                            characterClass = CharacterClass.KNIGHT,
                            level = 4,
                            position = Position(10, 0),
                            equipment = listOf("steel_sword"),
                            aiType = AIBehavior.DEFENSIVE,
                        ),
                        EnemyUnitSpawn(
                            id = "enemy_archer_4",
                            name = "Tower Archer",
                            characterClass = CharacterClass.ARCHER,
                            level = 4,
                            position = Position(9, 2),
                            equipment = listOf("steel_bow"),
                            aiType = AIBehavior.STATIONARY,
                        ),
                        EnemyUnitSpawn(
                            id = "enemy_mage_2",
                            name = "Court Mage",
                            characterClass = CharacterClass.MAGE,
                            level = 4,
                            position = Position(7, 3),
                            equipment = listOf("thunder_tome"),
                            aiType = AIBehavior.DEFENSIVE,
                        ),
                    ),
                bossUnit =
                    EnemyUnitSpawn(
                        id = "boss_general",
                        name = "General Ironhold",
                        characterClass = CharacterClass.KNIGHT,
                        level = 5,
                        position = Position(11, 0),
                        equipment = listOf("silver_sword"),
                        isBoss = true,
                        aiType = AIBehavior.DEFENSIVE,
                        namedUnitId = "boss_general",
                    ),
                preBattleDialogue = "Commander: The throne room lies ahead. Breach the defenses and claim victory!",
                postVictoryDialogue = "Commander: Castle Ironhold is ours! The enemy's stronghold has fallen!",
            ),
            // Chapter 5: Village Escape
            Chapter(
                id = "ch5_village_escape",
                number = 5,
                title = "Flight from the Village",
                description = "Evacuate the village before enemy reinforcements arrive",
                objective = ChapterObjective.ESCAPE,
                objectiveDetails = "Get 3 units to the escape point",
                mapLayout = MapLayout.VILLAGE_DEFENSE,
                requiredEscapes = 3,
                escapePositions =
                    listOf(
                        Position(0, 0),
                        Position(0, 1),
                        Position(0, 2),
                    ),
                playerStartPositions =
                    listOf(
                        Position(5, 6),
                        Position(5, 7),
                        Position(6, 7),
                    ),
                enemyUnits =
                    listOf(
                        EnemyUnitSpawn(
                            id = "enemy_knight_5",
                            name = "Pursuer",
                            characterClass = CharacterClass.KNIGHT,
                            level = 5,
                            position = Position(10, 1),
                            equipment = listOf("steel_sword"),
                            aiType = AIBehavior.AGGRESSIVE,
                        ),
                        EnemyUnitSpawn(
                            id = "enemy_thief_2",
                            name = "Swift Bandit",
                            characterClass = CharacterClass.THIEF,
                            level = 5,
                            position = Position(11, 2),
                            equipment = listOf("iron_sword"),
                            aiType = AIBehavior.AGGRESSIVE,
                        ),
                        EnemyUnitSpawn(
                            id = "enemy_archer_5",
                            name = "Hunter",
                            characterClass = CharacterClass.ARCHER,
                            level = 5,
                            position = Position(9, 3),
                            equipment = listOf("steel_bow"),
                            aiType = AIBehavior.AGGRESSIVE,
                        ),
                    ),
                reinforcements =
                    listOf(
                        Reinforcement(
                            spawnTurn = 3,
                            unit =
                                EnemyUnitSpawn(
                                    id = "reinforce_knight_2",
                                    name = "Cavalry",
                                    characterClass = CharacterClass.KNIGHT,
                                    level = 5,
                                    position = Position(11, 7),
                                    equipment = listOf("steel_sword"),
                                    aiType = AIBehavior.AGGRESSIVE,
                                ),
                        ),
                        Reinforcement(
                            spawnTurn = 5,
                            unit =
                                EnemyUnitSpawn(
                                    id = "reinforce_mage_1",
                                    name = "Dark Sorcerer",
                                    characterClass = CharacterClass.MAGE,
                                    level = 5,
                                    position = Position(11, 0),
                                    equipment = listOf("fire_tome"),
                                    aiType = AIBehavior.AGGRESSIVE,
                                ),
                        ),
                    ),
                preBattleDialogue = "Commander: The village is surrounded! We must reach the escape route quickly!",
                postVictoryDialogue =
                    "Commander: We made it! The villagers are safe, and we live to fight another day.",
                postDefeatDialogue = "The escape route was blocked...",
            ),
            // Act 2: Counterattack
            // Chapter 6: The Crossing
            Chapter(
                id = "ch6_river_crossing",
                number = 6,
                title = "The River Crossing",
                description = "Cross the Silverflow River to begin the counterattack against the Crimson Empire",
                objective = ChapterObjective.DEFEAT_ALL_ENEMIES,
                objectiveDetails = "Clear the enemy garrison at the river crossing",
                mapLayout = MapLayout.RIVER_CROSSING,
                playerStartPositions =
                    listOf(
                        Position(1, 3),
                        Position(1, 4),
                        Position(2, 3),
                        Position(2, 4),
                    ),
                enemyUnits =
                    listOf(
                        EnemyUnitSpawn(
                            id = "enemy_knight_6",
                            name = "River Guard",
                            characterClass = CharacterClass.KNIGHT,
                            level = 5,
                            position = Position(8, 3),
                            equipment = listOf("steel_sword"),
                            aiType = AIBehavior.DEFENSIVE,
                        ),
                        EnemyUnitSpawn(
                            id = "enemy_knight_7",
                            name = "Bridge Sentinel",
                            characterClass = CharacterClass.KNIGHT,
                            level = 5,
                            position = Position(8, 7),
                            equipment = listOf("steel_sword"),
                            aiType = AIBehavior.DEFENSIVE,
                        ),
                        EnemyUnitSpawn(
                            id = "enemy_archer_6",
                            name = "Lookout",
                            characterClass = CharacterClass.ARCHER,
                            level = 5,
                            position = Position(10, 5),
                            equipment = listOf("steel_bow"),
                            aiType = AIBehavior.STATIONARY,
                        ),
                        EnemyUnitSpawn(
                            id = "enemy_healer_1",
                            name = "Field Medic",
                            characterClass = CharacterClass.HEALER,
                            level = 4,
                            position = Position(11, 5),
                            equipment = listOf("heal"),
                            aiType = AIBehavior.SUPPORT,
                            namedUnitId = "generic_healer",
                        ),
                    ),
                preBattleDialogue =
                    "Commander: The Silverflow River lies ahead. " +
                        "We must seize the bridges to advance into enemy territory. " +
                        "Elara, your healing will be vital. Stay close to the front lines.",
                postVictoryDialogue =
                    "Commander: The crossing is ours! The counterattack begins. " +
                        "The Crimson Empire will learn that Valdris will not fall quietly.",
            ),
            // Chapter 7: Village Liberation
            Chapter(
                id = "ch7_village_liberation",
                number = 7,
                title = "Liberation of Millhaven",
                description = "Free the occupied village of Millhaven from Crimson Empire forces",
                objective = ChapterObjective.DEFEAT_BOSS,
                objectiveDetails = "Defeat Captain Voss",
                mapLayout = MapLayout.VILLAGE_DEFENSE,
                playerStartPositions =
                    listOf(
                        Position(0, 4),
                        Position(0, 5),
                        Position(1, 4),
                        Position(1, 5),
                    ),
                enemyUnits =
                    listOf(
                        EnemyUnitSpawn(
                            id = "enemy_knight_8",
                            name = "Occupation Soldier",
                            characterClass = CharacterClass.KNIGHT,
                            level = 6,
                            position = Position(7, 3),
                            equipment = listOf("steel_sword"),
                            aiType = AIBehavior.AGGRESSIVE,
                        ),
                        EnemyUnitSpawn(
                            id = "enemy_archer_7",
                            name = "Crimson Bowman",
                            characterClass = CharacterClass.ARCHER,
                            level = 6,
                            position = Position(9, 7),
                            equipment = listOf("steel_bow"),
                            aiType = AIBehavior.DEFENSIVE,
                        ),
                        EnemyUnitSpawn(
                            id = "enemy_mage_3",
                            name = "Crimson Mage",
                            characterClass = CharacterClass.MAGE,
                            level = 6,
                            position = Position(11, 5),
                            equipment = listOf("thunder_tome"),
                            aiType = AIBehavior.AGGRESSIVE,
                            namedUnitId = "generic_dark_mage",
                        ),
                        EnemyUnitSpawn(
                            id = "enemy_healer_2",
                            name = "War Priest",
                            characterClass = CharacterClass.HEALER,
                            level = 5,
                            position = Position(12, 3),
                            equipment = listOf("heal"),
                            aiType = AIBehavior.SUPPORT,
                            namedUnitId = "generic_healer",
                        ),
                    ),
                bossUnit =
                    EnemyUnitSpawn(
                        id = "boss_voss",
                        name = "Captain Voss",
                        characterClass = CharacterClass.KNIGHT,
                        level = 7,
                        position = Position(13, 5),
                        equipment = listOf("silver_sword"),
                        isBoss = true,
                        aiType = AIBehavior.DEFENSIVE,
                        namedUnitId = "boss_captain_voss",
                    ),
                preBattleDialogue =
                    "Scout: Millhaven is occupied by Captain Voss and his garrison. " +
                        "The villagers are trapped.\n" +
                        "Commander: We free them today. Raven, use your speed to flank. " +
                        "Everyone else, push forward together!",
                postVictoryDialogue =
                    "Villager: Thank you! Captain Voss terrorized us for weeks.\n" +
                        "Commander: Millhaven is free. Rest well — more battles lie ahead.",
            ),
            // Chapter 8: Forest Ambush (player sets the ambush this time)
            Chapter(
                id = "ch8_dark_forest",
                number = 8,
                title = "The Dark Forest",
                description = "Ambush an enemy supply convoy passing through the Thornwood",
                objective = ChapterObjective.DEFEAT_ALL_ENEMIES,
                objectiveDetails = "Eliminate the enemy convoy escort",
                mapLayout = MapLayout.DARK_FOREST,
                playerStartPositions =
                    listOf(
                        Position(2, 3),
                        Position(2, 4),
                        Position(2, 5),
                        Position(2, 6),
                    ),
                enemyUnits =
                    listOf(
                        EnemyUnitSpawn(
                            id = "enemy_knight_9",
                            name = "Convoy Guard",
                            characterClass = CharacterClass.KNIGHT,
                            level = 7,
                            position = Position(8, 4),
                            equipment = listOf("steel_sword"),
                            aiType = AIBehavior.AGGRESSIVE,
                        ),
                        EnemyUnitSpawn(
                            id = "enemy_knight_10",
                            name = "Convoy Guard",
                            characterClass = CharacterClass.KNIGHT,
                            level = 7,
                            position = Position(8, 6),
                            equipment = listOf("steel_sword"),
                            aiType = AIBehavior.AGGRESSIVE,
                        ),
                        EnemyUnitSpawn(
                            id = "enemy_archer_8",
                            name = "Escort Archer",
                            characterClass = CharacterClass.ARCHER,
                            level = 7,
                            position = Position(9, 5),
                            equipment = listOf("steel_bow"),
                            aiType = AIBehavior.DEFENSIVE,
                        ),
                        EnemyUnitSpawn(
                            id = "enemy_thief_3",
                            name = "Scout",
                            characterClass = CharacterClass.THIEF,
                            level = 7,
                            position = Position(10, 3),
                            equipment = listOf("iron_sword"),
                            aiType = AIBehavior.AGGRESSIVE,
                            namedUnitId = "generic_rogue",
                        ),
                        EnemyUnitSpawn(
                            id = "enemy_healer_3",
                            name = "Convoy Healer",
                            characterClass = CharacterClass.HEALER,
                            level = 6,
                            position = Position(9, 4),
                            equipment = listOf("mend"),
                            aiType = AIBehavior.SUPPORT,
                            namedUnitId = "generic_healer",
                        ),
                    ),
                reinforcements =
                    listOf(
                        Reinforcement(
                            spawnTurn = 4,
                            unit =
                                EnemyUnitSpawn(
                                    id = "reinforce_pegasus_1",
                                    name = "Sky Scout",
                                    characterClass = CharacterClass.PEGASUS_KNIGHT,
                                    level = 7,
                                    position = Position(11, 1),
                                    equipment = listOf("iron_lance"),
                                    aiType = AIBehavior.AGGRESSIVE,
                                    namedUnitId = "generic_pegasus",
                                ),
                        ),
                    ),
                preBattleDialogue =
                    "Raven: The convoy will pass through here at dusk. " +
                        "We have the element of surprise.\n" +
                        "Commander: Use the trees as cover. Strike fast and cut off their escape.",
                postVictoryDialogue =
                    "Raven: The supplies are ours. This will weaken their front lines.\n" +
                        "Commander: Good work. The Crimson Empire grows weaker with every victory.",
            ),
            // Chapter 9: Border Fortress
            Chapter(
                id = "ch9_border_fort",
                number = 9,
                title = "Assault on Fort Duskwall",
                description = "Storm the Crimson Empire's border fortress",
                objective = ChapterObjective.SEIZE_THRONE,
                objectiveDetails = "Seize the command post inside the fort",
                mapLayout = MapLayout.BORDER_FORT,
                thronePosition = Position(7, 5),
                playerStartPositions =
                    listOf(
                        Position(0, 3),
                        Position(0, 4),
                        Position(0, 5),
                        Position(0, 6),
                        Position(1, 4),
                    ),
                enemyUnits =
                    listOf(
                        EnemyUnitSpawn(
                            id = "enemy_knight_11",
                            name = "Fort Defender",
                            characterClass = CharacterClass.KNIGHT,
                            level = 8,
                            position = Position(6, 4),
                            equipment = listOf("steel_sword"),
                            aiType = AIBehavior.DEFENSIVE,
                        ),
                        EnemyUnitSpawn(
                            id = "enemy_knight_12",
                            name = "Fort Defender",
                            characterClass = CharacterClass.KNIGHT,
                            level = 8,
                            position = Position(7, 6),
                            equipment = listOf("steel_lance"),
                            aiType = AIBehavior.DEFENSIVE,
                        ),
                        EnemyUnitSpawn(
                            id = "enemy_archer_9",
                            name = "Wall Archer",
                            characterClass = CharacterClass.ARCHER,
                            level = 8,
                            position = Position(5, 3),
                            equipment = listOf("steel_bow"),
                            aiType = AIBehavior.STATIONARY,
                        ),
                        EnemyUnitSpawn(
                            id = "enemy_mage_4",
                            name = "Fort Mage",
                            characterClass = CharacterClass.MAGE,
                            level = 8,
                            position = Position(8, 4),
                            equipment = listOf("thunder_tome"),
                            aiType = AIBehavior.DEFENSIVE,
                            namedUnitId = "generic_dark_mage",
                        ),
                        EnemyUnitSpawn(
                            id = "enemy_healer_4",
                            name = "Fort Healer",
                            characterClass = CharacterClass.HEALER,
                            level = 7,
                            position = Position(7, 4),
                            equipment = listOf("mend"),
                            aiType = AIBehavior.SUPPORT,
                            namedUnitId = "generic_healer",
                        ),
                    ),
                reinforcements =
                    listOf(
                        Reinforcement(
                            spawnTurn = 3,
                            unit =
                                EnemyUnitSpawn(
                                    id = "reinforce_knight_3",
                                    name = "Garrison Reinforcement",
                                    characterClass = CharacterClass.KNIGHT,
                                    level = 7,
                                    position = Position(13, 5),
                                    equipment = listOf("steel_sword"),
                                    aiType = AIBehavior.AGGRESSIVE,
                                ),
                        ),
                        Reinforcement(
                            spawnTurn = 5,
                            unit =
                                EnemyUnitSpawn(
                                    id = "reinforce_wyvern_1",
                                    name = "Wyvern Patrol",
                                    characterClass = CharacterClass.WYVERN_RIDER,
                                    level = 8,
                                    position = Position(13, 0),
                                    equipment = listOf("steel_axe"),
                                    aiType = AIBehavior.AGGRESSIVE,
                                    namedUnitId = "generic_wyvern",
                                ),
                        ),
                    ),
                preBattleDialogue =
                    "Celeste: Fort Duskwall blocks the only road into Crimson territory. " +
                        "We must take it.\n" +
                        "Commander: Watch for archers on the walls. " +
                        "Celeste, use your wings to bypass the defenses. " +
                        "Everyone else, breach the front gate!",
                postVictoryDialogue =
                    "Commander: Fort Duskwall is ours! The road into the Crimson Empire lies open.\n" +
                        "Aldric: I sense dark magic ahead. The real battles are just beginning.",
            ),
            // Chapter 10: The Sorceress
            Chapter(
                id = "ch10_sorceress",
                number = 10,
                title = "The Crimson Sorceress",
                description = "Confront Sorceress Mira in the Coastal Ruins",
                objective = ChapterObjective.DEFEAT_BOSS,
                objectiveDetails = "Defeat Sorceress Mira",
                mapLayout = MapLayout.COASTAL_RUINS,
                playerStartPositions =
                    listOf(
                        Position(0, 3),
                        Position(0, 4),
                        Position(1, 3),
                        Position(1, 4),
                        Position(1, 5),
                    ),
                enemyUnits =
                    listOf(
                        EnemyUnitSpawn(
                            id = "enemy_mage_5",
                            name = "Crimson Acolyte",
                            characterClass = CharacterClass.MAGE,
                            level = 9,
                            position = Position(5, 3),
                            equipment = listOf("fire_tome"),
                            aiType = AIBehavior.AGGRESSIVE,
                            namedUnitId = "generic_dark_mage",
                        ),
                        EnemyUnitSpawn(
                            id = "enemy_mage_6",
                            name = "Crimson Acolyte",
                            characterClass = CharacterClass.MAGE,
                            level = 9,
                            position = Position(7, 5),
                            equipment = listOf("thunder_tome"),
                            aiType = AIBehavior.AGGRESSIVE,
                            namedUnitId = "generic_dark_mage",
                        ),
                        EnemyUnitSpawn(
                            id = "enemy_knight_13",
                            name = "Crimson Guard",
                            characterClass = CharacterClass.KNIGHT,
                            level = 9,
                            position = Position(8, 3),
                            equipment = listOf("steel_sword"),
                            aiType = AIBehavior.DEFENSIVE,
                        ),
                        EnemyUnitSpawn(
                            id = "enemy_pegasus_1",
                            name = "Sky Lancer",
                            characterClass = CharacterClass.PEGASUS_KNIGHT,
                            level = 8,
                            position = Position(11, 1),
                            equipment = listOf("steel_lance"),
                            aiType = AIBehavior.AGGRESSIVE,
                            namedUnitId = "generic_pegasus",
                        ),
                        EnemyUnitSpawn(
                            id = "enemy_healer_5",
                            name = "Dark Priest",
                            characterClass = CharacterClass.HEALER,
                            level = 8,
                            position = Position(10, 5),
                            equipment = listOf("mend"),
                            aiType = AIBehavior.SUPPORT,
                            namedUnitId = "generic_healer",
                        ),
                    ),
                bossUnit =
                    EnemyUnitSpawn(
                        id = "boss_mira",
                        name = "Sorceress Mira",
                        characterClass = CharacterClass.MAGE,
                        level = 10,
                        position = Position(12, 4),
                        equipment = listOf("thunder_tome"),
                        isBoss = true,
                        aiType = AIBehavior.DEFENSIVE,
                        namedUnitId = "boss_sorceress_mira",
                    ),
                reinforcements =
                    listOf(
                        Reinforcement(
                            spawnTurn = 3,
                            unit =
                                EnemyUnitSpawn(
                                    id = "reinforce_mage_2",
                                    name = "Summoned Acolyte",
                                    characterClass = CharacterClass.MAGE,
                                    level = 8,
                                    position = Position(13, 7),
                                    equipment = listOf("fire_tome"),
                                    aiType = AIBehavior.AGGRESSIVE,
                                    namedUnitId = "generic_dark_mage",
                                ),
                        ),
                        Reinforcement(
                            spawnTurn = 6,
                            unit =
                                EnemyUnitSpawn(
                                    id = "reinforce_mage_3",
                                    name = "Summoned Acolyte",
                                    characterClass = CharacterClass.MAGE,
                                    level = 9,
                                    position = Position(13, 1),
                                    equipment = listOf("thunder_tome"),
                                    aiType = AIBehavior.AGGRESSIVE,
                                    namedUnitId = "generic_dark_mage",
                                ),
                        ),
                    ),
                preBattleDialogue =
                    "Aldric: I can feel her magic from here. Sorceress Mira is powerful.\n" +
                        "Commander: She's the one enchanting the Crimson Empire's weapons. " +
                        "Without her, their army loses its magical advantage.\n" +
                        "Lyanna: Then let's make sure she falls today.",
                postVictoryDialogue =
                    "Mira: You... are stronger than I expected. " +
                        "But the Emperor's power is far beyond mine...\n" +
                        "Commander: The Crimson Empire's magic has been broken. " +
                        "Now we march on the heart of the empire itself.",
            ),
        )
}
