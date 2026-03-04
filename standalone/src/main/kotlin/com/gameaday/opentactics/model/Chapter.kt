@file:Suppress("MagicNumber")

package com.gameaday.opentactics.model

/**
 * Chapter objective types that determine victory conditions
 */
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
) {
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
 */
data class EnemyUnitSpawn(
    val id: String,
    val name: String,
    val characterClass: CharacterClass,
    val level: Int,
    val position: Position,
    val equipment: List<String>, // Weapon IDs
    val isBoss: Boolean = false,
    val aiType: AIBehavior = AIBehavior.AGGRESSIVE,
)

/**
 * Enemy AI behavior patterns
 */
enum class AIBehavior {
    AGGRESSIVE, // Always attacks nearest player unit
    DEFENSIVE, // Only attacks when in range
    STATIONARY, // Does not move unless attacked
    SUPPORT, // Prioritizes healing/buffing allies
}

/**
 * Reinforcement spawn configuration
 */
data class Reinforcement(
    val spawnTurn: Int,
    val unit: EnemyUnitSpawn,
)

/**
 * Map layout configuration
 */
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
    DESERT_OUTPOST, // Arid outpost with sand terrain
    FROZEN_PASS, // Icy mountain passage
    CRIMSON_CAPITAL, // Streets of the enemy capital
    IMPERIAL_PALACE, // Grand palace interior
    DRAGON_LAIR, // Volcanic cavern for final battle
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
                        ),
                        EnemyUnitSpawn(
                            id = "enemy_archer_1",
                            name = "Brigand",
                            characterClass = CharacterClass.ARCHER,
                            level = 1,
                            position = Position(9, 2),
                            equipment = listOf("iron_bow"),
                        ),
                    ),
                preBattleDialogue = "Commander: Welcome to the battlefield! Show me what you've learned.",
                postVictoryDialogue = "Commander: Well done! You're ready for real combat.",
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
                        ),
                        EnemyUnitSpawn(
                            id = "enemy_archer_2",
                            name = "Brigand Archer",
                            characterClass = CharacterClass.ARCHER,
                            level = 2,
                            position = Position(9, 2),
                            equipment = listOf("iron_bow"),
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
                        ),
                        EnemyUnitSpawn(
                            id = "enemy_healer_2",
                            name = "War Priest",
                            characterClass = CharacterClass.HEALER,
                            level = 5,
                            position = Position(12, 3),
                            equipment = listOf("heal"),
                            aiType = AIBehavior.SUPPORT,
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
                        ),
                        EnemyUnitSpawn(
                            id = "enemy_healer_3",
                            name = "Convoy Healer",
                            characterClass = CharacterClass.HEALER,
                            level = 6,
                            position = Position(9, 4),
                            equipment = listOf("mend"),
                            aiType = AIBehavior.SUPPORT,
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
                        ),
                        EnemyUnitSpawn(
                            id = "enemy_healer_4",
                            name = "Fort Healer",
                            characterClass = CharacterClass.HEALER,
                            level = 7,
                            position = Position(7, 4),
                            equipment = listOf("mend"),
                            aiType = AIBehavior.SUPPORT,
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
                        ),
                        EnemyUnitSpawn(
                            id = "enemy_mage_6",
                            name = "Crimson Acolyte",
                            characterClass = CharacterClass.MAGE,
                            level = 9,
                            position = Position(7, 5),
                            equipment = listOf("thunder_tome"),
                            aiType = AIBehavior.AGGRESSIVE,
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
                        ),
                        EnemyUnitSpawn(
                            id = "enemy_healer_5",
                            name = "Dark Priest",
                            characterClass = CharacterClass.HEALER,
                            level = 8,
                            position = Position(10, 5),
                            equipment = listOf("mend"),
                            aiType = AIBehavior.SUPPORT,
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
            // ============================================================
            // Act 3: Invasion (Chapters 11-15)
            // The army pushes deep into Crimson Empire territory
            // ============================================================
            // Chapter 11: Supply Lines
            Chapter(
                id = "ch11_supply_lines",
                number = 11,
                title = "Severed Supply Lines",
                description = "Destroy the Crimson Empire's desert supply depot to weaken their forces",
                objective = ChapterObjective.DEFEAT_ALL_ENEMIES,
                objectiveDetails = "Eliminate all enemies guarding the supply depot",
                mapLayout = MapLayout.DESERT_OUTPOST,
                playerStartPositions =
                    listOf(
                        Position(0, 3),
                        Position(0, 4),
                        Position(0, 5),
                        Position(1, 3),
                        Position(1, 4),
                        Position(1, 5),
                    ),
                enemyUnits =
                    listOf(
                        EnemyUnitSpawn(
                            id = "enemy_knight_14",
                            name = "Supply Guard",
                            characterClass = CharacterClass.KNIGHT,
                            level = 10,
                            position = Position(10, 3),
                            equipment = listOf("steel_sword"),
                            aiType = AIBehavior.DEFENSIVE,
                        ),
                        EnemyUnitSpawn(
                            id = "enemy_knight_15",
                            name = "Supply Guard",
                            characterClass = CharacterClass.KNIGHT,
                            level = 10,
                            position = Position(10, 6),
                            equipment = listOf("steel_sword"),
                            aiType = AIBehavior.DEFENSIVE,
                        ),
                        EnemyUnitSpawn(
                            id = "enemy_archer_10",
                            name = "Depot Sniper",
                            characterClass = CharacterClass.ARCHER,
                            level = 10,
                            position = Position(12, 4),
                            equipment = listOf("steel_bow"),
                            aiType = AIBehavior.STATIONARY,
                        ),
                        EnemyUnitSpawn(
                            id = "enemy_mage_7",
                            name = "Crimson Warlock",
                            characterClass = CharacterClass.MAGE,
                            level = 10,
                            position = Position(8, 5),
                            equipment = listOf("fire_tome"),
                            aiType = AIBehavior.AGGRESSIVE,
                        ),
                        EnemyUnitSpawn(
                            id = "enemy_healer_6",
                            name = "Depot Medic",
                            characterClass = CharacterClass.HEALER,
                            level = 9,
                            position = Position(11, 5),
                            equipment = listOf("mend"),
                            aiType = AIBehavior.SUPPORT,
                        ),
                        EnemyUnitSpawn(
                            id = "enemy_thief_3",
                            name = "Crimson Scout",
                            characterClass = CharacterClass.THIEF,
                            level = 10,
                            position = Position(7, 2),
                            equipment = listOf("iron_sword"),
                            aiType = AIBehavior.AGGRESSIVE,
                        ),
                    ),
                preBattleDialogue =
                    "Raven: Their supply depot is just ahead. Cut it off, and their front lines starve.\n" +
                        "Commander: Raven, you lead the flanking maneuver. " +
                        "Everyone else, hit them hard from the front.",
                postVictoryDialogue =
                    "Commander: Without supplies, the Crimson garrisons will crumble. " +
                        "Press the advantage!\n" +
                        "Lyanna: The mountain pass ahead leads to their inner territories.",
            ),
            // Chapter 12: Frozen Pass
            Chapter(
                id = "ch12_frozen_pass",
                number = 12,
                title = "The Frozen Pass",
                description = "Cross the treacherous frozen mountain pass into Crimson heartland",
                objective = ChapterObjective.SURVIVE,
                objectiveDetails = "Survive for 12 turns against relentless attacks",
                mapLayout = MapLayout.FROZEN_PASS,
                turnLimit = 12,
                playerStartPositions =
                    listOf(
                        Position(5, 3),
                        Position(5, 4),
                        Position(5, 5),
                        Position(6, 3),
                        Position(6, 4),
                        Position(6, 5),
                    ),
                enemyUnits =
                    listOf(
                        EnemyUnitSpawn(
                            id = "enemy_wyvern_1",
                            name = "Frost Wyvern",
                            characterClass = CharacterClass.WYVERN_RIDER,
                            level = 11,
                            position = Position(11, 2),
                            equipment = listOf("steel_axe"),
                            aiType = AIBehavior.AGGRESSIVE,
                        ),
                        EnemyUnitSpawn(
                            id = "enemy_wyvern_2",
                            name = "Frost Wyvern",
                            characterClass = CharacterClass.WYVERN_RIDER,
                            level = 11,
                            position = Position(11, 7),
                            equipment = listOf("steel_axe"),
                            aiType = AIBehavior.AGGRESSIVE,
                        ),
                        EnemyUnitSpawn(
                            id = "enemy_archer_11",
                            name = "Mountain Ranger",
                            characterClass = CharacterClass.ARCHER,
                            level = 11,
                            position = Position(9, 4),
                            equipment = listOf("steel_bow"),
                            aiType = AIBehavior.DEFENSIVE,
                        ),
                        EnemyUnitSpawn(
                            id = "enemy_mage_8",
                            name = "Ice Mage",
                            characterClass = CharacterClass.MAGE,
                            level = 11,
                            position = Position(0, 5),
                            equipment = listOf("thunder_tome"),
                            aiType = AIBehavior.AGGRESSIVE,
                        ),
                    ),
                reinforcements =
                    listOf(
                        Reinforcement(
                            spawnTurn = 3,
                            unit =
                                EnemyUnitSpawn(
                                    id = "reinforce_wyvern_3",
                                    name = "Frost Wyvern",
                                    characterClass = CharacterClass.WYVERN_RIDER,
                                    level = 10,
                                    position = Position(13, 5),
                                    equipment = listOf("iron_axe"),
                                    aiType = AIBehavior.AGGRESSIVE,
                                ),
                        ),
                        Reinforcement(
                            spawnTurn = 6,
                            unit =
                                EnemyUnitSpawn(
                                    id = "reinforce_knight_4",
                                    name = "Mountain Patrol",
                                    characterClass = CharacterClass.KNIGHT,
                                    level = 11,
                                    position = Position(0, 1),
                                    equipment = listOf("steel_sword"),
                                    aiType = AIBehavior.AGGRESSIVE,
                                ),
                        ),
                        Reinforcement(
                            spawnTurn = 9,
                            unit =
                                EnemyUnitSpawn(
                                    id = "reinforce_wyvern_4",
                                    name = "Frost Wyvern",
                                    characterClass = CharacterClass.WYVERN_RIDER,
                                    level = 11,
                                    position = Position(13, 1),
                                    equipment = listOf("steel_axe"),
                                    aiType = AIBehavior.AGGRESSIVE,
                                ),
                        ),
                    ),
                preBattleDialogue =
                    "Celeste: The wind is brutal up here. And those wyverns own the skies.\n" +
                        "Commander: We hold the pass. Twelve turns and we break through " +
                        "to the other side. Stay together!",
                postVictoryDialogue =
                    "Commander: We survived the pass! The Crimson heartland lies before us.\n" +
                        "Aldric: I can sense the Emperor's dark power growing stronger. We're close.",
                postDefeatDialogue =
                    "The frozen pass claimed too many lives. The army was forced to retreat.",
            ),
            // Chapter 13: Crimson Capital Outskirts
            Chapter(
                id = "ch13_capital_outskirts",
                number = 13,
                title = "Gates of the Capital",
                description = "Break through the outer defenses of the Crimson Capital",
                objective = ChapterObjective.SEIZE_THRONE,
                objectiveDetails = "Seize the gatehouse command post",
                mapLayout = MapLayout.CRIMSON_CAPITAL,
                thronePosition = Position(14, 5),
                playerStartPositions =
                    listOf(
                        Position(0, 3),
                        Position(0, 4),
                        Position(0, 5),
                        Position(0, 6),
                        Position(1, 4),
                        Position(1, 5),
                    ),
                enemyUnits =
                    listOf(
                        EnemyUnitSpawn(
                            id = "enemy_knight_16",
                            name = "Capital Guard",
                            characterClass = CharacterClass.KNIGHT,
                            level = 12,
                            position = Position(6, 4),
                            equipment = listOf("silver_sword"),
                            aiType = AIBehavior.DEFENSIVE,
                        ),
                        EnemyUnitSpawn(
                            id = "enemy_knight_17",
                            name = "Capital Guard",
                            characterClass = CharacterClass.KNIGHT,
                            level = 12,
                            position = Position(6, 6),
                            equipment = listOf("steel_lance"),
                            aiType = AIBehavior.DEFENSIVE,
                        ),
                        EnemyUnitSpawn(
                            id = "enemy_archer_12",
                            name = "Wall Archer",
                            characterClass = CharacterClass.ARCHER,
                            level = 12,
                            position = Position(10, 2),
                            equipment = listOf("steel_bow"),
                            aiType = AIBehavior.STATIONARY,
                        ),
                        EnemyUnitSpawn(
                            id = "enemy_archer_13",
                            name = "Wall Archer",
                            characterClass = CharacterClass.ARCHER,
                            level = 12,
                            position = Position(10, 7),
                            equipment = listOf("steel_bow"),
                            aiType = AIBehavior.STATIONARY,
                        ),
                        EnemyUnitSpawn(
                            id = "enemy_mage_9",
                            name = "Gate Warlock",
                            characterClass = CharacterClass.MAGE,
                            level = 12,
                            position = Position(12, 5),
                            equipment = listOf("thunder_tome"),
                            aiType = AIBehavior.DEFENSIVE,
                        ),
                        EnemyUnitSpawn(
                            id = "enemy_healer_7",
                            name = "Capital Priest",
                            characterClass = CharacterClass.HEALER,
                            level = 11,
                            position = Position(13, 5),
                            equipment = listOf("mend"),
                            aiType = AIBehavior.SUPPORT,
                        ),
                    ),
                reinforcements =
                    listOf(
                        Reinforcement(
                            spawnTurn = 4,
                            unit =
                                EnemyUnitSpawn(
                                    id = "reinforce_pegasus_3",
                                    name = "Sky Patrol",
                                    characterClass = CharacterClass.PEGASUS_KNIGHT,
                                    level = 11,
                                    position = Position(15, 1),
                                    equipment = listOf("steel_lance"),
                                    aiType = AIBehavior.AGGRESSIVE,
                                ),
                        ),
                    ),
                preBattleDialogue =
                    "Commander: The Crimson Capital. We've fought across an empire to reach this place.\n" +
                        "Sir Garrett: The gatehouse is heavily fortified. We need to seize the command post " +
                        "to open the inner gates.\n" +
                        "Elara: Stay close, everyone. I'll keep you healed.",
                postVictoryDialogue =
                    "Commander: The outer gates are breached! " +
                        "Into the capital — the Emperor's palace awaits.\n" +
                        "Raven: I've heard rumors of a secret weapon inside the palace. Be on guard.",
            ),
            // Chapter 14: Warlord's Last Stand
            Chapter(
                id = "ch14_warlord",
                number = 14,
                title = "Warlord's Last Stand",
                description = "Defeat Warlord Kael and his wyvern legion in the capital streets",
                objective = ChapterObjective.DEFEAT_BOSS,
                objectiveDetails = "Defeat Warlord Kael",
                mapLayout = MapLayout.CRIMSON_CAPITAL,
                playerStartPositions =
                    listOf(
                        Position(0, 3),
                        Position(0, 4),
                        Position(0, 5),
                        Position(0, 6),
                        Position(1, 4),
                        Position(1, 5),
                    ),
                enemyUnits =
                    listOf(
                        EnemyUnitSpawn(
                            id = "enemy_wyvern_3",
                            name = "Elite Wyvern",
                            characterClass = CharacterClass.WYVERN_RIDER,
                            level = 13,
                            position = Position(8, 2),
                            equipment = listOf("steel_axe"),
                            aiType = AIBehavior.AGGRESSIVE,
                        ),
                        EnemyUnitSpawn(
                            id = "enemy_wyvern_4",
                            name = "Elite Wyvern",
                            characterClass = CharacterClass.WYVERN_RIDER,
                            level = 13,
                            position = Position(8, 7),
                            equipment = listOf("steel_axe"),
                            aiType = AIBehavior.AGGRESSIVE,
                        ),
                        EnemyUnitSpawn(
                            id = "enemy_knight_18",
                            name = "Kael's Guard",
                            characterClass = CharacterClass.KNIGHT,
                            level = 13,
                            position = Position(10, 4),
                            equipment = listOf("silver_sword"),
                            aiType = AIBehavior.DEFENSIVE,
                        ),
                        EnemyUnitSpawn(
                            id = "enemy_knight_19",
                            name = "Kael's Guard",
                            characterClass = CharacterClass.KNIGHT,
                            level = 13,
                            position = Position(10, 6),
                            equipment = listOf("silver_sword"),
                            aiType = AIBehavior.DEFENSIVE,
                        ),
                        EnemyUnitSpawn(
                            id = "enemy_healer_8",
                            name = "Warlord's Priest",
                            characterClass = CharacterClass.HEALER,
                            level = 12,
                            position = Position(12, 5),
                            equipment = listOf("mend"),
                            aiType = AIBehavior.SUPPORT,
                        ),
                    ),
                bossUnit =
                    EnemyUnitSpawn(
                        id = "boss_kael",
                        name = "Warlord Kael",
                        characterClass = CharacterClass.WYVERN_RIDER,
                        level = 14,
                        position = Position(13, 5),
                        equipment = listOf("steel_axe"),
                        isBoss = true,
                        aiType = AIBehavior.AGGRESSIVE,
                    ),
                reinforcements =
                    listOf(
                        Reinforcement(
                            spawnTurn = 3,
                            unit =
                                EnemyUnitSpawn(
                                    id = "reinforce_wyvern_5",
                                    name = "Wyvern Reinforcement",
                                    characterClass = CharacterClass.WYVERN_RIDER,
                                    level = 12,
                                    position = Position(15, 1),
                                    equipment = listOf("iron_axe"),
                                    aiType = AIBehavior.AGGRESSIVE,
                                ),
                        ),
                        Reinforcement(
                            spawnTurn = 5,
                            unit =
                                EnemyUnitSpawn(
                                    id = "reinforce_wyvern_6",
                                    name = "Wyvern Reinforcement",
                                    characterClass = CharacterClass.WYVERN_RIDER,
                                    level = 12,
                                    position = Position(15, 8),
                                    equipment = listOf("steel_axe"),
                                    aiType = AIBehavior.AGGRESSIVE,
                                ),
                        ),
                    ),
                preBattleDialogue =
                    "Celeste: Wyverns above! That's Warlord Kael's personal squadron.\n" +
                        "Commander: Kael is the last barrier before the palace. " +
                        "Celeste, you know their flight patterns — guide us.\n" +
                        "Celeste: Leave the skies to me. Focus on Kael himself.",
                postVictoryDialogue =
                    "Kael: Impossible... my wyverns were undefeated...\n" +
                        "Commander: Your wyverns are grounded, Kael. " +
                        "Now there's nothing between us and the Emperor.\n" +
                        "Aldric: The palace doors lie ahead. This ends today.",
            ),
            // Chapter 15: Palace Infiltration
            Chapter(
                id = "ch15_palace",
                number = 15,
                title = "Palace Infiltration",
                description = "Infiltrate the Imperial Palace and reach the inner chambers",
                objective = ChapterObjective.ESCAPE,
                objectiveDetails = "Get 3 units to the inner chambers",
                mapLayout = MapLayout.IMPERIAL_PALACE,
                requiredEscapes = 3,
                escapePositions =
                    listOf(
                        Position(15, 5),
                        Position(15, 6),
                    ),
                playerStartPositions =
                    listOf(
                        Position(0, 4),
                        Position(0, 5),
                        Position(0, 6),
                        Position(1, 4),
                        Position(1, 5),
                        Position(1, 6),
                    ),
                enemyUnits =
                    listOf(
                        EnemyUnitSpawn(
                            id = "enemy_knight_20",
                            name = "Palace Guard",
                            characterClass = CharacterClass.KNIGHT,
                            level = 13,
                            position = Position(5, 3),
                            equipment = listOf("silver_sword"),
                            aiType = AIBehavior.DEFENSIVE,
                        ),
                        EnemyUnitSpawn(
                            id = "enemy_knight_21",
                            name = "Palace Guard",
                            characterClass = CharacterClass.KNIGHT,
                            level = 13,
                            position = Position(5, 7),
                            equipment = listOf("silver_sword"),
                            aiType = AIBehavior.DEFENSIVE,
                        ),
                        EnemyUnitSpawn(
                            id = "enemy_mage_10",
                            name = "Palace Warlock",
                            characterClass = CharacterClass.MAGE,
                            level = 13,
                            position = Position(8, 5),
                            equipment = listOf("thunder_tome"),
                            aiType = AIBehavior.AGGRESSIVE,
                        ),
                        EnemyUnitSpawn(
                            id = "enemy_thief_4",
                            name = "Palace Assassin",
                            characterClass = CharacterClass.THIEF,
                            level = 13,
                            position = Position(10, 3),
                            equipment = listOf("iron_sword"),
                            aiType = AIBehavior.AGGRESSIVE,
                        ),
                        EnemyUnitSpawn(
                            id = "enemy_archer_14",
                            name = "Royal Archer",
                            characterClass = CharacterClass.ARCHER,
                            level = 13,
                            position = Position(12, 6),
                            equipment = listOf("steel_bow"),
                            aiType = AIBehavior.STATIONARY,
                        ),
                        EnemyUnitSpawn(
                            id = "enemy_healer_9",
                            name = "Royal Priest",
                            characterClass = CharacterClass.HEALER,
                            level = 12,
                            position = Position(11, 5),
                            equipment = listOf("mend"),
                            aiType = AIBehavior.SUPPORT,
                        ),
                    ),
                reinforcements =
                    listOf(
                        Reinforcement(
                            spawnTurn = 4,
                            unit =
                                EnemyUnitSpawn(
                                    id = "reinforce_knight_5",
                                    name = "Palace Reinforcement",
                                    characterClass = CharacterClass.KNIGHT,
                                    level = 13,
                                    position = Position(0, 1),
                                    equipment = listOf("steel_sword"),
                                    aiType = AIBehavior.AGGRESSIVE,
                                ),
                        ),
                        Reinforcement(
                            spawnTurn = 7,
                            unit =
                                EnemyUnitSpawn(
                                    id = "reinforce_knight_6",
                                    name = "Palace Reinforcement",
                                    characterClass = CharacterClass.KNIGHT,
                                    level = 13,
                                    position = Position(0, 9),
                                    equipment = listOf("silver_sword"),
                                    aiType = AIBehavior.AGGRESSIVE,
                                ),
                        ),
                    ),
                preBattleDialogue =
                    "Raven: The palace is a maze of corridors. Follow me — " +
                        "I memorized the layout from stolen maps.\n" +
                        "Commander: We need at least three of us to reach the inner chambers. " +
                        "Move fast, watch for ambushes.\n" +
                        "Elara: I'll stay close to the advance team.",
                postVictoryDialogue =
                    "Commander: We're inside. The Emperor's throne room is through these doors.\n" +
                        "Aldric: The dark power is overwhelming here. " +
                        "The Emperor has been corrupted by something ancient.\n" +
                        "Sir Garrett: Then we end this corruption. For Valdris!",
                postDefeatDialogue =
                    "The palace guards overwhelmed the infiltration team...",
            ),
            // ============================================================
            // Finale (Chapters 16-20)
            // The final confrontation with Emperor Darius
            // ============================================================
            // Chapter 16: The Emperor's Guard
            Chapter(
                id = "ch16_emperors_guard",
                number = 16,
                title = "The Emperor's Guard",
                description = "Defeat the Emperor's elite guard in the throne room antechamber",
                objective = ChapterObjective.DEFEAT_ALL_ENEMIES,
                objectiveDetails = "Eliminate the Emperor's personal guard",
                mapLayout = MapLayout.THRONE_ROOM,
                playerStartPositions =
                    listOf(
                        Position(1, 4),
                        Position(1, 5),
                        Position(1, 6),
                        Position(2, 4),
                        Position(2, 5),
                        Position(2, 6),
                    ),
                enemyUnits =
                    listOf(
                        EnemyUnitSpawn(
                            id = "enemy_knight_22",
                            name = "Imperial Elite",
                            characterClass = CharacterClass.KNIGHT,
                            level = 14,
                            position = Position(8, 3),
                            equipment = listOf("silver_sword"),
                            aiType = AIBehavior.AGGRESSIVE,
                        ),
                        EnemyUnitSpawn(
                            id = "enemy_knight_23",
                            name = "Imperial Elite",
                            characterClass = CharacterClass.KNIGHT,
                            level = 14,
                            position = Position(8, 7),
                            equipment = listOf("silver_sword"),
                            aiType = AIBehavior.AGGRESSIVE,
                        ),
                        EnemyUnitSpawn(
                            id = "enemy_wyvern_5",
                            name = "Imperial Wyvern",
                            characterClass = CharacterClass.WYVERN_RIDER,
                            level = 14,
                            position = Position(11, 2),
                            equipment = listOf("steel_axe"),
                            aiType = AIBehavior.AGGRESSIVE,
                        ),
                        EnemyUnitSpawn(
                            id = "enemy_mage_11",
                            name = "Imperial Sorcerer",
                            characterClass = CharacterClass.MAGE,
                            level = 14,
                            position = Position(10, 5),
                            equipment = listOf("thunder_tome"),
                            aiType = AIBehavior.AGGRESSIVE,
                        ),
                        EnemyUnitSpawn(
                            id = "enemy_pegasus_2",
                            name = "Imperial Sky Knight",
                            characterClass = CharacterClass.PEGASUS_KNIGHT,
                            level = 14,
                            position = Position(11, 8),
                            equipment = listOf("steel_lance"),
                            aiType = AIBehavior.AGGRESSIVE,
                        ),
                        EnemyUnitSpawn(
                            id = "enemy_healer_10",
                            name = "Imperial Priest",
                            characterClass = CharacterClass.HEALER,
                            level = 13,
                            position = Position(12, 5),
                            equipment = listOf("mend"),
                            aiType = AIBehavior.SUPPORT,
                        ),
                    ),
                preBattleDialogue =
                    "Sir Garrett: The Emperor's personal guard. " +
                        "These are the finest warriors in the empire.\n" +
                        "Commander: We've come too far to turn back. " +
                        "Fight with everything you have!\n" +
                        "Lyanna: For every village they burned, for every life they took — we end this.",
                postVictoryDialogue =
                    "Commander: The guard has fallen. Only the Emperor remains.\n" +
                        "Aldric: Be cautious. His power has grown since we last sensed it. " +
                        "This is no ordinary foe.",
            ),
            // Chapter 17: Hall of Shadows
            Chapter(
                id = "ch17_hall_of_shadows",
                number = 17,
                title = "Hall of Shadows",
                description = "Navigate the cursed palace halls where shadows come alive",
                objective = ChapterObjective.SURVIVE,
                objectiveDetails = "Survive 10 turns against endless shadow soldiers",
                mapLayout = MapLayout.FORTRESS_INTERIOR,
                turnLimit = 10,
                playerStartPositions =
                    listOf(
                        Position(2, 5),
                        Position(2, 6),
                        Position(3, 5),
                        Position(3, 6),
                        Position(2, 7),
                        Position(3, 7),
                    ),
                enemyUnits =
                    listOf(
                        EnemyUnitSpawn(
                            id = "enemy_knight_24",
                            name = "Shadow Knight",
                            characterClass = CharacterClass.KNIGHT,
                            level = 14,
                            position = Position(8, 3),
                            equipment = listOf("silver_sword"),
                            aiType = AIBehavior.AGGRESSIVE,
                        ),
                        EnemyUnitSpawn(
                            id = "enemy_mage_12",
                            name = "Shadow Mage",
                            characterClass = CharacterClass.MAGE,
                            level = 14,
                            position = Position(10, 8),
                            equipment = listOf("thunder_tome"),
                            aiType = AIBehavior.AGGRESSIVE,
                        ),
                        EnemyUnitSpawn(
                            id = "enemy_thief_5",
                            name = "Shadow Assassin",
                            characterClass = CharacterClass.THIEF,
                            level = 14,
                            position = Position(6, 9),
                            equipment = listOf("iron_sword"),
                            aiType = AIBehavior.AGGRESSIVE,
                        ),
                    ),
                reinforcements =
                    listOf(
                        Reinforcement(
                            spawnTurn = 2,
                            unit =
                                EnemyUnitSpawn(
                                    id = "reinforce_shadow_1",
                                    name = "Shadow Warrior",
                                    characterClass = CharacterClass.KNIGHT,
                                    level = 13,
                                    position = Position(12, 2),
                                    equipment = listOf("steel_sword"),
                                    aiType = AIBehavior.AGGRESSIVE,
                                ),
                        ),
                        Reinforcement(
                            spawnTurn = 4,
                            unit =
                                EnemyUnitSpawn(
                                    id = "reinforce_shadow_2",
                                    name = "Shadow Mage",
                                    characterClass = CharacterClass.MAGE,
                                    level = 13,
                                    position = Position(1, 2),
                                    equipment = listOf("fire_tome"),
                                    aiType = AIBehavior.AGGRESSIVE,
                                ),
                        ),
                        Reinforcement(
                            spawnTurn = 6,
                            unit =
                                EnemyUnitSpawn(
                                    id = "reinforce_shadow_3",
                                    name = "Shadow Knight",
                                    characterClass = CharacterClass.KNIGHT,
                                    level = 14,
                                    position = Position(12, 9),
                                    equipment = listOf("silver_sword"),
                                    aiType = AIBehavior.AGGRESSIVE,
                                ),
                        ),
                        Reinforcement(
                            spawnTurn = 8,
                            unit =
                                EnemyUnitSpawn(
                                    id = "reinforce_shadow_4",
                                    name = "Shadow Assassin",
                                    characterClass = CharacterClass.THIEF,
                                    level = 14,
                                    position = Position(1, 9),
                                    equipment = listOf("iron_sword"),
                                    aiType = AIBehavior.AGGRESSIVE,
                                ),
                        ),
                    ),
                preBattleDialogue =
                    "Aldric: The Emperor's dark magic fills these halls. " +
                        "These shadows are manifestations of his power!\n" +
                        "Commander: They just keep coming! Hold the line — " +
                        "we just need to survive until the magic weakens.\n" +
                        "Elara: Stay close to each other. My healing will keep us standing.",
                postVictoryDialogue =
                    "Aldric: The shadow magic is fading. The Emperor is expending too much power.\n" +
                        "Commander: Now is our chance. To the throne room!",
                postDefeatDialogue =
                    "The shadows overwhelmed the army. Darkness consumed the halls.",
            ),
            // Chapter 18: The Throne Room
            Chapter(
                id = "ch18_throne_room",
                number = 18,
                title = "The Crimson Throne",
                description = "Seize the Crimson Throne and confront Emperor Darius",
                objective = ChapterObjective.SEIZE_THRONE,
                objectiveDetails = "Reach the Crimson Throne",
                mapLayout = MapLayout.THRONE_ROOM,
                thronePosition = Position(13, 5),
                playerStartPositions =
                    listOf(
                        Position(1, 4),
                        Position(1, 5),
                        Position(1, 6),
                        Position(2, 4),
                        Position(2, 5),
                        Position(2, 6),
                    ),
                enemyUnits =
                    listOf(
                        EnemyUnitSpawn(
                            id = "enemy_knight_25",
                            name = "Throne Guard",
                            characterClass = CharacterClass.KNIGHT,
                            level = 15,
                            position = Position(6, 3),
                            equipment = listOf("silver_sword"),
                            aiType = AIBehavior.DEFENSIVE,
                        ),
                        EnemyUnitSpawn(
                            id = "enemy_knight_26",
                            name = "Throne Guard",
                            characterClass = CharacterClass.KNIGHT,
                            level = 15,
                            position = Position(6, 7),
                            equipment = listOf("silver_sword"),
                            aiType = AIBehavior.DEFENSIVE,
                        ),
                        EnemyUnitSpawn(
                            id = "enemy_mage_13",
                            name = "Dark Enchanter",
                            characterClass = CharacterClass.MAGE,
                            level = 15,
                            position = Position(9, 5),
                            equipment = listOf("thunder_tome"),
                            aiType = AIBehavior.AGGRESSIVE,
                        ),
                        EnemyUnitSpawn(
                            id = "enemy_healer_11",
                            name = "Emperor's Priest",
                            characterClass = CharacterClass.HEALER,
                            level = 14,
                            position = Position(12, 6),
                            equipment = listOf("mend"),
                            aiType = AIBehavior.SUPPORT,
                        ),
                    ),
                bossUnit =
                    EnemyUnitSpawn(
                        id = "boss_darius_1",
                        name = "Emperor Darius",
                        characterClass = CharacterClass.KNIGHT,
                        level = 16,
                        position = Position(13, 5),
                        equipment = listOf("silver_sword"),
                        isBoss = true,
                        aiType = AIBehavior.DEFENSIVE,
                    ),
                preBattleDialogue =
                    "Darius: So, you've made it this far. Impressive, little commander.\n" +
                        "Commander: Your reign of terror ends today, Darius!\n" +
                        "Darius: You don't understand the power I command. " +
                        "I have transcended humanity itself!\n" +
                        "Sir Garrett: For Valdris! CHARGE!",
                postVictoryDialogue =
                    "Darius: No... this cannot be... But the darkness within me " +
                        "cannot be so easily destroyed!\n" +
                        "Aldric: He's transforming! The dark power is consuming him completely!\n" +
                        "Commander: Everyone, prepare yourselves. This isn't over yet.",
            ),
            // Chapter 19: Dark Awakening
            Chapter(
                id = "ch19_dark_awakening",
                number = 19,
                title = "Dark Awakening",
                description = "The Emperor unleashes his true form — defend against the darkness",
                objective = ChapterObjective.DEFEND,
                objectiveDetails = "Defend for 8 turns while Aldric prepares a sealing spell",
                mapLayout = MapLayout.DRAGON_LAIR,
                turnLimit = 8,
                playerStartPositions =
                    listOf(
                        Position(6, 4),
                        Position(6, 5),
                        Position(6, 6),
                        Position(7, 4),
                        Position(7, 5),
                        Position(7, 6),
                    ),
                enemyUnits =
                    listOf(
                        EnemyUnitSpawn(
                            id = "enemy_knight_27",
                            name = "Dark Soldier",
                            characterClass = CharacterClass.KNIGHT,
                            level = 16,
                            position = Position(12, 3),
                            equipment = listOf("silver_sword"),
                            aiType = AIBehavior.AGGRESSIVE,
                        ),
                        EnemyUnitSpawn(
                            id = "enemy_knight_28",
                            name = "Dark Soldier",
                            characterClass = CharacterClass.KNIGHT,
                            level = 16,
                            position = Position(12, 7),
                            equipment = listOf("silver_sword"),
                            aiType = AIBehavior.AGGRESSIVE,
                        ),
                        EnemyUnitSpawn(
                            id = "enemy_mage_14",
                            name = "Dark Sorcerer",
                            characterClass = CharacterClass.MAGE,
                            level = 16,
                            position = Position(14, 5),
                            equipment = listOf("thunder_tome"),
                            aiType = AIBehavior.AGGRESSIVE,
                        ),
                        EnemyUnitSpawn(
                            id = "enemy_wyvern_6",
                            name = "Dark Wyvern",
                            characterClass = CharacterClass.WYVERN_RIDER,
                            level = 16,
                            position = Position(14, 2),
                            equipment = listOf("steel_axe"),
                            aiType = AIBehavior.AGGRESSIVE,
                        ),
                    ),
                reinforcements =
                    listOf(
                        Reinforcement(
                            spawnTurn = 2,
                            unit =
                                EnemyUnitSpawn(
                                    id = "reinforce_dark_1",
                                    name = "Dark Knight",
                                    characterClass = CharacterClass.KNIGHT,
                                    level = 15,
                                    position = Position(15, 5),
                                    equipment = listOf("silver_sword"),
                                    aiType = AIBehavior.AGGRESSIVE,
                                ),
                        ),
                        Reinforcement(
                            spawnTurn = 4,
                            unit =
                                EnemyUnitSpawn(
                                    id = "reinforce_dark_2",
                                    name = "Dark Mage",
                                    characterClass = CharacterClass.MAGE,
                                    level = 15,
                                    position = Position(0, 5),
                                    equipment = listOf("fire_tome"),
                                    aiType = AIBehavior.AGGRESSIVE,
                                ),
                        ),
                        Reinforcement(
                            spawnTurn = 6,
                            unit =
                                EnemyUnitSpawn(
                                    id = "reinforce_dark_3",
                                    name = "Dark Wyvern",
                                    characterClass = CharacterClass.WYVERN_RIDER,
                                    level = 16,
                                    position = Position(15, 1),
                                    equipment = listOf("steel_axe"),
                                    aiType = AIBehavior.AGGRESSIVE,
                                ),
                        ),
                    ),
                preBattleDialogue =
                    "Aldric: The Emperor's dark power is too strong to destroy by force alone! " +
                        "I need time to prepare a sealing spell.\n" +
                        "Commander: How long?\n" +
                        "Aldric: Eight turns. Protect me while I channel the spell!\n" +
                        "Commander: You heard him — hold the line at all costs!",
                postVictoryDialogue =
                    "Aldric: The seal is ready! But it will only weaken his dark power, " +
                        "not destroy him. Someone must strike the final blow!\n" +
                        "Commander: Then we finish this. Together.",
                postDefeatDialogue =
                    "The dark forces overwhelmed the army before the sealing spell could be completed.",
            ),
            // Chapter 20: Dawn of Victory
            Chapter(
                id = "ch20_final_battle",
                number = 20,
                title = "Dawn of Victory",
                description = "The final battle against Emperor Darius — decide the fate of two kingdoms",
                objective = ChapterObjective.DEFEAT_BOSS,
                objectiveDetails = "Defeat Emperor Darius once and for all",
                mapLayout = MapLayout.DRAGON_LAIR,
                playerStartPositions =
                    listOf(
                        Position(1, 3),
                        Position(1, 4),
                        Position(1, 5),
                        Position(1, 6),
                        Position(2, 4),
                        Position(2, 5),
                    ),
                enemyUnits =
                    listOf(
                        EnemyUnitSpawn(
                            id = "enemy_knight_29",
                            name = "Imperial Champion",
                            characterClass = CharacterClass.KNIGHT,
                            level = 17,
                            position = Position(7, 3),
                            equipment = listOf("silver_sword"),
                            aiType = AIBehavior.AGGRESSIVE,
                        ),
                        EnemyUnitSpawn(
                            id = "enemy_knight_30",
                            name = "Imperial Champion",
                            characterClass = CharacterClass.KNIGHT,
                            level = 17,
                            position = Position(7, 7),
                            equipment = listOf("silver_sword"),
                            aiType = AIBehavior.AGGRESSIVE,
                        ),
                        EnemyUnitSpawn(
                            id = "enemy_mage_15",
                            name = "Imperial Archmage",
                            characterClass = CharacterClass.MAGE,
                            level = 17,
                            position = Position(10, 2),
                            equipment = listOf("thunder_tome"),
                            aiType = AIBehavior.AGGRESSIVE,
                        ),
                        EnemyUnitSpawn(
                            id = "enemy_mage_16",
                            name = "Imperial Archmage",
                            characterClass = CharacterClass.MAGE,
                            level = 17,
                            position = Position(10, 8),
                            equipment = listOf("fire_tome"),
                            aiType = AIBehavior.AGGRESSIVE,
                        ),
                        EnemyUnitSpawn(
                            id = "enemy_healer_12",
                            name = "Emperor's Last Priest",
                            characterClass = CharacterClass.HEALER,
                            level = 16,
                            position = Position(12, 5),
                            equipment = listOf("mend"),
                            aiType = AIBehavior.SUPPORT,
                        ),
                    ),
                bossUnit =
                    EnemyUnitSpawn(
                        id = "boss_darius_final",
                        name = "Emperor Darius",
                        characterClass = CharacterClass.KNIGHT,
                        level = 20,
                        position = Position(14, 5),
                        equipment = listOf("silver_sword"),
                        isBoss = true,
                        aiType = AIBehavior.AGGRESSIVE,
                    ),
                preBattleDialogue =
                    "Darius: You dare face me again?! " +
                        "Even weakened, my power eclipses yours!\n" +
                        "Commander: We've fought across an entire empire to reach you. " +
                        "Every soldier who fell, every village you burned — " +
                        "it all ends HERE.\n" +
                        "Sir Garrett: For Valdris!\n" +
                        "Lyanna: For the fallen!\n" +
                        "Aldric: For a world free of tyranny!\n" +
                        "All: CHARGE!",
                postVictoryDialogue =
                    "Darius: How... how can this be? I was chosen... by the darkness...\n" +
                        "Commander: The darkness chose wrong.\n\n" +
                        "Narrator: With the fall of Emperor Darius, the Crimson Empire crumbled. " +
                        "The armies laid down their weapons, and peace returned to the land.\n\n" +
                        "Sir Garrett returned home a hero, though he claimed he was " +
                        "merely doing his duty.\n" +
                        "Lyanna became the finest markswoman in all of Valdris, " +
                        "training the next generation of archers.\n" +
                        "Aldric dedicated himself to sealing away the dark magic, " +
                        "ensuring it would never threaten the world again.\n" +
                        "Elara established healing houses across the land, " +
                        "tending to those wounded by war.\n" +
                        "Raven... disappeared, as thieves do. " +
                        "But whenever injustice threatened, a shadow appeared to set things right.\n" +
                        "Celeste and her pegasus knights patrolled the skies, " +
                        "guardians of a hard-won peace.\n\n" +
                        "And the Commander? They sat beneath the old oak tree " +
                        "where it all began, finally at rest.\n\n" +
                        "THE END\n\n" +
                        "Thank you for playing Open Tactics!",
            ),
        )
}
