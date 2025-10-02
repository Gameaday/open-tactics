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
import com.gameaday.opentactics.model.Stats
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

    // Undo move state
    private var previousPosition: Position? = null
    private var canUndoMove: Boolean = false

    // Skip enemy turn state
    private var skipEnemyAnimations: Boolean = false

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
        val chapter =
            com.gameaday.opentactics.model.ChapterRepository
                .getChapter(chapterNumber)
        if (chapter == null) {
            Toast.makeText(this, "Chapter $chapterNumber not found!", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        // Create game board based on chapter's map layout
        val board =
            when (chapter.mapLayout) {
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
            AlertDialog
                .Builder(this)
                .setTitle(chapter.title)
                .setMessage(chapter.preBattleDialogue)
                .setPositiveButton("Begin Battle", null)
                .show()
        }

        // Create player characters at starting positions
        val playerPositions = chapter.playerStartPositions
        val playerCharacters =
            listOf(
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
            val enemyChar =
                Character(
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
            val bossChar =
                Character(
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

        // Check if we're loading into a new chapter
        val chapterNumber = intent.getIntExtra(EXTRA_CHAPTER_NUMBER, gameSave.campaignLevel)
        val chapter =
            com.gameaday.opentactics.model.ChapterRepository
                .getChapter(chapterNumber)

        gameState = GameState(board, currentChapter = chapter)

        // Restore player characters (they carry over between chapters)
        val playerStartPositions = chapter?.playerStartPositions ?: listOf()
        savedState.playerCharacters.forEachIndexed { index, character ->
            // Update position to new chapter's start position if available
            val newPosition = playerStartPositions.getOrNull(index) ?: character.position
            character.position = newPosition
            character.hasMovedThisTurn = false
            character.hasActedThisTurn = false

            gameState.addPlayerCharacter(character)
            board.placeCharacter(character, newPosition)
        }

        // If loading a new chapter (no enemies saved), spawn new enemies from chapter data
        if (chapter != null && savedState.enemyCharacters.isEmpty()) {
            chapter.enemyUnits.forEach { enemySpawn ->
                val weapon = getWeaponById(enemySpawn.equipment.firstOrNull() ?: "iron_sword")
                val enemy =
                    Character(
                        id = enemySpawn.id,
                        name = enemySpawn.name,
                        characterClass = enemySpawn.characterClass,
                        team = Team.ENEMY,
                        level = enemySpawn.level,
                        position = enemySpawn.position,
                        isBoss = enemySpawn.isBoss,
                        aiType = enemySpawn.aiType,
                    ).apply {
                        weapon?.let { addWeapon(it) }
                    }
                gameState.addEnemyCharacter(enemy)
                board.placeCharacter(enemy, enemy.position)
            }
        } else {
            // Restore existing enemies (from saved game mid-chapter)
            savedState.enemyCharacters.forEach { character ->
                gameState.addEnemyCharacter(character)
                board.placeCharacter(character, character.position)
            }
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
        binding.btnInventory.setOnClickListener { handleInventoryAction() }
        binding.btnUndo.setOnClickListener { handleUndoAction() }
        binding.btnEndTurn.setOnClickListener { handleEndTurnAction() }

        // Range display toggle
        var showingRanges = false
        binding.btnToggleRanges.setOnClickListener {
            showingRanges = !showingRanges
            if (showingRanges) {
                showEnemyRanges()
            } else {
                gameBoardView.clearHighlights()
            }
        }

        // Skip enemy turn animations
        binding.btnSkipEnemyTurn.setOnClickListener {
            skipEnemyAnimations = true
        }

        // Add save/load buttons to the overflow menu
        binding.root.setOnLongClickListener {
            showGameMenu()
            true
        }

        updateUI()
    }

    private fun handleUndoAction() {
        val selectedCharacter = gameState.selectedCharacter
        val prevPos = previousPosition

        if (selectedCharacter != null && prevPos != null && canUndoMove) {
            // Undo the move
            gameState.board.removeCharacter(selectedCharacter)
            gameState.board.placeCharacter(selectedCharacter, prevPos)
            selectedCharacter.position = prevPos
            selectedCharacter.hasMovedThisTurn = false

            canUndoMove = false
            previousPosition = null
            binding.btnUndo.visibility = android.view.View.GONE

            gameBoardView.invalidate()
            updateUI()

            Toast.makeText(this, "Move undone", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleInventoryAction() {
        val selectedCharacter = gameState.selectedCharacter
        if (selectedCharacter != null) {
            showInventoryDialog(selectedCharacter)
        } else {
            Toast.makeText(this, "Select a unit first", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showInventoryDialog(character: Character) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_inventory, null)
        val recyclerView = dialogView.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.weaponRecyclerView)

        dialogView.findViewById<android.widget.TextView>(R.id.characterNameInventory).text =
            "${character.name} - ${character.characterClass.displayName}"

        val adapter =
            WeaponAdapter(character) { weapon, action ->
                when (action) {
                    "equip" -> {
                        character.equipWeapon(weapon)
                        Toast.makeText(this, "${weapon.name} equipped", Toast.LENGTH_SHORT).show()
                        updateUI()
                    }
                    "drop" -> {
                        if (character.inventory.size > 1) {
                            character.removeWeapon(weapon)
                            Toast.makeText(this, "${weapon.name} dropped", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this, "Cannot drop last weapon", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }

        recyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)
        recyclerView.adapter = adapter

        val dialog =
            AlertDialog
                .Builder(this)
                .setView(dialogView)
                .create()

        dialogView.findViewById<android.widget.Button>(R.id.btnCloseInventory).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private inner class WeaponAdapter(
        private val character: Character,
        private val onAction: (Weapon, String) -> Unit,
    ) : androidx.recyclerview.widget.RecyclerView.Adapter<WeaponAdapter.WeaponViewHolder>() {
        override fun onCreateViewHolder(
            parent: android.view.ViewGroup,
            viewType: Int,
        ): WeaponViewHolder {
            val view = layoutInflater.inflate(R.layout.item_weapon, parent, false)
            return WeaponViewHolder(view)
        }

        override fun onBindViewHolder(
            holder: WeaponViewHolder,
            position: Int,
        ) {
            val weapon = character.inventory[position]
            holder.bind(weapon, character.equippedWeapon == weapon)
        }

        override fun getItemCount(): Int = character.inventory.size

        inner class WeaponViewHolder(
            view: android.view.View,
        ) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {
            fun bind(
                weapon: Weapon,
                isEquipped: Boolean,
            ) {
                itemView.findViewById<android.widget.TextView>(R.id.weaponName).text = weapon.name
                itemView.findViewById<android.widget.TextView>(R.id.weaponType).text = weapon.type.name
                itemView.findViewById<android.widget.TextView>(R.id.weaponMight).text = "Mt: ${weapon.might}"
                itemView.findViewById<android.widget.TextView>(R.id.weaponDurability).text =
                    "Uses: ${weapon.currentUses}/${weapon.maxUses}"

                val equippedLabel = itemView.findViewById<android.widget.TextView>(R.id.weaponEquipped)
                equippedLabel.visibility = if (isEquipped) android.view.View.VISIBLE else android.view.View.GONE

                val btnEquip = itemView.findViewById<android.widget.Button>(R.id.btnEquipWeapon)
                btnEquip.isEnabled = !isEquipped
                btnEquip.setOnClickListener {
                    onAction(weapon, "equip")
                    notifyDataSetChanged()
                }

                val btnDrop = itemView.findViewById<android.widget.Button>(R.id.btnDropWeapon)
                btnDrop.setOnClickListener {
                    onAction(weapon, "drop")
                    notifyDataSetChanged()
                }
            }
        }
    }

    private fun showEnemyRanges() {
        val enemyRanges = mutableSetOf<Position>()
        gameState.getAliveEnemyCharacters().forEach { enemy ->
            val targets = gameState.calculateAttackTargets(enemy)
            enemyRanges.addAll(targets.map { it.position })
        }
        gameBoardView.highlightEnemyRanges(enemyRanges.toList())
    }

    private fun showGameMenu() {
        val options = arrayOf("Save Game", "Load Game", "Settings", "Help", "Quit to Menu")

        AlertDialog
            .Builder(this)
            .setTitle("Game Menu")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> performManualSave()
                    1 -> showLoadGameDialog()
                    2 -> showSettingsDialog()
                    3 -> showHelpDialog()
                    4 -> confirmQuitToMenu()
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
                "Auto-save Frequency: Every ${preferences.autoSaveFrequency} turns",
            )

        AlertDialog
            .Builder(this)
            .setTitle("Settings")
            .setItems(items) { _, which ->
                when (which) {
                    0 -> toggleMusicSetting()
                    1 -> toggleSoundEffectsSetting()
                    2 -> toggleAutoSave()
                    3 -> changeAnimationSpeed()
                    4 -> changeAutoSaveFrequency()
                }
            }.setNegativeButton("Close", null)
            .show()
    }

    private fun toggleMusicSetting() {
        val currentPrefs = playerProfile?.preferences ?: GamePreferences()
        val newPrefs = currentPrefs.copy(musicEnabled = !currentPrefs.musicEnabled)
        updatePreferences(newPrefs)
        Toast
            .makeText(
                this,
                "Music ${if (newPrefs.musicEnabled) "enabled" else "disabled"}",
                Toast.LENGTH_SHORT,
            ).show()
        showSettingsDialog() // Refresh dialog
    }

    private fun toggleSoundEffectsSetting() {
        val currentPrefs = playerProfile?.preferences ?: GamePreferences()
        val newPrefs = currentPrefs.copy(soundEffectsEnabled = !currentPrefs.soundEffectsEnabled)
        updatePreferences(newPrefs)
        Toast
            .makeText(
                this,
                "Sound effects ${if (newPrefs.soundEffectsEnabled) "enabled" else "disabled"}",
                Toast.LENGTH_SHORT,
            ).show()
        showSettingsDialog()
    }

    private fun changeAnimationSpeed() {
        val currentPrefs = playerProfile?.preferences ?: GamePreferences()
        val speeds = arrayOf("0.5x", "1.0x", "1.5x", "2.0x")
        val currentIndex =
            when (currentPrefs.animationSpeed) {
                0.5f -> 0
                1.0f -> 1
                1.5f -> 2
                2.0f -> 3
                else -> 1
            }

        AlertDialog
            .Builder(this)
            .setTitle("Animation Speed")
            .setSingleChoiceItems(speeds, currentIndex) { dialog, which ->
                val newSpeed =
                    when (which) {
                        0 -> 0.5f
                        1 -> 1.0f
                        2 -> 1.5f
                        3 -> 2.0f
                        else -> 1.0f
                    }
                val newPrefs = currentPrefs.copy(animationSpeed = newSpeed)
                updatePreferences(newPrefs)
                Toast.makeText(this, "Animation speed set to ${newSpeed}x", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
                showSettingsDialog()
            }.setNegativeButton("Cancel") { _, _ -> showSettingsDialog() }
            .show()
    }

    private fun toggleAutoSave() {
        val currentPrefs = playerProfile?.preferences ?: GamePreferences()
        val newPrefs = currentPrefs.copy(autoSaveEnabled = !currentPrefs.autoSaveEnabled)
        updatePreferences(newPrefs)
        Toast
            .makeText(
                this,
                "Auto-save ${if (newPrefs.autoSaveEnabled) "enabled" else "disabled"}",
                Toast.LENGTH_SHORT,
            ).show()
        showSettingsDialog()
    }

    private fun changeAutoSaveFrequency() {
        val currentPrefs = playerProfile?.preferences ?: GamePreferences()
        val frequencies = arrayOf("Every 3 turns", "Every 5 turns", "Every 10 turns")
        val currentIndex =
            when (currentPrefs.autoSaveFrequency) {
                3 -> 0
                5 -> 1
                10 -> 2
                else -> 1
            }

        AlertDialog
            .Builder(this)
            .setTitle("Auto-save Frequency")
            .setSingleChoiceItems(frequencies, currentIndex) { dialog, which ->
                val newFrequency =
                    when (which) {
                        0 -> 3
                        1 -> 5
                        2 -> 10
                        else -> 5
                    }
                val newPrefs = currentPrefs.copy(autoSaveFrequency = newFrequency)
                updatePreferences(newPrefs)
                Toast.makeText(this, "Auto-save every $newFrequency turns", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
                showSettingsDialog()
            }.setNegativeButton("Cancel") { _, _ -> showSettingsDialog() }
            .show()
    }

    private fun updatePreferences(newPrefs: GamePreferences) {
        playerProfile = playerProfile?.copy(preferences = newPrefs)
        savePlayerProfile()
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
                } else {
                    // Show terrain info when clicking empty tile
                    showTerrainTooltip(position)
                }
            }
            GameState.GamePhase.MOVEMENT -> {
                val selectedCharacter = gameState.selectedCharacter
                if (selectedCharacter != null) {
                    val possibleMoves = gameState.calculatePossibleMoves(selectedCharacter)
                    if (position in possibleMoves) {
                        // Store previous position for undo
                        previousPosition = selectedCharacter.position
                        canUndoMove = true
                        binding.btnUndo.visibility = android.view.View.VISIBLE

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
                    val targetCharacter = gameState.board.getCharacterAt(position)
                    if (targetCharacter != null) {
                        // Check if using a healing staff
                        val weapon = selectedCharacter.equippedWeapon
                        if (weapon != null && weapon.canHeal) {
                            // Handle healing action
                            val healTargets = gameState.calculateHealTargets(selectedCharacter)
                            if (targetCharacter in healTargets) {
                                showHealConfirmation(selectedCharacter, targetCharacter)
                            }
                        } else {
                            // Handle attack action
                            val attackTargets = gameState.calculateAttackTargets(selectedCharacter)
                            if (targetCharacter in attackTargets) {
                                // Show battle forecast before attacking
                                showBattleForecast(selectedCharacter, targetCharacter)
                            }
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
            // Check if character has a healing staff equipped
            val weapon = selectedCharacter.equippedWeapon
            if (weapon != null && weapon.canHeal) {
                // Show heal targets
                val targets = gameState.calculateHealTargets(selectedCharacter)
                gameBoardView.highlightAttacks(targets.map { it.position })
                gameState.gamePhase = GameState.GamePhase.ACTION
                updateUI()
            } else {
                // Show attack targets
                val targets = gameState.calculateAttackTargets(selectedCharacter)
                gameBoardView.highlightAttacks(targets.map { it.position })
                gameState.gamePhase = GameState.GamePhase.ACTION
                updateUI()
            }
        }
    }

    private fun handleWaitAction() {
        val selectedCharacter = gameState.selectedCharacter
        if (selectedCharacter != null) {
            selectedCharacter.hasActedThisTurn = true
            selectedCharacter.hasMovedThisTurn = true
            gameState.selectCharacter(null)
            gameBoardView.clearHighlights()

            // Disable undo after action
            canUndoMove = false
            previousPosition = null
            binding.btnUndo.visibility = android.view.View.GONE

            updateUI()
        }
    }

    private fun handleEndTurnAction() {
        // Disable undo when ending turn
        canUndoMove = false
        previousPosition = null
        binding.btnUndo.visibility = android.view.View.GONE

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

        // Show skip button
        binding.btnSkipEnemyTurn.visibility = android.view.View.VISIBLE
        skipEnemyAnimations = false

        // Disable UI during enemy turn
        setUIEnabled(false)

        // Process all enemy units with a delay between actions
        val enemies = gameState.getAliveEnemyCharacters()
        val delayBetweenActions = if (skipEnemyAnimations) 50L else 800L
        var delayMs = if (skipEnemyAnimations) 100L else 500L

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
            delayMs += delayBetweenActions
        }

        // End enemy turn after all actions complete
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            binding.btnSkipEnemyTurn.visibility = android.view.View.GONE
            skipEnemyAnimations = false
            gameState.endTurn()
            setUIEnabled(true)
            updateUI()

            // Check for reinforcements at start of player turn
            spawnReinforcements()

            checkGameEnd()
            Toast.makeText(this, "Player Turn ${gameState.turnCount}", Toast.LENGTH_SHORT).show()
        }, delayMs + 500)
    }

    private fun spawnReinforcements() {
        val chapter = gameState.currentChapter ?: return
        val reinforcements = chapter.getReinforcementsForTurn(gameState.turnCount)

        if (reinforcements.isNotEmpty()) {
            Toast
                .makeText(
                    this,
                    "Enemy reinforcements have arrived!",
                    Toast.LENGTH_LONG,
                ).show()

            reinforcements.forEach { enemySpawn ->
                val weapon = getWeaponById(enemySpawn.equipment.firstOrNull() ?: "iron_sword")
                val reinforcement =
                    Character(
                        id = "${enemySpawn.id}_turn${gameState.turnCount}",
                        name = enemySpawn.name,
                        characterClass = enemySpawn.characterClass,
                        team = Team.ENEMY,
                        level = enemySpawn.level,
                        position = enemySpawn.position,
                        isBoss = enemySpawn.isBoss,
                        aiType = enemySpawn.aiType,
                    ).apply {
                        weapon?.let { addWeapon(it) }
                    }

                gameState.addEnemyCharacter(reinforcement)
                gameState.board.placeCharacter(reinforcement, reinforcement.position)

                // Animate the spawn with a flash effect
                gameBoardView.invalidate()
            }

            // Show visual feedback
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                gameBoardView.invalidate()
            }, 300)
        }
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

    private fun showBattleForecast(
        attacker: Character,
        target: Character,
    ) {
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
        val dialog =
            AlertDialog
                .Builder(this)
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

                // Disable undo after attacking
                canUndoMove = false
                previousPosition = null
                binding.btnUndo.visibility = android.view.View.GONE

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
        val criticalText = if (result.wasCritical) " CRITICAL HIT! ⚡" else ""
        val message =
            if (result.targetDefeated) {
                "${result.attacker.name} defeated ${result.target.name}!$criticalText (${result.damage} damage)"
            } else {
                "${result.attacker.name} attacks ${result.target.name} for ${result.damage} damage!$criticalText"
            }
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()

        // Show EXP gain if attacker is a player unit
        // Note: EXP is already awarded in GameState.performAttack()
        if (result.attacker.team == Team.PLAYER && result.expGained > 0) {
            val previousLevel = result.previousLevel
            val newLevel = result.attacker.level

            // Show EXP gain effect
            showExpGainEffect(result.attacker, result.expGained)

            // Check for level up
            if (newLevel > previousLevel && result.statGains != null) {
                showLevelUpEffect(result.attacker, previousLevel, newLevel, result.statGains)
            }
        }
    }

    private fun showHealConfirmation(
        healer: Character,
        target: Character,
    ) {
        val staff = healer.equippedWeapon ?: return
        val healAmount =
            when (staff.name) {
                "Heal" -> 10
                "Mend" -> 20
                else -> 10
            }

        val message =
            buildString {
                append("${healer.name} will heal ${target.name}\n\n")
                append("Staff: ${staff.name}\n")
                append("Heal Amount: $healAmount HP\n")
                append("Current HP: ${target.currentHp}/${target.maxHp}\n")
                append("After: ${minOf(target.currentHp + healAmount, target.maxHp)}/${target.maxHp}")
            }

        AlertDialog
            .Builder(this)
            .setTitle("Use Staff")
            .setMessage(message)
            .setPositiveButton("Heal") { _, _ ->
                // Execute the healing
                val result = gameState.performHeal(healer, target)
                showSupportResult(result)
                healer.hasActedThisTurn = true
                gameState.selectCharacter(null)
                gameBoardView.clearHighlights()

                // Disable undo after action
                canUndoMove = false
                previousPosition = null
                binding.btnUndo.visibility = android.view.View.GONE

                checkGameEnd()
                updateUI()
            }.setNegativeButton("Cancel", null)
            .show()
    }

    private fun showSupportResult(result: com.gameaday.opentactics.game.SupportResult) {
        val message = "${result.user.name} healed ${result.target.name} for ${result.healAmount} HP!"
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()

        // Show EXP gain if user is a player unit
        if (result.user.team == Team.PLAYER && result.expGained > 0) {
            val previousLevel = result.previousLevel
            val newLevel = result.user.level

            // Show EXP gain effect
            showExpGainEffect(result.user, result.expGained)

            // Check for level up
            if (newLevel > previousLevel && result.statGains != null) {
                showLevelUpEffect(result.user, previousLevel, newLevel, result.statGains)
            }
        }
    }

    private fun showExpGainEffect(
        character: Character,
        expGained: Int,
    ) {
        // Show floating text animation for EXP gain
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            Toast
                .makeText(
                    this,
                    "${character.name} gained $expGained EXP! (${character.experience}/${character.level * 100})",
                    Toast.LENGTH_SHORT,
                ).show()
        }, 500)
    }

    private fun showLevelUpEffect(
        character: Character,
        oldLevel: Int,
        newLevel: Int,
        statGains: Stats,
    ) {
        // Show level up dialog with stat increases
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            val oldStats = character.currentStats - statGains
            val newStats = character.currentStats

            val levelUpMessage =
                buildString {
                    append("${character.name} reached Level $newLevel!\n\n")

                    // Show stat changes with arrows
                    if (statGains.hp > 0) {
                        append("HP: ${oldStats.hp}→${newStats.hp}\n")
                    } else {
                        append("HP: ${newStats.hp}\n")
                    }

                    if (statGains.mp > 0) {
                        append("MP: ${oldStats.mp}→${newStats.mp}\n")
                    } else {
                        append("MP: ${newStats.mp}\n")
                    }

                    if (statGains.attack > 0) {
                        append("ATK: ${oldStats.attack}→${newStats.attack}\n")
                    } else {
                        append("ATK: ${newStats.attack}\n")
                    }

                    if (statGains.defense > 0) {
                        append("DEF: ${oldStats.defense}→${newStats.defense}\n")
                    } else {
                        append("DEF: ${newStats.defense}\n")
                    }

                    if (statGains.speed > 0) {
                        append("SPD: ${oldStats.speed}→${newStats.speed}\n")
                    } else {
                        append("SPD: ${newStats.speed}\n")
                    }

                    if (statGains.skill > 0) {
                        append("SKL: ${oldStats.skill}→${newStats.skill}\n")
                    } else {
                        append("SKL: ${newStats.skill}\n")
                    }

                    if (statGains.luck > 0) {
                        append("LCK: ${oldStats.luck}→${newStats.luck}")
                    } else {
                        append("LCK: ${newStats.luck}")
                    }
                }

            AlertDialog
                .Builder(this)
                .setTitle("⭐ Level Up! ⭐")
                .setMessage(levelUpMessage)
                .setPositiveButton("OK") { _, _ ->
                    updateUI()
                }.setCancelable(false)
                .show()
        }, 1000)
    }

    private fun checkGameEnd() {
        val chapter = gameState.currentChapter

        // Check chapter objectives if chapter is set
        if (chapter != null) {
            val isVictory =
                chapter.isObjectiveComplete(
                    gameState.getPlayerCharacters(),
                    gameState.getEnemyCharacters(),
                    gameState.turnCount,
                    emptyList(), // TODO: Track throne units
                    gameState.escapedUnitCount,
                )

            val isDefeat =
                chapter.isObjectiveFailed(
                    gameState.getPlayerCharacters(),
                    gameState.turnCount,
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
        val message =
            buildString {
                append("Chapter ${chapter.number} Complete!\n\n")
                if (chapter.postVictoryDialogue.isNotEmpty()) {
                    append(chapter.postVictoryDialogue)
                    append("\n\n")
                }
                append("Objective: ${chapter.objectiveDetails}\n")
                append("Turns: ${gameState.turnCount}\n")
                append("Units: ${gameState.getAlivePlayerCharacters().size}/${gameState.getPlayerCharacters().size} survived")
            }

        val hasNextChapter =
            com.gameaday.opentactics.model.ChapterRepository
                .getChapter(chapter.number + 1) != null

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
            }.setNegativeButton("Return to Menu") { _, _ ->
                performManualSave()
                finish()
            }.setCancelable(false)
            .show()
    }

    private fun showDefeatScreen(chapter: com.gameaday.opentactics.model.Chapter) {
        val message =
            buildString {
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
            }.setNegativeButton("Return to Menu") { _, _ ->
                finish()
            }.setCancelable(false)
            .show()
    }

    private fun startNextChapter(nextChapterNumber: Int) {
        // Save current player characters to carry over
        val carriedCharacters = gameState.getAlivePlayerCharacters()

        // Create a save that will be loaded in the next chapter
        val carryOverSave =
            createGameSave(
                currentGameSave?.playerName ?: "Player",
                nextChapterNumber,
            )

        lifecycleScope.launch {
            saveGameManager.saveGame(carryOverSave, isAutoSave = false).fold(
                onSuccess = {
                    // Start next chapter with the saved game
                    val intent =
                        Intent(this@GameActivity, GameActivity::class.java).apply {
                            putExtra(EXTRA_LOAD_SAVE_ID, carryOverSave.saveId)
                            putExtra(EXTRA_PLAYER_NAME, carryOverSave.playerName)
                            putExtra(EXTRA_IS_NEW_GAME, false)
                            putExtra(EXTRA_CHAPTER_NUMBER, nextChapterNumber)
                        }
                    startActivity(intent)
                    finish()
                },
                onFailure = { error ->
                    Toast
                        .makeText(
                            this@GameActivity,
                            "Failed to save progress: ${error.message}",
                            Toast.LENGTH_LONG,
                        ).show()
                },
            )
        }
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
            }.setCancelable(false)
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

    private fun showTerrainTooltip(position: Position) {
        val tile = gameState.board.getTile(position) ?: return
        val terrain = tile.terrain

        val terrainInfo =
            buildString {
                append("Terrain: ${terrain.displayName}\n\n")
                append("Movement Cost: ${terrain.movementCost}\n")
                append("Defense Bonus: +${terrain.defensiveBonus}\n")
                append("Avoidance Bonus: +${terrain.avoidanceBonus}%\n\n")

                when (terrain) {
                    com.gameaday.opentactics.model.TerrainType.FOREST ->
                        append("Dense forest provides cover and slows movement.")
                    com.gameaday.opentactics.model.TerrainType.MOUNTAIN ->
                        append("High ground offers excellent defensive position but is hard to traverse.")
                    com.gameaday.opentactics.model.TerrainType.FORT ->
                        append("Fortified position provides strong defensive bonuses.")
                    com.gameaday.opentactics.model.TerrainType.VILLAGE ->
                        append("Village tiles offer moderate defensive bonus.")
                    com.gameaday.opentactics.model.TerrainType.WATER ->
                        append("Water is impassable for most units. Only flying units can cross.")
                    else ->
                        append("Open terrain with no special properties.")
                }
            }

        AlertDialog
            .Builder(this)
            .setTitle("Terrain Info")
            .setMessage(terrainInfo)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun showHelpDialog() {
        val helpText =
            """
            |Welcome to Open Tactics!
            |
            |BASIC CONTROLS:
            |• Tap a unit to select it
            |• Tap Move to see movement range
            |• Tap Attack to see attack range
            |• Tap Wait to end unit's turn
            |• Tap End Turn to end your phase
            |
            |COMBAT:
            |• Units attack based on equipped weapon
            |• Weapon triangle affects damage
            |• Terrain provides defensive bonuses
            |• Speed difference enables double attacks
            |
            |TERRAIN EFFECTS:
            |• Plains: No bonuses (1 move cost)
            |• Forest: +1 Def, +10 Avoid (2 cost)
            |• Mountain: +2 Def, +20 Avoid (3 cost)
            |• Fort: +3 Def, +20 Avoid (1 cost)
            |• Water: Impassable (flying only)
            |
            |UI FEATURES:
            |• Toggle Ranges: Show enemy attack ranges
            |• Undo: Undo last move (before action)
            |• Long-press menu: Save/Load/Settings
            |• Tap terrain: View terrain details
            |
            |OBJECTIVES:
            |Check the top-left panel for chapter objectives and turn count.
            |
            |TIPS:
            |• Use terrain to your advantage
            |• Keep units together for support
            |• Watch enemy ranges carefully
            |• Save often!
            """.trimMargin()

        AlertDialog
            .Builder(this)
            .setTitle("Game Help")
            .setMessage(helpText)
            .setPositiveButton("OK", null)
            .show()
    }
}
