package com.gameaday.opentactics

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.gameaday.opentactics.data.*
import com.gameaday.opentactics.databinding.ActivityGameBinding
import com.gameaday.opentactics.game.GameState
import com.gameaday.opentactics.model.*
import com.gameaday.opentactics.view.GameBoardView
import kotlinx.coroutines.launch

class GameActivity : AppCompatActivity() {
    private lateinit var binding: ActivityGameBinding
    private lateinit var gameState: GameState
    private lateinit var gameBoardView: GameBoardView
    private lateinit var saveGameManager: SaveGameManager

    private var playerProfile: PlayerProfile? = null
    private var currentGameSave: GameSave? = null
    private var turnsSinceLastSave = 0

    companion object {
        const val EXTRA_LOAD_SAVE_ID = "load_save_id"
        const val EXTRA_PLAYER_NAME = "player_name"
        const val EXTRA_IS_NEW_GAME = "is_new_game"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        saveGameManager = SaveGameManager(this)
        loadPlayerProfile()

        val loadSaveId = intent.getStringExtra(EXTRA_LOAD_SAVE_ID)
        val playerName = intent.getStringExtra(EXTRA_PLAYER_NAME) ?: "Player"
        val isNewGame = intent.getBooleanExtra(EXTRA_IS_NEW_GAME, true)

        if (isNewGame || loadSaveId == null) {
            initializeNewGame(playerName)
        } else {
            loadGame(loadSaveId)
        }

        setupGameBoard()
        setupControls()
    }

    override fun onPause() {
        super.onPause()
        // Auto-save when the activity is paused
        performAutoSave()
    }

    private fun loadPlayerProfile() {
        playerProfile = saveGameManager.loadProfile() ?: PlayerProfile(
            playerName = "Player",
            totalPlayTime = 0,
        )
    }

    private fun savePlayerProfile() {
        playerProfile?.let { saveGameManager.saveProfile(it) }
    }

    private fun initializeNewGame(playerName: String) {
        val board = GameBoard.createTestMap()
        gameState = GameState(board)

        // Create player characters with the enhanced stats
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

        val enemyThief =
            Character(
                id = "enemy_thief",
                name = "Rogue",
                characterClass = CharacterClass.THIEF,
                team = Team.ENEMY,
                position = Position(11, 0),
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

        // Create initial save data
        currentGameSave = createGameSave(playerName, 1)
    }

    private fun loadGame(saveId: String) {
        lifecycleScope.launch {
            saveGameManager.loadGame(saveId).fold(
                onSuccess = { gameSave ->
                    currentGameSave = gameSave
                    restoreGameFromSave(gameSave)
                    Toast.makeText(this@GameActivity, "Game loaded successfully!", Toast.LENGTH_SHORT).show()
                },
                onFailure = { error ->
                    Toast.makeText(this@GameActivity, "Failed to load game: ${error.message}", Toast.LENGTH_LONG).show()
                    // Fall back to new game
                    initializeNewGame(playerProfile?.playerName ?: "Player")
                },
            )
        }
    }

    private fun restoreGameFromSave(gameSave: GameSave) {
        val savedState = gameSave.gameState
        val board = GameBoard(savedState.boardWidth, savedState.boardHeight)
        gameState = GameState(board)

        // Restore characters
        savedState.playerCharacters.forEach { character ->
            gameState.addPlayerCharacter(character)
            board.placeCharacter(character, character.position)
        }

        savedState.enemyCharacters.forEach { character ->
            gameState.addEnemyCharacter(character)
            board.placeCharacter(character, character.position)
        }

        // Restore game state
        gameState.currentTurn = savedState.currentTurn
        gameState.turnCount = savedState.turnCount
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

        // Add save/load buttons to the overflow menu
        binding.root.setOnLongClickListener {
            showGameMenu()
            true
        }

        updateUI()
    }

    private fun showGameMenu() {
        val options = arrayOf("Save Game", "Load Game", "Settings", "Quit to Menu")

        AlertDialog
            .Builder(this)
            .setTitle("Game Menu")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> performManualSave()
                    1 -> showLoadGameDialog()
                    2 -> showSettingsDialog()
                    3 -> confirmQuitToMenu()
                }
            }.show()
    }

    private fun performManualSave() {
        currentGameSave?.let { save ->
            val updatedSave = createGameSave(save.playerName, save.campaignLevel)
            lifecycleScope.launch {
                saveGameManager.saveGame(updatedSave, isAutoSave = false).fold(
                    onSuccess = {
                        Toast.makeText(this@GameActivity, "Game saved successfully!", Toast.LENGTH_SHORT).show()
                        currentGameSave = updatedSave
                    },
                    onFailure = { error ->
                        Toast.makeText(this@GameActivity, "Failed to save game: ${error.message}", Toast.LENGTH_LONG).show()
                    },
                )
            }
        }
    }

    private fun performAutoSave() {
        val preferences = playerProfile?.preferences
        if (preferences?.autoSaveEnabled == true && turnsSinceLastSave >= preferences.autoSaveFrequency) {
            currentGameSave?.let { save ->
                val autoSave = createGameSave(save.playerName, save.campaignLevel)
                lifecycleScope.launch {
                    saveGameManager.saveGame(autoSave, isAutoSave = true)
                    turnsSinceLastSave = 0
                }
            }
        }
    }

    private fun createGameSave(
        playerName: String,
        campaignLevel: Int,
    ): GameSave {
        val savedGameState =
            SavedGameState(
                boardWidth = gameState.board.width,
                boardHeight = gameState.board.height,
                playerCharacters = gameState.getPlayerCharacters(),
                enemyCharacters = gameState.getEnemyCharacters(),
                currentTurn = gameState.currentTurn,
                turnCount = gameState.turnCount,
                campaignProgress =
                    CampaignProgress(
                        currentChapter = campaignLevel,
                        totalBattlesWon = playerProfile?.totalBattlesWon ?: 0,
                    ),
            )

        return GameSave(
            playerName = playerName,
            campaignLevel = campaignLevel,
            totalPlayTime = playerProfile?.totalPlayTime ?: 0,
            gameState = savedGameState,
        )
    }

    private fun showLoadGameDialog() {
        lifecycleScope.launch {
            val saveFiles = saveGameManager.listSaveFiles()
            if (saveFiles.isEmpty()) {
                Toast.makeText(this@GameActivity, "No save files found", Toast.LENGTH_SHORT).show()
                return@launch
            }

            val fileNames =
                saveFiles
                    .map { "${it.playerName} - Level ${it.campaignLevel} ${if (it.isAutoSave) "(Auto)" else ""}" }
                    .toTypedArray()

            AlertDialog
                .Builder(this@GameActivity)
                .setTitle("Load Game")
                .setItems(fileNames) { _, which ->
                    val selectedSave = saveFiles[which]
                    loadGame(selectedSave.saveId)
                }.setNegativeButton("Cancel", null)
                .show()
        }
    }

    private fun showSettingsDialog() {
        val preferences = playerProfile?.preferences ?: GamePreferences()

        val items =
            arrayOf(
                "Music: ${if (preferences.musicEnabled) "On" else "Off"}",
                "Sound Effects: ${if (preferences.soundEffectsEnabled) "On" else "Off"}",
                "Auto-save: ${if (preferences.autoSaveEnabled) "On" else "Off"}",
                "Animation Speed: ${preferences.animationSpeed}x",
            )

        AlertDialog
            .Builder(this)
            .setTitle("Settings")
            .setItems(items) { _, which ->
                // Settings implementation would go here
                Toast.makeText(this, "Settings feature coming soon!", Toast.LENGTH_SHORT).show()
            }.setNegativeButton("Close", null)
            .show()
    }

    private fun confirmQuitToMenu() {
        AlertDialog
            .Builder(this)
            .setTitle("Quit to Menu")
            .setMessage("Any unsaved progress will be lost. Auto-save before quitting?")
            .setPositiveButton("Save & Quit") { _, _ ->
                performAutoSave()
                finish()
            }.setNeutralButton("Quit Without Saving") { _, _ ->
                finish()
            }.setNegativeButton("Cancel", null)
            .show()
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
                        gameState.gamePhase =
                            if (selectedCharacter.canAct) {
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
                        // Animate the attack
                        gameBoardView.animateAttack(selectedCharacter, targetCharacter) {
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
        turnsSinceLastSave++
        updateUI()

        if (gameState.currentTurn == Team.PLAYER) {
            Toast.makeText(this, "Turn ${gameState.turnCount}", Toast.LENGTH_SHORT).show()
        }

        // Check for auto-save
        val preferences = playerProfile?.preferences
        if (preferences?.autoSaveEnabled == true && turnsSinceLastSave >= preferences.autoSaveFrequency) {
            performAutoSave()
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
        val message =
            if (result.targetDefeated) {
                "${result.attacker.name} defeated ${result.target.name}! (${result.damage} damage)"
            } else {
                "${result.attacker.name} attacks ${result.target.name} for ${result.damage} damage!"
            }
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun checkGameEnd() {
        when {
            gameState.isGameWon() -> {
                // Update profile statistics
                playerProfile =
                    playerProfile?.copy(
                        totalBattlesWon = (playerProfile?.totalBattlesWon ?: 0) + 1,
                        campaignsCompleted = (playerProfile?.campaignsCompleted ?: 0) + 1,
                    )
                savePlayerProfile()

                AlertDialog
                    .Builder(this)
                    .setTitle("Victory!")
                    .setMessage("All enemies have been defeated! Your progress has been saved.")
                    .setPositiveButton("Continue") { _, _ ->
                        performManualSave()
                        finish()
                    }.show()
            }
            gameState.isGameLost() -> {
                AlertDialog
                    .Builder(this)
                    .setTitle("Defeat")
                    .setMessage("All your units have fallen... Your progress has been saved.")
                    .setPositiveButton("Retry") { _, _ ->
                        // Could reload from save here
                        finish()
                    }.show()
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
