package com.gameaday.opentactics.game

import com.gameaday.opentactics.model.Character
import com.gameaday.opentactics.model.GameBoard
import com.gameaday.opentactics.model.Position
import com.gameaday.opentactics.model.Team

// Battle and experience constants
private const val EXPERIENCE_PER_KILL_MULTIPLIER = 25
private const val EXPERIENCE_PER_HIT = 10
private const val DAMAGE_VARIANCE = 0.25

class GameState(
    val board: GameBoard,
    private val playerCharacters: MutableList<Character> = mutableListOf(),
    private val enemyCharacters: MutableList<Character> = mutableListOf(),
) {
    var currentTurn: Team = Team.PLAYER
    var selectedCharacter: Character? = null
    var gamePhase: GamePhase = GamePhase.UNIT_SELECT
    var turnCount: Int = 1

    enum class GamePhase {
        UNIT_SELECT,
        MOVEMENT,
        ACTION,
        ENEMY_TURN,
        GAME_OVER,
    }

    fun getAllCharacters(): List<Character> = playerCharacters + enemyCharacters

    fun getPlayerCharacters(): List<Character> = playerCharacters.toList()

    fun getEnemyCharacters(): List<Character> = enemyCharacters.toList()

    fun addPlayerCharacter(character: Character) {
        playerCharacters.add(character)
    }

    fun addEnemyCharacter(character: Character) {
        enemyCharacters.add(character)
    }

    fun selectCharacter(character: Character?) {
        selectedCharacter = character
        gamePhase =
            if (character != null && character.team == currentTurn) {
                when {
                    character.canMove -> GamePhase.MOVEMENT
                    character.canAct -> GamePhase.ACTION
                    else -> GamePhase.UNIT_SELECT
                }
            } else {
                GamePhase.UNIT_SELECT
            }
    }

    fun canSelectCharacter(character: Character): Boolean =
        character.team == currentTurn && (character.canMove || character.canAct)

    fun endTurn() {
        when (currentTurn) {
            Team.PLAYER -> {
                // Reset all player units
                playerCharacters.forEach { it.resetTurn() }
                currentTurn = Team.ENEMY
                gamePhase = GamePhase.ENEMY_TURN
                executeEnemyTurn()
            }
            Team.ENEMY -> {
                // Reset all enemy units
                enemyCharacters.forEach { it.resetTurn() }
                currentTurn = Team.PLAYER
                gamePhase = GamePhase.UNIT_SELECT
                turnCount++
            }
            else -> {}
        }
        selectedCharacter = null
    }

    private fun executeEnemyTurn() {
        // Simple AI: Move towards nearest player unit and attack if possible
        for (enemy in enemyCharacters.filter { it.isAlive && it.canAct }) {
            val nearestPlayer = findNearestPlayerCharacter(enemy)
            if (nearestPlayer != null) {
                // Try to move closer
                if (enemy.canMove) {
                    val moveTarget = findBestMovePosition(enemy, nearestPlayer)
                    moveTarget?.let {
                        board.moveCharacter(enemy, it)
                        enemy.hasMovedThisTurn = true
                    }
                }

                // Try to attack
                val isInAttackRange =
                    enemy.position.distanceTo(nearestPlayer.position) <= enemy.characterClass.attackRange
                if (enemy.canAct && isInAttackRange) {
                    performAttack(enemy, nearestPlayer)
                    enemy.hasActedThisTurn = true
                }
            }
        }

        // End enemy turn
        endTurn()
    }

    private fun findNearestPlayerCharacter(enemy: Character): Character? =
        playerCharacters
            .filter { it.isAlive }
            .minByOrNull { it.position.distanceTo(enemy.position) }

    private fun findBestMovePosition(
        character: Character,
        target: Character,
    ): Position? {
        val possibleMoves = calculatePossibleMoves(character)
        return possibleMoves.minByOrNull { it.distanceTo(target.position) }
    }

    fun calculatePossibleMoves(character: Character): List<Position> {
        if (!character.canMove) return emptyList()

        val moves = mutableListOf<Position>()
        val visited = mutableSetOf<Position>()
        val queue = mutableListOf(Pair(character.position, 0))

        while (queue.isNotEmpty()) {
            val (currentPos, distance) = queue.removeAt(0)

            if (currentPos in visited) continue
            visited.add(currentPos)

            if (distance > 0) { // Don't include starting position
                val tile = board.getTile(currentPos)
                if (tile != null && tile.canBeOccupiedBy(character)) {
                    moves.add(currentPos)
                }
            }

            if (distance < character.characterClass.movementRange) {
                for (neighbor in currentPos.getNeighbors()) {
                    if (board.isValidPosition(neighbor) && neighbor !in visited) {
                        val tile = board.getTile(neighbor)
                        val movementRemaining = character.characterClass.movementRange - distance
                        if (tile != null && tile.terrain.movementCost <= movementRemaining) {
                            queue.add(Pair(neighbor, distance + tile.terrain.movementCost))
                        }
                    }
                }
            }
        }

        return moves
    }

    fun calculateAttackTargets(character: Character): List<Character> {
        if (!character.canAct) return emptyList()

        val targets = mutableListOf<Character>()
        val attackRange = character.characterClass.attackRange

        for (target in getAllCharacters()) {
            if (target.team != character.team && target.isAlive) {
                if (character.position.distanceTo(target.position) <= attackRange) {
                    targets.add(target)
                }
            }
        }

        return targets
    }

    fun performAttack(
        attacker: Character,
        target: Character,
    ): BattleResult {
        val damage = calculateDamage(attacker, target)
        target.takeDamage(damage)
        
        // Use weapon durability
        attacker.useEquippedWeapon()

        val result =
            BattleResult(
                attacker = attacker,
                target = target,
                damage = damage,
                targetDefeated = !target.isAlive,
            )

        if (result.targetDefeated) {
            attacker.gainExperience(target.level * EXPERIENCE_PER_KILL_MULTIPLIER)
            board.removeCharacter(target)
            removeDefeatedCharacter(target)
        } else {
            attacker.gainExperience(EXPERIENCE_PER_HIT)
        }

        checkGameEnd()
        return result
    }

    private fun calculateDamage(
        attacker: Character,
        target: Character,
    ): Int {
        val attackerWeapon = attacker.equippedWeapon
        val targetWeapon = target.equippedWeapon
        
        // Base attack from character stats
        var attackPower = attacker.currentStats.attack
        
        // Add weapon might
        if (attackerWeapon != null && !attackerWeapon.isBroken) {
            attackPower += attackerWeapon.might
            
            // Apply weapon triangle bonus
            if (targetWeapon != null) {
                val triangleBonus = attackerWeapon.getTriangleBonus(targetWeapon.type)
                attackPower = (attackPower * triangleBonus).toInt()
            }
            
            // Apply effective damage bonus
            if (attackerWeapon.isEffectiveAgainst(target.characterClass)) {
                attackPower *= 2
            }
        }
        
        // Get defender's defense
        var defensePower = target.currentStats.defense
        
        // Apply terrain defensive bonus
        val targetTile = board.getTile(target.position)
        if (targetTile != null) {
            defensePower += targetTile.terrain.defensiveBonus
        }
        
        // Calculate base damage
        val baseDamage = maxOf(1, attackPower - defensePower / 2)

        // Add some randomness (±25%)
        val variance = (baseDamage * DAMAGE_VARIANCE).toInt()
        return maxOf(1, baseDamage + (-variance..variance).random())
    }

    private fun removeDefeatedCharacter(character: Character) {
        when (character.team) {
            Team.PLAYER -> playerCharacters.remove(character)
            Team.ENEMY -> enemyCharacters.remove(character)
            else -> {}
        }
    }

    private fun checkGameEnd() {
        when {
            playerCharacters.none { it.isAlive } -> {
                gamePhase = GamePhase.GAME_OVER
            }
            enemyCharacters.none { it.isAlive } -> {
                gamePhase = GamePhase.GAME_OVER
            }
        }
    }

    fun isGameWon(): Boolean = enemyCharacters.none { it.isAlive }

    fun isGameLost(): Boolean = playerCharacters.none { it.isAlive }
}

data class BattleResult(
    val attacker: Character,
    val target: Character,
    val damage: Int,
    val targetDefeated: Boolean,
)
