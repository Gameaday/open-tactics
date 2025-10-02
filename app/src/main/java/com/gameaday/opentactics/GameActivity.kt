@file:Suppress("MagicNumber")

package com.gameaday.opentactics

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.gameaday.opentactics.data.CampaignProgress
import com.gameaday.opentactics.data.GamePreferences
import com.gameaday.opentactics.data.GameSave
import com.gameaday.opentactics.data.PlayerProfile
import com.gameaday.opentactics.data.SaveGameManager
import com.gameaday.opentactics.data.SavedGameState
import com.gameaday.opentactics.databinding.ActivityGameBinding
import com.gameaday.opentactics.game.GameState
import com.gameaday.opentactics.model.Character
import com.gameaday.opentactics.model.CharacterClass
import com.gameaday.opentactics.model.GameBoard
import com.gameaday.opentactics.model.Position
import com.gameaday.opentactics.model.Team
import com.gameaday.opentactics.model.Weapon
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
        const val EXTRA_CHAPTER_NUMBER = "chapter_number"
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
        val chapterNumber = intent.getIntExtra(EXTRA_CHAPTER_NUMBER, 1)

        if (isNewGame || loadSaveId == null) {
            initializeNewGame(playerName, chapterNumber)
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

    /**
     * Get weapon by equipment ID string
     */
    private fun getWeaponById(equipmentId: String): Weapon? =
        when (equipmentId) {
            "iron_sword" -> Weapon.ironSword()
            "steel_sword" -> Weapon.steelSword()
            "silver_sword" -> Weapon.silverSword()
            "iron_lance" -> Weapon.ironLance()
            "steel_lance" -> Weapon.steelLance()
            "iron_axe" -> Weapon.ironAxe()
            "steel_axe" -> Weapon.steelAxe()
            "iron_bow" -> Weapon.ironBow()
            "steel_bow" -> Weapon.steelBow()
            "fire_tome" -> Weapon.fire()
            "thunder_tome" -> Weapon.thunder()
            else -> null
        }

    @Suppress("LongMethod", "ComplexMethod") // Game initialization requires many character setups
    private fun initializeNewGame(playerName: String, chapterNumber: Int = 1) {
        // Load chapter data
        val chapter = com.gameaday.opentactics.model.ChapterRepository.getChapter(chapterNumber)
        if (chapter == null) {
            Toast.makeText(this, "Chapter $chapterNumber not found!", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        // Create game board based on chapter's map layout
        val board = when (chapter.mapLayout) {
            com.gameaday.opentactics.model.MapLayout.TEST_MAP -> GameBoard.createTestMap()
            com.gameaday.opentactics.model.MapLayout.FOREST_AMBUSH -> GameBoard.createTestMap() // TODO: Create specific maps
            com.gameaday.opentactics.model.MapLayout.MOUNTAIN_PASS -> GameBoard.createTestMap()
            com.gameaday.opentactics.model.MapLayout.CASTLE_SIEGE -> GameBoard.createTestMap()
            com.gameaday.opentactics.model.MapLayout.VILLAGE_DEFENSE -> GameBoard.createTestMap()
            else -> GameBoard.createTestMap()
        }

        gameState = GameState(board, currentChapter = chapter)

        // Show pre-battle dialogue if present
        if (chapter.preBattleDialogue.isNotEmpty()) {
            AlertDialog.Builder(this)
                .setTitle(chapter.title)
                .setMessage(chapter.preBattleDialogue)
                .setPositiveButton("Begin Battle", null)
                .show()
        }

        // Create player characters at starting positions
        val playerPositions = chapter.playerStartPositions
        val playerCharacters = listOf(
            Character(
                id = "player_knight",
                name = "Sir Garrett",
                characterClass = CharacterClass.KNIGHT,
                team = Team.PLAYER,
                position = playerPositions.getOrElse(0) { Position(1, 6) },
            ).apply {
                addWeapon(Weapon.ironSword())
                addWeapon(Weapon.steelSword())
            },
            Character(
                id = "player_archer",
                name = "Lyanna",
                characterClass = CharacterClass.ARCHER,
                team = Team.PLAYER,
                position = playerPositions.getOrElse(1) { Position(2, 7) },
            ).apply {
                addWeapon(Weapon.ironBow())
                addWeapon(Weapon.steelBow())
            },
            Character(
                id = "player_mage",
                name = "Aldric",
                characterClass = CharacterClass.MAGE,
                team = Team.PLAYER,
                position = playerPositions.getOrElse(2) { Position(0, 7) },
            ).apply {
                addWeapon(Weapon.fire())
                addWeapon(Weapon.thunder())
            },
        )

        // Add player characters
        playerCharacters.forEach { char ->
            gameState.addPlayerCharacter(char)
            board.placeCharacter(char, char.position)
        }

        // Create enemy characters from chapter data
        chapter.enemyUnits.forEach { enemySpawn ->
            val enemyChar = Character(
                id = enemySpawn.id,
                name = enemySpawn.name,
                characterClass = enemySpawn.characterClass,
                team = Team.ENEMY,
                position = enemySpawn.position,
                level = enemySpawn.level,
            ).apply {
                // Add weapons from equipment list
                enemySpawn.equipment.forEach { equipId ->
                    getWeaponById(equipId)?.let { weapon -> addWeapon(weapon) }
                }
            }
            gameState.addEnemyCharacter(enemyChar)
            board.placeCharacter(enemyChar, enemyChar.position)
        }

        // Add boss unit if present
        chapter.bossUnit?.let { bossSpawn ->
            val bossChar = Character(
                id = bossSpawn.id,
                name = bossSpawn.name,
                characterClass = bossSpawn.characterClass,
                team = Team.ENEMY,
                position = bossSpawn.position,
                level = bossSpawn.level,
            ).apply {
                bossSpawn.equipment.forEach { equipId ->
                    getWeaponById(equipId)?.let { weapon -> addWeapon(weapon) }
                }
            }
            gameState.addEnemyCharacter(bossChar)
            board.placeCharacter(bossChar, bossChar.position)
        }

        // Create initial save data
        currentGameSave = createGameSave(playerName, chapterNumber)
    }

    @Suppress("LongMethod") // Game initialization requires many character setups - DEPRECATED
    private fun initializeNewGameOld(playerName: String) {
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
            ).apply {
                addWeapon(Weapon.ironSword())
                addWeapon(Weapon.steelSword())
            }

        val archer =
            Character(
                id = "player_archer",
                name = "Lyanna",
                characterClass = CharacterClass.ARCHER,
                team = Team.PLAYER,
                position = Position(2, 7),
            ).apply {
                addWeapon(Weapon.ironBow())
                addWeapon(Weapon.steelBow())
            }

        val mage =
            Character(
                id = "player_mage",
                name = "Aldric",
                characterClass = CharacterClass.MAGE,
                team = Team.PLAYER,
                position = Position(0, 7),
            ).apply {
                addWeapon(Weapon.fire())
                addWeapon(Weapon.thunder())
            }

        // Create enemy characters
        val enemyKnight =
            Character(
                id = "enemy_knight",
                name = "Dark Knight",
                characterClass = CharacterClass.KNIGHT,
                team = Team.ENEMY,
                position = Position(10, 1),
            ).apply {
                addWeapon(Weapon.ironSword())
            }

        val enemyArcher =
            Character(
                id = "enemy_archer",
                name = "Bandit Archer",
                characterClass = CharacterClass.ARCHER,
                team = Team.ENEMY,
                position = Position(9, 2),
            ).apply {
                addWeapon(Weapon.ironBow())
            }

        val enemyThief =
            Character(
                id = "enemy_thief",
                name = "Rogue",
                characterClass = CharacterClass.THIEF,
                team = Team.ENEMY,
                position = Position(11, 0),
            ).apply {
                addWeapon(Weapon.ironSword())
            }

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

        // Setup chapter objective HUD
        updateChapterObjectiveHUD()
    }

    private fun updateChapterObjectiveHUD() {
        val chapter = gameState.currentChapter
        if (chapter != null) {
            binding.chapterTitle.text = "Ch${chapter.number}: ${chapter.title}"
            binding.chapterObjective.text = chapter.objectiveDetails
            binding.chapterObjectivePanel.visibility = android.view.View.VISIBLE
        } else {
            binding.chapterObjectivePanel.visibility = android.view.View.GONE
        }
        binding.turnCounter.text = "Turn: ${gameState.turnCount}"
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
                        val message = "Failed to save game: ${error.message}"
                        Toast.makeText(this@GameActivity, message, Toast.LENGTH_LONG).show()
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
                        // Show battle forecast before attacking
                        showBattleForecast(selectedCharacter, targetCharacter)
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
            
            // Check game end conditions at start of player turn
            checkGameEnd()
        } else if (gameState.currentTurn == Team.ENEMY) {
            // Start enemy turn automation
            executeEnemyTurn()
        }

        // Check for auto-save
        val preferences = playerProfile?.preferences
        if (preferences?.autoSaveEnabled == true && turnsSinceLastSave >= preferences.autoSaveFrequency) {
            performAutoSave()
        }
    }

    private fun executeEnemyTurn() {
        // Show enemy turn banner
        Toast.makeText(this, "Enemy Turn", Toast.LENGTH_SHORT).show()
        
        // Disable UI during enemy turn
        setUIEnabled(false)
        
        // Process all enemy units with a delay between actions
        val enemies = gameState.getAliveEnemyCharacters()
        var delayMs = 500L
        
        enemies.forEach { enemy ->
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                if (enemy.isAlive && !enemy.hasActedThisTurn) {
                    // Execute AI behavior
                    gameState.executeEnemyAction(enemy)
                    
                    // Refresh UI
                    gameBoardView.invalidate()
                    updateUI()
                }
            }, delayMs)
            delayMs += 800L // 800ms delay between enemy actions
        }
        
        // End enemy turn after all actions complete
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            gameState.endTurn()
            setUIEnabled(true)
            updateUI()
            checkGameEnd()
            Toast.makeText(this, "Player Turn ${gameState.turnCount}", Toast.LENGTH_SHORT).show()
        }, delayMs + 500)
    }
    
    private fun setUIEnabled(enabled: Boolean) {
        binding.btnMove.isEnabled = enabled
        binding.btnAttack.isEnabled = enabled
        binding.btnWait.isEnabled = enabled
        binding.btnEndTurn.isEnabled = enabled
    }

    private fun showCharacterInfo(character: Character) {
        binding.characterInfoPanel.visibility = android.view.View.VISIBLE
        binding.characterName.text = "${character.name} (${character.characterClass.displayName})"
        binding.characterStats.text = character.currentStats.toDisplayString()
    }

    private fun hideCharacterInfo() {
        binding.characterInfoPanel.visibility = android.view.View.GONE
    }

    private fun showBattleForecast(attacker: Character, target: Character) {
        val forecast = gameState.calculateBattleForecast(attacker, target)
        
        // Create dialog with battle forecast layout
        val dialogView = layoutInflater.inflate(R.layout.dialog_battle_forecast, null)
        
        // Populate attacker info
        dialogView.findViewById<android.widget.TextView>(R.id.attackerName).text = attacker.name
        dialogView.findViewById<android.widget.TextView>(R.id.attackerStats).text = 
            "HP: ${attacker.currentStats.hp}/${attacker.maxHp}"
        dialogView.findViewById<android.widget.TextView>(R.id.attackerDamage).text = 
            "Damage: ${forecast.attackerDamage}"
        dialogView.findViewById<android.widget.TextView>(R.id.attackerHit).text = 
            "Hit: ${forecast.attackerHitRate}%"
        dialogView.findViewById<android.widget.TextView>(R.id.attackerDoubles).visibility =
            if (forecast.attackerDoubles) android.view.View.VISIBLE else android.view.View.GONE
        
        // Populate target info
        dialogView.findViewById<android.widget.TextView>(R.id.targetName).text = target.name
        dialogView.findViewById<android.widget.TextView>(R.id.targetStats).text = 
            "HP: ${target.currentStats.hp}/${target.maxHp}"
        dialogView.findViewById<android.widget.TextView>(R.id.targetDamage).text = 
            if (forecast.canCounter) "Damage: ${forecast.targetDamage}" else "Cannot Counter"
        dialogView.findViewById<android.widget.TextView>(R.id.targetHit).text = 
            if (forecast.canCounter) "Hit: ${forecast.targetHitRate}%" else ""
        dialogView.findViewById<android.widget.TextView>(R.id.targetDoubles).visibility =
            if (forecast.targetDoubles) android.view.View.VISIBLE else android.view.View.GONE
        
        // Populate result prediction
        val resultText = "After: ${forecast.predictedAttackerHp} ← → ${forecast.predictedTargetHp} HP"
        dialogView.findViewById<android.widget.TextView>(R.id.forecastResult).text = resultText
        
        // Create and show dialog
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true)
            .create()
        
        // Setup buttons
        dialogView.findViewById<android.widget.Button>(R.id.btnConfirmAttack).setOnClickListener {
            dialog.dismiss()
            // Execute the attack
            gameBoardView.animateAttack(attacker, target) {
                val result = gameState.performAttack(attacker, target)
                showBattleResult(result)
                attacker.hasActedThisTurn = true
                gameState.selectCharacter(null)
                gameBoardView.clearHighlights()
                checkGameEnd()
                updateUI()
            }
        }
        
        dialogView.findViewById<android.widget.Button>(R.id.btnCancelAttack).setOnClickListener {
            dialog.dismiss()
            // Stay in action phase, allow selecting different target
        }
        
        dialog.show()
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
        val chapter = gameState.currentChapter

        // Check chapter objectives if chapter is set
        if (chapter != null) {
            val isVictory = chapter.isObjectiveComplete(
                gameState.getPlayerCharacters(),
                gameState.getEnemyCharacters(),
                gameState.turnCount,
                emptyList(), // TODO: Track throne units
                gameState.escapedUnitCount
            )

            val isDefeat = chapter.isObjectiveFailed(
                gameState.getPlayerCharacters(),
                gameState.turnCount
            )

            when {
                isVictory -> showVictoryScreen(chapter)
                isDefeat -> showDefeatScreen(chapter)
            }
        } else {
            // Fallback to old victory/defeat logic
            when {
                gameState.isGameWon() -> {
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
                            finish()
                        }.show()
                }
            }
        }
    }

    private fun showVictoryScreen(chapter: com.gameaday.opentactics.model.Chapter) {
        // Update profile statistics
        playerProfile =
            playerProfile?.copy(
                totalBattlesWon = (playerProfile?.totalBattlesWon ?: 0) + 1,
            )
        savePlayerProfile()

        // Build victory message
        val message = buildString {
            append("Chapter ${chapter.number} Complete!\n\n")
            if (chapter.postVictoryDialogue.isNotEmpty()) {
                append(chapter.postVictoryDialogue)
                append("\n\n")
            }
            append("Objective: ${chapter.objectiveDetails}\n")
            append("Turns: ${gameState.turnCount}\n")
            append("Units: ${gameState.getAlivePlayerCharacters().size}/${gameState.getPlayerCharacters().size} survived")
        }

        val hasNextChapter = com.gameaday.opentactics.model.ChapterRepository.getChapter(chapter.number + 1) != null

        AlertDialog
            .Builder(this)
            .setTitle("⭐ Victory! ⭐")
            .setMessage(message)
            .setPositiveButton(if (hasNextChapter) "Next Chapter" else "Finish") { _, _ ->
                performManualSave()
                if (hasNextChapter) {
                    // Start next chapter
                    startNextChapter(chapter.number + 1)
                } else {
                    // Campaign complete
                    showCampaignComplete()
                }
            }
            .setNegativeButton("Return to Menu") { _, _ ->
                performManualSave()
                finish()
            }
            .setCancelable(false)
            .show()
    }

    private fun showDefeatScreen(chapter: com.gameaday.opentactics.model.Chapter) {
        val message = buildString {
            append("Chapter ${chapter.number} Failed\n\n")
            if (chapter.postDefeatDialogue.isNotEmpty()) {
                append(chapter.postDefeatDialogue)
                append("\n\n")
            } else {
                append("All your units have fallen...\n\n")
            }
            append("Would you like to retry?")
        }

        AlertDialog
            .Builder(this)
            .setTitle("💀 Defeat 💀")
            .setMessage(message)
            .setPositiveButton("Retry Chapter") { _, _ ->
                // Restart current chapter
                recreate()
            }
            .setNegativeButton("Return to Menu") { _, _ ->
                finish()
            }
            .setCancelable(false)
            .show()
    }

    private fun startNextChapter(nextChapterNumber: Int) {
        val intent =
            Intent(this, GameActivity::class.java).apply {
                putExtra(EXTRA_PLAYER_NAME, currentGameSave?.playerName ?: "Player")
                putExtra(EXTRA_IS_NEW_GAME, true)
                putExtra(EXTRA_CHAPTER_NUMBER, nextChapterNumber)
            }
        startActivity(intent)
        finish()
    }

    private fun showCampaignComplete() {
        playerProfile =
            playerProfile?.copy(
                campaignsCompleted = (playerProfile?.campaignsCompleted ?: 0) + 1,
            )
        savePlayerProfile()

        AlertDialog
            .Builder(this)
            .setTitle("🎉 Campaign Complete! 🎉")
            .setMessage("Congratulations! You have completed all chapters!\n\nThank you for playing Open Tactics!")
            .setPositiveButton("Return to Menu") { _, _ ->
                finish()
            }
            .setCancelable(false)
            .show()
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

        // Update turn counter
        binding.turnCounter.text = "Turn: ${gameState.turnCount}"

        gameBoardView.invalidate()
    }
}
