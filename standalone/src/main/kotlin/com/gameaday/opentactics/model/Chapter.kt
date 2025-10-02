@file:Suppress("MagicNumber")

package com.gameaday.opentactics.model


/**
 * Chapter objective types that determine victory conditions
 */
enum class ChapterObjective {
    DEFEAT_ALL_ENEMIES,    // Kill all enemy units
    DEFEAT_BOSS,           // Kill the boss unit
    SEIZE_THRONE,          // Move a unit to the throne tile
    SURVIVE,               // Survive for N turns
    ESCAPE,                // Get N units to escape tiles
    DEFEND,                // Defend an objective for N turns
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
)  {
    
    /**
     * Check if chapter objective is complete
     */
    fun isObjectiveComplete(
        playerCharacters: List<Character>,
        enemyCharacters: List<Character>,
        turnCount: Int,
        unitsOnThrone: List<Character>,
        escapedUnits: Int,
    ): Boolean {
        return when (objective) {
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
    fun getReinforcementsForTurn(turnCount: Int): List<EnemyUnitSpawn> {
        return reinforcements
            .filter { it.spawnTurn == turnCount }
            .map { it.unit }
    }
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
    AGGRESSIVE,  // Always attacks nearest player unit
    DEFENSIVE,   // Only attacks when in range
    STATIONARY,  // Does not move unless attacked
    SUPPORT,     // Prioritizes healing/buffing allies
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
    TEST_MAP,       // Current 12x8 test map
    PLAINS_BATTLE,  // Open plains
    FOREST_AMBUSH,  // Dense forest
    MOUNTAIN_PASS,  // Narrow mountain path
    CASTLE_SIEGE,   // Castle with throne
    VILLAGE_DEFENSE, // Defend village tiles
    RIVER_CROSSING, // Map split by river
}

/**
 * Chapter repository for campaign data
 */
object ChapterRepository {
    
    fun getChapter(chapterNumber: Int): Chapter? {
        return chapters.getOrNull(chapterNumber - 1)
    }
    
    fun getTotalChapters(): Int = chapters.size
    
    private val chapters = listOf(
        // Chapter 1: Tutorial
        Chapter(
            id = "ch1_tutorial",
            number = 1,
            title = "The First Battle",
            description = "Learn the basics of tactical combat",
            objective = ChapterObjective.DEFEAT_ALL_ENEMIES,
            objectiveDetails = "Defeat all enemy units",
            mapLayout = MapLayout.TEST_MAP,
            playerStartPositions = listOf(
                Position(1, 6),
                Position(2, 7),
                Position(0, 7),
            ),
            enemyUnits = listOf(
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
            playerStartPositions = listOf(
                Position(1, 6),
                Position(2, 7),
                Position(0, 7),
            ),
            enemyUnits = listOf(
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
            bossUnit = EnemyUnitSpawn(
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
    )
}
