package com.gameaday.opentactics

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.gameaday.opentactics.databinding.ActivityGameBinding
import com.gameaday.opentactics.game.GameState
import com.gameaday.opentactics.model.*
import com.gameaday.opentactics.view.GameBoardView

class GameActivity : AppCompatActivity() {
    private lateinit var binding: ActivityGameBinding
    private lateinit var gameState: GameState
    private lateinit var gameBoardView: GameBoardView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initializeGame()
        setupGameBoard()
        setupControls()
    }

    private fun initializeGame() {
        val board = GameBoard.createTestMap()
        gameState = GameState(board)
        
        // Create player characters
        val knight = Character(
            id = "player_knight",
            name = "Sir Garrett",
            characterClass = CharacterClass.KNIGHT,
            team = Team.PLAYER,
            position = Position(1, 6)
        )
        
        val archer = Character(
            id = "player_archer",
            name = "Lyanna",
            characterClass = CharacterClass.ARCHER,
            team = Team.PLAYER,
            position = Position(2, 7)
        )
        
        val mage = Character(
            id = "player_mage",
            name = "Aldric",
            characterClass = CharacterClass.MAGE,
            team = Team.PLAYER,
            position = Position(0, 7)
        )
        
        // Create enemy characters
        val enemyKnight = Character(
            id = "enemy_knight",
            name = "Dark Knight",
            characterClass = CharacterClass.KNIGHT,
            team = Team.ENEMY,
            position = Position(10, 1)
        )
        
        val enemyArcher = Character(
            id = "enemy_archer",
            name = "Bandit Archer",
            characterClass = CharacterClass.ARCHER,
            team = Team.ENEMY,
            position = Position(9, 2)
        )
        
        val enemyThief = Character(
            id = "enemy_thief",
            name = "Rogue",
            characterClass = CharacterClass.THIEF,
            team = Team.ENEMY,
            position = Position(11, 0)
        )
        
        // Add characters to game state
        gameState.addPlayerCharacter(knight)
        gameState.addPlayerCharacter(archer)
        gameState.addPlayerCharacter(mage)
        
        gameState.addEnemyCharacter(enemyKnight)
        gameState.addEnemyCharacter(enemyArcher)
        gameState.addEnemyCharacter(enemyThief)
        
        // Place characters on board
        board.placeCharacter(knight, knight.position)
        board.placeCharacter(archer, archer.position)
        board.placeCharacter(mage, mage.position)
        board.placeCharacter(enemyKnight, enemyKnight.position)
        board.placeCharacter(enemyArcher, enemyArcher.position)
        board.placeCharacter(enemyThief, enemyThief.position)
    }

    private fun setupGameBoard() {
        gameBoardView = GameBoardView(this)
        gameBoardView.setGameState(gameState)
        gameBoardView.onTileClicked = { position -> handleTileClick(position) }
        
        binding.gameContainer.addView(gameBoardView)
    }

    private fun setupControls() {
        binding.btnMove.setOnClickListener { handleMoveAction() }
        binding.btnAttack.setOnClickListener { handleAttackAction() }
        binding.btnWait.setOnClickListener { handleWaitAction() }
        binding.btnEndTurn.setOnClickListener { handleEndTurnAction() }
        
        updateUI()
    }

    private fun handleTileClick(position: Position) {
        when (gameState.gamePhase) {
            GameState.GamePhase.UNIT_SELECT -> {
                val character = gameState.board.getCharacterAt(position)
                if (character != null && gameState.canSelectCharacter(character)) {
                    gameState.selectCharacter(character)
                    showCharacterInfo(character)
                    updateUI()
                }
            }
            GameState.GamePhase.MOVEMENT -> {
                val selectedCharacter = gameState.selectedCharacter
                if (selectedCharacter != null) {
                    val possibleMoves = gameState.calculatePossibleMoves(selectedCharacter)
                    if (position in possibleMoves) {
                        gameState.board.moveCharacter(selectedCharacter, position)
                        selectedCharacter.hasMovedThisTurn = true
                        gameState.gamePhase = if (selectedCharacter.canAct) {
                            GameState.GamePhase.ACTION
                        } else {
                            GameState.GamePhase.UNIT_SELECT
                        }
                        gameBoardView.clearHighlights()
                        updateUI()
                    }
                }
            }
            GameState.GamePhase.ACTION -> {
                val selectedCharacter = gameState.selectedCharacter
                if (selectedCharacter != null) {
                    val targets = gameState.calculateAttackTargets(selectedCharacter)
                    val targetCharacter = gameState.board.getCharacterAt(position)
                    if (targetCharacter != null && targetCharacter in targets) {
                        val result = gameState.performAttack(selectedCharacter, targetCharacter)
                        showBattleResult(result)
                        selectedCharacter.hasActedThisTurn = true
                        gameState.selectCharacter(null)
                        gameBoardView.clearHighlights()
                        checkGameEnd()
                        updateUI()
                    }
                }
            }
            else -> {}
        }
    }

    private fun handleMoveAction() {
        val selectedCharacter = gameState.selectedCharacter
        if (selectedCharacter != null && selectedCharacter.canMove) {
            val possibleMoves = gameState.calculatePossibleMoves(selectedCharacter)
            gameBoardView.highlightMovement(possibleMoves)
            gameState.gamePhase = GameState.GamePhase.MOVEMENT
            updateUI()
        }
    }

    private fun handleAttackAction() {
        val selectedCharacter = gameState.selectedCharacter
        if (selectedCharacter != null && selectedCharacter.canAct) {
            val targets = gameState.calculateAttackTargets(selectedCharacter)
            gameBoardView.highlightAttacks(targets.map { it.position })
            gameState.gamePhase = GameState.GamePhase.ACTION
            updateUI()
        }
    }

    private fun handleWaitAction() {
        val selectedCharacter = gameState.selectedCharacter
        if (selectedCharacter != null) {
            selectedCharacter.hasActedThisTurn = true
            selectedCharacter.hasMovedThisTurn = true
            gameState.selectCharacter(null)
            gameBoardView.clearHighlights()
            updateUI()
        }
    }

    private fun handleEndTurnAction() {
        gameState.endTurn()
        gameBoardView.clearHighlights()
        updateUI()
        
        if (gameState.currentTurn == Team.PLAYER) {
            Toast.makeText(this, "Turn ${gameState.turnCount}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showCharacterInfo(character: Character) {
        binding.characterInfoPanel.visibility = android.view.View.VISIBLE
        binding.characterName.text = "${character.name} (${character.characterClass.displayName})"
        binding.characterStats.text = character.currentStats.toDisplayString()
    }

    private fun hideCharacterInfo() {
        binding.characterInfoPanel.visibility = android.view.View.GONE
    }

    private fun showBattleResult(result: com.gameaday.opentactics.game.BattleResult) {
        val message = if (result.targetDefeated) {
            "${result.attacker.name} defeated ${result.target.name}! (${result.damage} damage)"
        } else {
            "${result.attacker.name} attacks ${result.target.name} for ${result.damage} damage!"
        }
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun checkGameEnd() {
        when {
            gameState.isGameWon() -> {
                AlertDialog.Builder(this)
                    .setTitle("Victory!")
                    .setMessage("All enemies have been defeated!")
                    .setPositiveButton("OK") { _, _ -> finish() }
                    .show()
            }
            gameState.isGameLost() -> {
                AlertDialog.Builder(this)
                    .setTitle("Defeat")
                    .setMessage("All your units have fallen...")
                    .setPositiveButton("OK") { _, _ -> finish() }
                    .show()
            }
        }
    }

    private fun updateUI() {
        val selectedCharacter = gameState.selectedCharacter
        val isPlayerTurn = gameState.currentTurn == Team.PLAYER
        val canMove = selectedCharacter?.canMove == true
        val canAct = selectedCharacter?.canAct == true
        
        binding.btnMove.isEnabled = isPlayerTurn && canMove
        binding.btnAttack.isEnabled = isPlayerTurn && canAct
        binding.btnWait.isEnabled = isPlayerTurn && selectedCharacter != null
        binding.btnEndTurn.isEnabled = isPlayerTurn
        
        if (selectedCharacter != null) {
            showCharacterInfo(selectedCharacter)
        } else {
            hideCharacterInfo()
        }
        
        gameBoardView.invalidate()
    }
}