package com.gameaday.opentactics

import com.gameaday.opentactics.game.GameState
import com.gameaday.opentactics.model.*

fun main() {
    println("=================================")
    println("    OPEN TACTICS DEMO")
    println("  Fire Emblem-style Tactical RPG")
    println("=================================")

    val demo = GameDemo()
    demo.run()
}

class GameDemo {
    private lateinit var gameState: GameState

    fun run() {
        initializeGame()

        println("\nInitial Board State:")
        printBoard()

        println("\n=== TACTICAL BATTLE SIMULATION ===")
        simulateBattle()
    }

    private fun initializeGame() {
        val board = GameBoard.createTestMap()
        gameState = GameState(board)

        // Create player characters
        val knight =
            Character(
                id = "player_knight",
                name = "Sir Garrett",
                characterClass = CharacterClass.KNIGHT,
                team = Team.PLAYER,
                position = Position(1, 6),
            )

        val archer =
            Character(
                id = "player_archer",
                name = "Lyanna",
                characterClass = CharacterClass.ARCHER,
                team = Team.PLAYER,
                position = Position(2, 7),
            )

        val mage =
            Character(
                id = "player_mage",
                name = "Aldric",
                characterClass = CharacterClass.MAGE,
                team = Team.PLAYER,
                position = Position(0, 7),
            )

        // Create enemy characters
        val enemyKnight =
            Character(
                id = "enemy_knight",
                name = "Dark Knight",
                characterClass = CharacterClass.KNIGHT,
                team = Team.ENEMY,
                position = Position(10, 1),
            )

        val enemyArcher =
            Character(
                id = "enemy_archer",
                name = "Bandit Archer",
                characterClass = CharacterClass.ARCHER,
                team = Team.ENEMY,
                position = Position(9, 2),
            )

        // Add characters to game state
        gameState.addPlayerCharacter(knight)
        gameState.addPlayerCharacter(archer)
        gameState.addPlayerCharacter(mage)
        gameState.addEnemyCharacter(enemyKnight)
        gameState.addEnemyCharacter(enemyArcher)

        // Place characters on board
        board.placeCharacter(knight, knight.position)
        board.placeCharacter(archer, archer.position)
        board.placeCharacter(mage, mage.position)
        board.placeCharacter(enemyKnight, enemyKnight.position)
        board.placeCharacter(enemyArcher, enemyArcher.position)

        println(
            "Game initialized with ${gameState.getPlayerCharacters().size} player units and ${gameState.getEnemyCharacters().size} enemy units",
        )
    }

    private fun printBoard() {
        val board = gameState.board

        // Print column headers
        print("   ")
        for (x in 0 until board.width) {
            print("%2d ".format(x))
        }
        println()

        for (y in 0 until board.height) {
            print("%2d ".format(y))

            for (x in 0 until board.width) {
                val character = board.getCharacterAt(Position(x, y))
                val tile = board.getTile(Position(x, y))

                when {
                    character != null -> {
                        val symbol =
                            when (character.team) {
                                Team.PLAYER ->
                                    when (character.characterClass) {
                                        CharacterClass.KNIGHT -> "♔"
                                        CharacterClass.ARCHER -> "♖"
                                        CharacterClass.MAGE -> "♗"
                                        CharacterClass.HEALER -> "♕"
                                        CharacterClass.THIEF -> "♘"
                                    }
                                Team.ENEMY ->
                                    when (character.characterClass) {
                                        CharacterClass.KNIGHT -> "♚"
                                        CharacterClass.ARCHER -> "♜"
                                        CharacterClass.MAGE -> "♝"
                                        CharacterClass.HEALER -> "♛"
                                        CharacterClass.THIEF -> "♞"
                                    }
                                else -> "?"
                            }
                        print(" $symbol ")
                    }
                    else -> {
                        val terrain =
                            when (tile?.terrain) {
                                TerrainType.PLAIN -> "."
                                TerrainType.FOREST -> "♠"
                                TerrainType.MOUNTAIN -> "▲"
                                TerrainType.FORT -> "⌂"
                                TerrainType.VILLAGE -> "⌂"
                                TerrainType.WATER -> "~"
                                else -> "?"
                            }
                        print(" $terrain ")
                    }
                }
            }
            println()
        }

        println("\nLegend:")
        println("Player: ♔ Knight, ♖ Archer, ♗ Mage, ♕ Healer, ♘ Thief")
        println("Enemy:  ♚ Knight, ♜ Archer, ♝ Mage, ♛ Healer, ♞ Thief")
        println("Terrain: . Plain, ♠ Forest, ▲ Mountain, ⌂ Fort/Village, ~ Water")
    }

    private fun simulateBattle() {
        var turnCount = 0
        val maxTurns = 20

        while (!gameState.isGameWon() && !gameState.isGameLost() && turnCount < maxTurns) {
            turnCount++

            println("\n--- TURN $turnCount (${gameState.currentTurn}) ---")

            when (gameState.currentTurn) {
                Team.PLAYER -> simulatePlayerTurn()
                Team.ENEMY -> simulateEnemyTurn()
                else -> {}
            }

            gameState.endTurn()

            if (gameState.currentTurn == Team.PLAYER) {
                printBoard()
                printUnitStatus()
            }
        }

        println("\n=== BATTLE RESULTS ===")
        when {
            gameState.isGameWon() -> println("🎉 VICTORY! All enemies defeated!")
            gameState.isGameLost() -> println("💀 DEFEAT! All player units fallen!")
            else -> println("⏰ Battle ended after $maxTurns turns")
        }

        printFinalStats()
    }

    private fun simulatePlayerTurn() {
        val playerUnits = gameState.getPlayerCharacters().filter { it.isAlive && it.canAct }

        for (unit in playerUnits) {
            println("${unit.name} (${unit.characterClass.displayName}) takes action...")

            // Try to find enemy in attack range
            val targets = gameState.calculateAttackTargets(unit)
            if (targets.isNotEmpty()) {
                val target = targets.first()
                println("  Attacking ${target.name}!")
                val result = gameState.performAttack(unit, target)
                println("  💥 Dealt ${result.damage} damage! (${target.currentHp}/${target.maxHp} HP remaining)")
                if (result.targetDefeated) {
                    println("  ☠️ ${target.name} has been defeated!")
                }
                unit.hasActedThisTurn = true
            } else {
                // Try to move closer to an enemy
                val possibleMoves = gameState.calculatePossibleMoves(unit)
                if (possibleMoves.isNotEmpty()) {
                    val nearestEnemy =
                        gameState
                            .getEnemyCharacters()
                            .filter { it.isAlive }
                            .minByOrNull { it.position.distanceTo(unit.position) }

                    if (nearestEnemy != null) {
                        val bestMove =
                            possibleMoves.minByOrNull {
                                it.distanceTo(nearestEnemy.position)
                            }
                        if (bestMove != null && bestMove != unit.position) {
                            println("  Moving towards ${nearestEnemy.name}...")
                            gameState.board.moveCharacter(unit, bestMove)
                            unit.hasMovedThisTurn = true
                        }
                    }
                }
                unit.hasActedThisTurn = true
            }
        }
    }

    private fun simulateEnemyTurn() {
        // Enemy turn is handled by GameState.endTurn()
        println("Enemy units are planning their moves...")
    }

    private fun printUnitStatus() {
        println("\nUnit Status:")
        println("PLAYER UNITS:")
        for (unit in gameState.getPlayerCharacters().filter { it.isAlive }) {
            val stats = unit.currentStats
            println(
                "  ${unit.name} (Lv.${unit.level} ${unit.characterClass.displayName}) - HP: ${unit.currentHp}/${stats.hp}, ATK: ${stats.attack}, DEF: ${stats.defense}",
            )
        }

        println("ENEMY UNITS:")
        for (unit in gameState.getEnemyCharacters().filter { it.isAlive }) {
            val stats = unit.currentStats
            println(
                "  ${unit.name} (Lv.${unit.level} ${unit.characterClass.displayName}) - HP: ${unit.currentHp}/${stats.hp}, ATK: ${stats.attack}, DEF: ${stats.defense}",
            )
        }
    }

    private fun printFinalStats() {
        println("\nFinal Statistics:")

        val survivingPlayers = gameState.getPlayerCharacters().filter { it.isAlive }
        val survivingEnemies = gameState.getEnemyCharacters().filter { it.isAlive }

        println("Surviving Players: ${survivingPlayers.size}")
        survivingPlayers.forEach {
            println("  • ${it.name} (Lv.${it.level}, ${it.experience} exp)")
        }

        println("Surviving Enemies: ${survivingEnemies.size}")
        survivingEnemies.forEach {
            println("  • ${it.name} (Lv.${it.level})")
        }
    }
}
