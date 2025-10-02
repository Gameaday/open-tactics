package com.gameaday.opentactics.game

import com.gameaday.opentactics.model.AIBehavior
import com.gameaday.opentactics.model.Chapter
import com.gameaday.opentactics.model.Character
import com.gameaday.opentactics.model.GameBoard
import com.gameaday.opentactics.model.Position
import com.gameaday.opentactics.model.Stats
import com.gameaday.opentactics.model.Team

// Battle and experience constants
private const val EXPERIENCE_PER_KILL_MULTIPLIER = 25
private const val EXPERIENCE_PER_HIT = 10
private const val EXPERIENCE_PER_HEAL = 12 // EXP for using healing staff
private const val DAMAGE_VARIANCE = 0.25
private const val CRITICAL_HIT_MULTIPLIER = 3

// Healing constants
private const val HEAL_STAFF_AMOUNT = 10
private const val MEND_STAFF_AMOUNT = 20
private const val DEFAULT_HEAL_AMOUNT = 10

// Critical hit constants
private const val LUCK_CRIT_DIVISOR = 2
private const val CRIT_ROLL_MAX = 100

// Battle forecast constants
private const val BASE_HIT_RATE = 90
private const val HIT_RATE_MIN = 50
private const val HIT_RATE_MAX = 100
private const val SPEED_HIT_RATE_MODIFIER = 2
private const val DOUBLE_ATTACK_SPEED_THRESHOLD = 5
private const val FORECAST_HIT_ASSUMPTION = 75

@Suppress("TooManyFunctions") // GameState requires many functions for game logic
class GameState(
    val board: GameBoard,
    private val playerCharacters: MutableList<Character> = mutableListOf(),
    private val enemyCharacters: MutableList<Character> = mutableListOf(),
    var currentChapter: Chapter? = null,
) {
    var currentTurn: Team = Team.PLAYER
    var selectedCharacter: Character? = null
    var gamePhase: GamePhase = GamePhase.UNIT_SELECT
    var turnCount: Int = 0
    var escapedUnitCount: Int = 0
    private val unitsOnThrone: MutableList<Character> = mutableListOf()
    private var moveBeforeAction: Boolean = false // Tracks if unit moved before attacking

    // Properties expected by tests
    val currentTeam: Team get() = currentTurn
    val turnCounter: Int get() = turnCount
    val isGameOver: Boolean
        get() =
            gamePhase == GamePhase.GAME_OVER ||
                (playerCharacters.isNotEmpty() && getAlivePlayerCharacters().isEmpty()) ||
                (enemyCharacters.isNotEmpty() && getAliveEnemyCharacters().isEmpty())
    val currentPlayerCharacter: Character?
        get() = if (currentTurn == Team.PLAYER) selectedCharacter else null

    enum class GamePhase {
        UNIT_SELECT,
        MOVEMENT,
        ACTION,
        CANTO_MOVEMENT, // Post-attack movement for units with Canto
        CONFIRM_WAIT, // Waiting for player to confirm end of unit's turn
        ENEMY_TURN,
        GAME_OVER,
    }

    fun getAllCharacters(): List<Character> = playerCharacters + enemyCharacters

    fun getPlayerCharacters(): List<Character> = playerCharacters.toList()

    fun getEnemyCharacters(): List<Character> = enemyCharacters.toList()

    fun getAlivePlayerCharacters(): List<Character> = playerCharacters.filter { it.isAlive }

    fun getAliveEnemyCharacters(): List<Character> = enemyCharacters.filter { it.isAlive }

    fun addPlayerCharacter(character: Character) {
        playerCharacters.add(character)
    }

    fun addEnemyCharacter(character: Character) {
        enemyCharacters.add(character)
    }

    fun selectCharacter(character: Character?): Boolean {
        if (character != null && character.team == currentTurn && character.isAlive) {
            selectedCharacter = character
            moveBeforeAction = false
            gamePhase =
                when {
                    character.canMoveNow() -> GamePhase.MOVEMENT
                    character.canAct -> GamePhase.ACTION
                    else -> GamePhase.UNIT_SELECT
                }
            return true
        } else {
            selectedCharacter = null
            gamePhase = GamePhase.UNIT_SELECT
            return false
        }
    }

    fun canSelectCharacter(character: Character): Boolean =
        character.team == currentTurn &&
            (character.canMove || character.canAct)

    /**
     * Perform a character move
     * @return true if move was successful
     */
    @Suppress("ReturnCount") // Multiple validation points require early returns
    fun performMove(
        character: Character,
        destination: Position,
    ): Boolean {
        if (character != selectedCharacter) return false
        if (!character.canMoveNow()) return false

        val possibleMoves = calculatePossibleMoves(character)
        if (destination !in possibleMoves) return false

        val previousPos = character.position
        board.moveCharacter(character, destination)
        character.commitMove(previousPos)
        moveBeforeAction = !character.hasActedThisTurn

        // Update game phase based on what actions are still available
        gamePhase =
            when {
                character.canAct -> GamePhase.ACTION
                character.canStillMoveAfterAttack -> GamePhase.CANTO_MOVEMENT
                else -> GamePhase.CONFIRM_WAIT
            }

        return true
    }

    /**
     * Undo the last move for the selected character
     * @return true if undo was successful
     */
    fun undoMove(): Boolean {
        val character = selectedCharacter ?: return false
        val previousPos = character.undoMove() ?: return false

        board.moveCharacter(character, previousPos)
        gamePhase = GamePhase.MOVEMENT
        moveBeforeAction = false

        return true
    }

    /**
     * Perform an attack action
     * @return BattleResult of the attack
     */
    @Suppress("ReturnCount") // Multiple validation points require early returns
    fun performPlayerAttack(target: Character): BattleResult? {
        val character = selectedCharacter ?: return null
        if (!character.canAct) return null
        if (target.team == character.team) return null
        if (!character.canAttackPosition(target.position)) return null

        val result = performAttack(character, target)
        character.commitAction()

        // Update game phase based on Canto ability
        gamePhase =
            when {
                character.canStillMoveAfterAttack -> GamePhase.CANTO_MOVEMENT
                else -> GamePhase.CONFIRM_WAIT
            }

        return result
    }

    /**
     * Commit the wait action - ends the character's turn
     */
    fun performWait() {
        val character = selectedCharacter ?: return
        character.commitWait()
        selectedCharacter = null
        gamePhase = GamePhase.UNIT_SELECT
    }

    fun endTurn() {
        when (currentTurn) {
            Team.PLAYER -> {
                // Reset all player units
                playerCharacters.forEach { it.resetTurn() }
                currentTurn = Team.ENEMY
                gamePhase = GamePhase.ENEMY_TURN
                turnCount++
                spawnReinforcements() // Spawn reinforcements at start of enemy turn
                executeEnemyTurn()
            }
            Team.ENEMY -> {
                // Reset all enemy units
                enemyCharacters.forEach { it.resetTurn() }
                currentTurn = Team.PLAYER
                gamePhase = GamePhase.UNIT_SELECT
            }
            else -> {}
        }
        selectedCharacter = null
        checkGameEnd()
    }

    /**
     * Execute AI action for a single enemy unit (called from UI for animated turns)
     */
    fun executeEnemyAction(enemy: Character) {
        if (!enemy.isAlive || enemy.hasActedThisTurn) return
        val behavior = getEnemyBehavior(enemy)
        executeAIBehavior(enemy, behavior)
    }

    private fun executeEnemyTurn() {
        // Execute AI for each enemy unit based on their behavior
        for (enemy in enemyCharacters.filter { it.isAlive && it.canAct }) {
            val behavior = getEnemyBehavior(enemy)
            executeAIBehavior(enemy, behavior)
        }

        // End enemy turn
        endTurn()
    }

    /**
     * Get the AI behavior for an enemy unit
     */
    private fun getEnemyBehavior(enemy: Character): AIBehavior {
        // Check if this is a boss unit with specific behavior
        currentChapter?.let { chapter ->
            if (chapter.bossUnit?.id == enemy.id) {
                return chapter.bossUnit.aiType
            }

            // Check other enemy units for their behavior
            chapter.enemyUnits.find { it.id == enemy.id }?.let { spawn ->
                return spawn.aiType
            }
        }

        // Default behavior
        return AIBehavior.AGGRESSIVE
    }

    /**
     * Execute AI behavior for an enemy unit
     */
    @Suppress("ComplexMethod") // AI logic inherently complex
    private fun executeAIBehavior(enemy: Character, behavior: AIBehavior) {
        when (behavior) {
            AIBehavior.AGGRESSIVE -> executeAggressiveBehavior(enemy)
            AIBehavior.DEFENSIVE -> executeDefensiveBehavior(enemy)
            AIBehavior.STATIONARY -> executeStationaryBehavior(enemy)
            AIBehavior.SUPPORT -> executeSupportBehavior(enemy)
        }
    }

    /**
     * Aggressive AI: Always move towards and attack nearest player unit
     */
    private fun executeAggressiveBehavior(enemy: Character) {
        val target = findNearestPlayerCharacter(enemy) ?: return

        // Try to attack first if in range
        val distance = enemy.position.distanceTo(target.position)
        val inRange =
            enemy.equippedWeapon?.let { distance in it.range }
                ?: (distance <= enemy.characterClass.attackRange)

        if (enemy.canAct && inRange) {
            performAttack(enemy, target)
            enemy.commitAction()
        }

        // Then try to move (or move closer if couldn't attack)
        if (enemy.canMoveNow()) {
            val moveTarget = findBestMovePosition(enemy, target)
            moveTarget?.let {
                board.moveCharacter(enemy, it)
                enemy.commitMove(enemy.position)
            }

            // If has Canto and already attacked, try to attack again or retreat
            if (enemy.characterClass.hasCanto && !enemy.canAct) {
                // Try to attack again if now in range
                val newDistance = enemy.position.distanceTo(target.position)
                val newInRange =
                    enemy.equippedWeapon?.let { newDistance in it.range }
                        ?: (newDistance <= enemy.characterClass.attackRange)
                if (newInRange) {
                    performAttack(enemy, target)
                    enemy.commitAction()
                }
            }
        }

        // If can still attack after moving
        if (enemy.canAct) {
            val distanceAfterMove = enemy.position.distanceTo(target.position)
            val inRangeAfterMove =
                enemy.equippedWeapon?.let { distanceAfterMove in it.range }
                    ?: (distanceAfterMove <= enemy.characterClass.attackRange)
            if (inRangeAfterMove) {
                performAttack(enemy, target)
                enemy.commitAction()
            }
        }

        enemy.commitWait()
    }

    /**
     * Defensive AI: Only attacks when player units are in range, doesn't actively pursue
     */
    private fun executeDefensiveBehavior(enemy: Character) {
        // Find all player units in attack range
        val targetsInRange =
            playerCharacters.filter { player ->
                val distance = enemy.position.distanceTo(player.position)
                val inRange =
                    enemy.equippedWeapon?.let { distance in it.range }
                        ?: (distance <= enemy.characterClass.attackRange)
                player.isAlive && inRange
            }

        if (targetsInRange.isNotEmpty() && enemy.canAct) {
            // Attack the weakest target in range
            val target = targetsInRange.minByOrNull { it.currentStats.hp } ?: return
            performAttack(enemy, target)
            enemy.commitAction()
        } else if (enemy.canMove) {
            // Move to a defensive position (preferably terrain with bonuses)
            val defensivePosition = findBestDefensivePosition(enemy)
            defensivePosition?.let {
                board.moveCharacter(enemy, it)
                enemy.commitMove(enemy.position)
            }
        }

        enemy.commitWait()
    }

    /**
     * Stationary AI: Does not move unless attacked, only attacks units in range
     */
    private fun executeStationaryBehavior(enemy: Character) {
        // Only act if has been attacked this turn or player is in range
        val targetsInRange =
            playerCharacters.filter { player ->
                val distance = enemy.position.distanceTo(player.position)
                val inRange =
                    enemy.equippedWeapon?.let { distance in it.range }
                        ?: (distance <= enemy.characterClass.attackRange)
                player.isAlive && inRange
            }

        if (targetsInRange.isNotEmpty() && enemy.canAct) {
            // Attack the closest target
            val target =
                targetsInRange.minByOrNull {
                    enemy.position.distanceTo(it.position)
                } ?: return
            performAttack(enemy, target)
            enemy.commitAction()
        }

        enemy.commitWait()
    }

    /**
     * Support AI: Prioritizes healing allies (future: buffing)
     */
    private fun executeSupportBehavior(enemy: Character) {
        // If this is a healer, try to heal wounded allies
        if (enemy.characterClass == com.gameaday.opentactics.model.CharacterClass.HEALER) {
            val woundedAllies =
                enemyCharacters.filter { ally ->
                    ally.isAlive &&
                        ally != enemy &&
                        ally.currentHp < ally.maxHp &&
                        enemy.position.distanceTo(ally.position) <= enemy.characterClass.attackRange
                }

            if (woundedAllies.isNotEmpty()) {
                // Heal the most wounded ally
                val target = woundedAllies.minByOrNull { it.currentStats.hp } ?: return
                // TODO: Implement healing when staff weapons are added
                // For now, just move towards them
                if (enemy.canMove) {
                    val moveTarget = findBestMovePosition(enemy, target)
                    moveTarget?.let {
                        board.moveCharacter(enemy, it)
                        enemy.commitMove(enemy.position)
                    }
                }
            }
        } else {
            // Fall back to defensive behavior
            executeDefensiveBehavior(enemy)
        }

        enemy.commitWait()
    }

    /**
     * Find best defensive position with terrain bonuses
     */
    private fun findBestDefensivePosition(character: Character): Position? {
        val possibleMoves = calculatePossibleMoves(character)
        if (possibleMoves.isEmpty()) return null

        // Prefer positions with high defensive bonuses
        return possibleMoves.maxByOrNull { pos ->
            val tile = board.getTile(pos)
            tile?.terrain?.defensiveBonus ?: 0
        }
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
        if (!character.canMoveNow()) return emptyList()

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

    /**
     * Calculate valid heal targets for a character with a healing staff
     */
    fun calculateHealTargets(character: Character): List<Character> {
        if (!character.canAct) return emptyList()

        val staff = character.equippedWeapon
        if (staff == null || !staff.canHeal) return emptyList()

        val targets = mutableListOf<Character>()
        val healRange = staff.range.first // Use weapon range

        for (target in getAllCharacters()) {
            // Can heal allies (same team) who are wounded
            if (target.team == character.team && target.isAlive && target.currentHp < target.maxHp) {
                if (character.position.distanceTo(target.position) <= healRange) {
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
        // Track level before EXP gain
        val previousLevel = attacker.level

        // Check for critical hit
        val isCritical = checkCriticalHit(attacker)
        val damage = calculateDamage(attacker, target, isCritical)
        target.takeDamage(damage)

        // Use weapon durability
        attacker.useEquippedWeapon()

        // Award EXP and track stat gains
        val expGained =
            if (!target.isAlive) {
                target.level * EXPERIENCE_PER_KILL_MULTIPLIER
            } else {
                EXPERIENCE_PER_HIT
            }

        val statGains = attacker.gainExperienceWithTracking(expGained)

        val result =
            BattleResult(
                attacker = attacker,
                target = target,
                damage = damage,
                targetDefeated = !target.isAlive,
                wasCritical = isCritical,
                expGained = expGained,
                previousLevel = previousLevel,
                statGains = statGains,
            )

        if (result.targetDefeated) {
            board.removeCharacter(target)
            removeDefeatedCharacter(target)
        }

        checkGameEnd()
        return result
    }

    /**
     * Perform a healing action with a staff
     * @param healer The character using the healing staff
     * @param target The character being healed
     * @return SupportResult with healing details and EXP gained
     */
    fun performHeal(
        healer: Character,
        target: Character,
    ): SupportResult {
        // Track level before EXP gain
        val previousLevel = healer.level

        // Calculate heal amount based on staff
        val staff = healer.equippedWeapon
        val healAmount =
            when {
                staff == null || !staff.canHeal -> 0
                staff.name == "Heal" -> HEAL_STAFF_AMOUNT
                staff.name == "Mend" -> MEND_STAFF_AMOUNT
                else -> DEFAULT_HEAL_AMOUNT
            }

        // Apply healing
        target.heal(healAmount)

        // Use staff durability
        healer.useEquippedWeapon()

        // Award EXP for healing (only if target was actually wounded)
        val expGained =
            if (target.currentHp < target.maxHp || healAmount > 0) {
                EXPERIENCE_PER_HEAL
            } else {
                0
            }

        val statGains = healer.gainExperienceWithTracking(expGained)

        val result =
            SupportResult(
                user = healer,
                target = target,
                healAmount = healAmount,
                expGained = expGained,
                previousLevel = previousLevel,
                statGains = statGains,
            )

        return result
    }

    private fun checkCriticalHit(attacker: Character): Boolean {
        // Critical rate = (Skill + Luck / 2)
        val critRate = attacker.currentStats.skill + (attacker.currentStats.luck / LUCK_CRIT_DIVISOR)
        return (1..CRIT_ROLL_MAX).random() <= critRate
    }

    private fun calculateDamage(
        attacker: Character,
        target: Character,
        isCritical: Boolean = false,
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

            // Apply effective damage bonus (2x against specific classes)
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

        // Apply critical hit multiplier
        val criticalDamage = if (isCritical) baseDamage * CRITICAL_HIT_MULTIPLIER else baseDamage

        // Add some randomness (±25%)
        val variance = (criticalDamage * DAMAGE_VARIANCE).toInt()
        return maxOf(1, criticalDamage + (-variance..variance).random())
    }

    private fun removeDefeatedCharacter(character: Character) {
        when (character.team) {
            Team.PLAYER -> playerCharacters.remove(character)
            Team.ENEMY -> enemyCharacters.remove(character)
            else -> {}
        }
    }

    private fun checkGameEnd() {
        // Check chapter objectives if chapter is set
        currentChapter?.let { chapter ->
            if (chapter.isObjectiveComplete(
                    playerCharacters,
                    enemyCharacters,
                    turnCount,
                    unitsOnThrone,
                    escapedUnitCount,
                )
            ) {
                gamePhase = GamePhase.GAME_OVER
                return
            }

            if (chapter.isObjectiveFailed(playerCharacters, turnCount)) {
                gamePhase = GamePhase.GAME_OVER
                return
            }
        }

        // Default victory/defeat conditions
        when {
            playerCharacters.none { it.isAlive } -> {
                gamePhase = GamePhase.GAME_OVER
            }
            enemyCharacters.none { it.isAlive } -> {
                gamePhase = GamePhase.GAME_OVER
            }
        }
    }

    fun isGameWon(): Boolean {
        currentChapter?.let { chapter ->
            return chapter.isObjectiveComplete(
                playerCharacters,
                enemyCharacters,
                turnCount,
                unitsOnThrone,
                escapedUnitCount,
            )
        }
        // Default: all enemies defeated
        return enemyCharacters.isNotEmpty() && enemyCharacters.none { it.isAlive }
    }

    fun isGameLost(): Boolean {
        currentChapter?.let { chapter ->
            return chapter.isObjectiveFailed(playerCharacters, turnCount)
        }
        // Default: all players defeated
        return playerCharacters.isNotEmpty() && playerCharacters.none { it.isAlive }
    }

    /**
     * Mark a unit as being on the throne (for SEIZE_THRONE objective)
     */
    fun markUnitOnThrone(character: Character) {
        if (currentChapter?.thronePosition == character.position) {
            if (!unitsOnThrone.contains(character)) {
                unitsOnThrone.add(character)
            }
        }
    }

    /**
     * Mark a unit as escaped (for ESCAPE objective)
     */
    fun markUnitEscaped(character: Character) {
        currentChapter?.let { chapter ->
            if (character.position in chapter.escapePositions) {
                escapedUnitCount++
                playerCharacters.remove(character)
                board.removeCharacter(character)
            }
        }
    }

    /**
     * Spawn reinforcements for the current turn
     */
    fun spawnReinforcements() {
        currentChapter?.let { chapter ->
            val reinforcements = chapter.getReinforcementsForTurn(turnCount)
            reinforcements.forEach { spawn ->
                // Create character from spawn data
                val reinforcement =
                    Character(
                        id = spawn.id,
                        name = spawn.name,
                        characterClass = spawn.characterClass,
                        team = Team.ENEMY,
                        position = spawn.position,
                        level = spawn.level,
                    )
                // Add weapons from equipment list
                spawn.equipment.forEach { weaponId ->
                    // Weapon factory would go here
                }
                addEnemyCharacter(reinforcement)
                board.placeCharacter(reinforcement, spawn.position)
            }
        }
    }

    /**
     * Calculate battle forecast between attacker and target
     * Shows predicted damage, hit rates, and potential outcomes
     */
    fun calculateBattleForecast(
        attacker: Character,
        target: Character,
    ): BattleForecast {
        // Calculate attacker's damage
        val attackerDamage = calculateDamage(attacker, target)

        // Calculate if target can counter-attack
        val canCounter = canCounterAttack(attacker, target)
        val counterDamage = if (canCounter) calculateDamage(target, attacker) else 0

        // Calculate hit rates (simplified - base 90%, adjusted by speed difference)
        val speedDiff = attacker.currentStats.speed - target.currentStats.speed
        val attackerHitRate =
            minOf(
                HIT_RATE_MAX,
                maxOf(HIT_RATE_MIN, BASE_HIT_RATE + speedDiff * SPEED_HIT_RATE_MODIFIER),
            )
        val targetHitRate =
            if (canCounter) {
                minOf(
                    HIT_RATE_MAX,
                    maxOf(HIT_RATE_MIN, BASE_HIT_RATE - speedDiff * SPEED_HIT_RATE_MODIFIER),
                )
            } else {
                0
            }

        // Calculate if attacker can double attack (speed >= target speed + 5)
        val attackerDoubles = attacker.currentStats.speed >= target.currentStats.speed + DOUBLE_ATTACK_SPEED_THRESHOLD
        val targetDoubles =
            canCounter &&
                target.currentStats.speed >= attacker.currentStats.speed + DOUBLE_ATTACK_SPEED_THRESHOLD

        // Predict battle outcome
        var predictedAttackerHp = attacker.currentStats.hp
        var predictedTargetHp = target.currentStats.hp

        // Attacker's first strike
        if (attackerHitRate >= FORECAST_HIT_ASSUMPTION) { // Assume hit if 75%+ chance
            predictedTargetHp = maxOf(0, predictedTargetHp - attackerDamage)
        }

        // Target counter if alive
        if (predictedTargetHp > 0 && canCounter && targetHitRate >= FORECAST_HIT_ASSUMPTION) {
            predictedAttackerHp = maxOf(0, predictedAttackerHp - counterDamage)
        }

        // Double attacks if applicable and target alive
        if (predictedTargetHp > 0 && attackerDoubles && predictedAttackerHp > 0) {
            if (attackerHitRate >= FORECAST_HIT_ASSUMPTION) {
                predictedTargetHp = maxOf(0, predictedTargetHp - attackerDamage)
            }
        }

        @Suppress("ComplexCondition") // Battle simulation requires multiple state checks
        if (predictedAttackerHp > 0 && predictedTargetHp > 0 && targetDoubles && canCounter) {
            if (targetHitRate >= FORECAST_HIT_ASSUMPTION) {
                predictedAttackerHp = maxOf(0, predictedAttackerHp - counterDamage)
            }
        }

        return BattleForecast(
            attackerDamage = attackerDamage,
            targetDamage = counterDamage,
            attackerHitRate = attackerHitRate,
            targetHitRate = targetHitRate,
            attackerDoubles = attackerDoubles,
            targetDoubles = targetDoubles,
            canCounter = canCounter,
            predictedAttackerHp = predictedAttackerHp,
            predictedTargetHp = predictedTargetHp,
            targetWillBeDefeated = predictedTargetHp <= 0,
            attackerWillBeDefeated = predictedAttackerHp <= 0,
        )
    }

    /**
     * Check if target can counter-attack
     */
    private fun canCounterAttack(
        attacker: Character,
        target: Character,
    ): Boolean {
        if (!target.canAct) return false
        if (target.equippedWeapon == null || target.equippedWeapon!!.isBroken) return false

        val distance = attacker.position.distanceTo(target.position)
        val targetWeaponRange = target.equippedWeapon!!.range

        return distance in targetWeaponRange
    }
}

/**
 * Battle forecast data for UI display
 */
data class BattleForecast(
    val attackerDamage: Int,
    val targetDamage: Int,
    val attackerHitRate: Int, // 0-100
    val targetHitRate: Int, // 0-100
    val attackerDoubles: Boolean,
    val targetDoubles: Boolean,
    val canCounter: Boolean,
    val predictedAttackerHp: Int,
    val predictedTargetHp: Int,
    val targetWillBeDefeated: Boolean,
    val attackerWillBeDefeated: Boolean,
)

data class BattleResult(
    val attacker: Character,
    val target: Character,
    val damage: Int,
    val targetDefeated: Boolean,
    val wasCritical: Boolean = false,
    val expGained: Int = 0,
    val previousLevel: Int = 0,
    val statGains: Stats? = null,
)

data class SupportResult(
    val user: Character,
    val target: Character,
    val healAmount: Int,
    val expGained: Int = 0,
    val previousLevel: Int = 0,
    val statGains: Stats? = null,
)
