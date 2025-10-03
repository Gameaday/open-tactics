@file:Suppress("MagicNumber", "TooManyFunctions", "LargeClass")

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
import com.gameaday.opentactics.model.Item
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
                // TODO: Create specific maps
                com.gameaday.opentactics.model.MapLayout.FOREST_AMBUSH -> GameBoard.createTestMap()
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
                    addItem(Item.vulnerary())
                    addItem(Item.vulnerary())
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
                    addItem(Item.vulnerary())
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
                    addItem(Item.tonic())
                },
            )

        // Add player characters
        playerCharacters.forEach { char ->
            gameState.addPlayerCharacter(char)
            board.placeCharacter(char, char.position)
        }

        // Initialize support relationships between player characters
        initializeSupportRelationships()

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

    private fun initializeSupportRelationships() {
        // Add support relationships between player characters
        // Sir Garrett (Knight) and Lyanna (Archer)
        gameState.addSupportRelationship(
            com.gameaday.opentactics.model.SupportRelationship(
                characterId1 = "player_knight",
                characterId2 = "player_archer",
                rank = com.gameaday.opentactics.model.SupportRank.C,
                conversationsSeen = 1,
            ),
        )

        // Sir Garrett (Knight) and Aldric (Mage)
        gameState.addSupportRelationship(
            com.gameaday.opentactics.model.SupportRelationship(
                characterId1 = "player_knight",
                characterId2 = "player_mage",
                rank = com.gameaday.opentactics.model.SupportRank.B,
                conversationsSeen = 2,
            ),
        )

        // Lyanna (Archer) and Aldric (Mage)
        gameState.addSupportRelationship(
            com.gameaday.opentactics.model.SupportRelationship(
                characterId1 = "player_archer",
                characterId2 = "player_mage",
                rank = com.gameaday.opentactics.model.SupportRank.C,
                conversationsSeen = 1,
            ),
        )
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

        // Combined adapter for weapons and items
        val combinedAdapter = CombinedInventoryAdapter(character)

        recyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)
        recyclerView.adapter = combinedAdapter

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

    private inner class CombinedInventoryAdapter(
        private val character: Character,
    ) : androidx.recyclerview.widget.RecyclerView.Adapter<androidx.recyclerview.widget.RecyclerView.ViewHolder>() {
        private val typeWeapon = 0
        private val typeItem = 1

        override fun getItemViewType(position: Int): Int =
            if (position < character.inventory.size) {
                typeWeapon
            } else {
                typeItem
            }

        override fun onCreateViewHolder(
            parent: android.view.ViewGroup,
            viewType: Int,
        ): androidx.recyclerview.widget.RecyclerView.ViewHolder =
            if (viewType == typeWeapon) {
                val view = layoutInflater.inflate(R.layout.item_weapon, parent, false)
                WeaponViewHolder(view)
            } else {
                val view = layoutInflater.inflate(R.layout.item_consumable, parent, false)
                ItemViewHolder(view)
            }

        override fun onBindViewHolder(
            holder: androidx.recyclerview.widget.RecyclerView.ViewHolder,
            position: Int,
        ) {
            if (holder is WeaponViewHolder) {
                val weapon = character.inventory[position]
                holder.bind(weapon, character.equippedWeapon == weapon)
            } else if (holder is ItemViewHolder) {
                val itemIndex = position - character.inventory.size
                val item = character.items[itemIndex]
                holder.bind(item)
            }
        }

        override fun getItemCount(): Int = character.inventory.size + character.items.size

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
                    character.equipWeapon(weapon)
                    Toast.makeText(this@GameActivity, "${weapon.name} equipped", Toast.LENGTH_SHORT).show()
                    updateUI()
                    notifyDataSetChanged()
                }

                val btnDrop = itemView.findViewById<android.widget.Button>(R.id.btnDropWeapon)
                btnDrop.setOnClickListener {
                    if (character.inventory.size > 1) {
                        character.removeWeapon(weapon)
                        Toast.makeText(this@GameActivity, "${weapon.name} dropped", Toast.LENGTH_SHORT).show()
                        notifyDataSetChanged()
                    } else {
                        Toast.makeText(this@GameActivity, "Cannot drop last weapon", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        inner class ItemViewHolder(
            view: android.view.View,
        ) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {
            fun bind(item: Item) {
                itemView.findViewById<android.widget.TextView>(R.id.itemName).text = item.name
                itemView.findViewById<android.widget.TextView>(R.id.itemDescription).text = item.description
                itemView.findViewById<android.widget.TextView>(R.id.itemUses).text =
                    "Uses: ${item.currentUses}/${item.maxUses}"

                val btnUse = itemView.findViewById<android.widget.Button>(R.id.btnUseItem)
                btnUse.setOnClickListener {
                    // Use item on self
                    if (character.useItem(item, character)) {
                        Toast
                            .makeText(
                                this@GameActivity,
                                "${character.name} used ${item.name}",
                                Toast.LENGTH_SHORT,
                            ).show()
                        updateUI()
                        notifyDataSetChanged()
                    }
                }

                val btnDiscard = itemView.findViewById<android.widget.Button>(R.id.btnDiscardItem)
                btnDiscard.setOnClickListener {
                    character.removeItem(item)
                    Toast.makeText(this@GameActivity, "${item.name} discarded", Toast.LENGTH_SHORT).show()
                    notifyDataSetChanged()
                }
            }
        }
    }

    private fun showTradeDialog(
        sourceCharacter: Character,
        targetCharacter: Character,
    ) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_trade, null)

        dialogView.findViewById<android.widget.TextView>(R.id.sourceCharacterName).text = sourceCharacter.name
        dialogView.findViewById<android.widget.TextView>(R.id.targetCharacterName).text = targetCharacter.name

        val recyclerView = dialogView.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.tradeItemsRecyclerView)
        val adapter = TradeAdapter(sourceCharacter, targetCharacter)
        recyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)
        recyclerView.adapter = adapter

        val dialog =
            AlertDialog
                .Builder(this)
                .setView(dialogView)
                .create()

        dialogView.findViewById<android.widget.Button>(R.id.btnCancelTrade).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private inner class TradeAdapter(
        private val sourceCharacter: Character,
        private val targetCharacter: Character,
    ) : androidx.recyclerview.widget.RecyclerView.Adapter<TradeAdapter.TradeViewHolder>() {
        private val maxItems = maxOf(sourceCharacter.inventory.size + sourceCharacter.items.size, 1)

        override fun onCreateViewHolder(
            parent: android.view.ViewGroup,
            viewType: Int,
        ): TradeViewHolder {
            val view = layoutInflater.inflate(R.layout.item_trade, parent, false)
            return TradeViewHolder(view)
        }

        override fun onBindViewHolder(
            holder: TradeViewHolder,
            position: Int,
        ) {
            holder.bind(position)
        }

        override fun getItemCount(): Int = maxItems

        inner class TradeViewHolder(
            view: android.view.View,
        ) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {
            fun bind(position: Int) {
                val sourceWeaponCount = sourceCharacter.inventory.size
                val targetWeaponCount = targetCharacter.inventory.size

                // Get source item (weapon or consumable)
                val sourceItem: Any? =
                    when {
                        position < sourceWeaponCount -> sourceCharacter.inventory[position]
                        position < sourceWeaponCount + sourceCharacter.items.size ->
                            sourceCharacter.items[position - sourceWeaponCount]
                        else -> null
                    }

                // Get target item
                val targetItem: Any? =
                    when {
                        position < targetWeaponCount -> targetCharacter.inventory[position]
                        position < targetWeaponCount + targetCharacter.items.size ->
                            targetCharacter.items[position - targetWeaponCount]
                        else -> null
                    }

                // Bind source item
                when (sourceItem) {
                    is Weapon -> {
                        itemView.findViewById<android.widget.TextView>(R.id.sourceItemName).text = sourceItem.name
                        itemView.findViewById<android.widget.TextView>(R.id.sourceItemType).text = sourceItem.type.name
                        itemView.findViewById<android.widget.TextView>(R.id.sourceItemUses).text =
                            "Uses: ${sourceItem.currentUses}/${sourceItem.maxUses}"
                    }
                    is Item -> {
                        itemView.findViewById<android.widget.TextView>(R.id.sourceItemName).text = sourceItem.name
                        itemView.findViewById<android.widget.TextView>(R.id.sourceItemType).text = sourceItem.type.name
                        itemView.findViewById<android.widget.TextView>(R.id.sourceItemUses).text =
                            "Uses: ${sourceItem.currentUses}/${sourceItem.maxUses}"
                    }
                    else -> {
                        itemView.findViewById<android.widget.TextView>(R.id.sourceItemName).text = "Empty"
                        itemView.findViewById<android.widget.TextView>(R.id.sourceItemType).text = ""
                        itemView.findViewById<android.widget.TextView>(R.id.sourceItemUses).text = ""
                    }
                }

                // Bind target item
                when (targetItem) {
                    is Weapon -> {
                        itemView.findViewById<android.widget.TextView>(R.id.targetItemName).text = targetItem.name
                        itemView.findViewById<android.widget.TextView>(R.id.targetItemType).apply {
                            text = targetItem.type.name
                            visibility = android.view.View.VISIBLE
                        }
                        itemView.findViewById<android.widget.TextView>(R.id.targetItemUses).apply {
                            text = "Uses: ${targetItem.currentUses}/${targetItem.maxUses}"
                            visibility = android.view.View.VISIBLE
                        }
                    }
                    is Item -> {
                        itemView.findViewById<android.widget.TextView>(R.id.targetItemName).text = targetItem.name
                        itemView.findViewById<android.widget.TextView>(R.id.targetItemType).apply {
                            text = targetItem.type.name
                            visibility = android.view.View.VISIBLE
                        }
                        itemView.findViewById<android.widget.TextView>(R.id.targetItemUses).apply {
                            text = "Uses: ${targetItem.currentUses}/${targetItem.maxUses}"
                            visibility = android.view.View.VISIBLE
                        }
                    }
                    else -> {
                        itemView.findViewById<android.widget.TextView>(R.id.targetItemName).text = "Empty"
                        itemView.findViewById<android.widget.TextView>(R.id.targetItemType).visibility =
                            android.view.View.GONE
                        itemView.findViewById<android.widget.TextView>(R.id.targetItemUses).visibility =
                            android.view.View.GONE
                    }
                }

                // Handle trade button
                val btnTrade = itemView.findViewById<android.widget.Button>(R.id.btnTradeItem)
                btnTrade.isEnabled = sourceItem != null || targetItem != null
                btnTrade.setOnClickListener {
                    performTrade(position, sourceItem, targetItem)
                }
            }

            private fun performTrade(
                position: Int,
                sourceItem: Any?,
                targetItem: Any?,
            ) {
                when {
                    sourceItem is Weapon && targetItem is Weapon -> {
                        // Swap weapons
                        val sourceIdx = sourceCharacter.inventory.indexOf(sourceItem)
                        val targetIdx = targetCharacter.inventory.indexOf(targetItem)
                        sourceCharacter.inventory[sourceIdx] = targetItem
                        targetCharacter.inventory[targetIdx] = sourceItem
                    }
                    sourceItem is Weapon && targetItem == null -> {
                        // Give weapon to target
                        if (targetCharacter.inventory.size < Character.MAX_INVENTORY_SIZE) {
                            targetCharacter.inventory.add(sourceItem)
                            sourceCharacter.inventory.remove(sourceItem)
                        } else {
                            Toast.makeText(this@GameActivity, "Target inventory full", Toast.LENGTH_SHORT).show()
                            return
                        }
                    }
                    sourceItem == null && targetItem is Weapon -> {
                        // Take weapon from target
                        if (sourceCharacter.inventory.size < Character.MAX_INVENTORY_SIZE) {
                            sourceCharacter.inventory.add(targetItem)
                            targetCharacter.inventory.remove(targetItem)
                        } else {
                            Toast.makeText(this@GameActivity, "Source inventory full", Toast.LENGTH_SHORT).show()
                            return
                        }
                    }
                    sourceItem is Item && targetItem is Item -> {
                        // Swap items
                        val sourceIdx = sourceCharacter.items.indexOf(sourceItem)
                        val targetIdx = targetCharacter.items.indexOf(targetItem)
                        sourceCharacter.items[sourceIdx] = targetItem
                        targetCharacter.items[targetIdx] = sourceItem
                    }
                    sourceItem is Item && targetItem == null -> {
                        // Give item to target
                        targetCharacter.items.add(sourceItem)
                        sourceCharacter.items.remove(sourceItem)
                    }
                    sourceItem == null && targetItem is Item -> {
                        // Take item from target
                        sourceCharacter.items.add(targetItem)
                        targetCharacter.items.remove(targetItem)
                    }
                }

                Toast.makeText(this@GameActivity, "Trade completed", Toast.LENGTH_SHORT).show()
                notifyDataSetChanged()
                updateUI()
            }
        }
    }

    private fun showSupportDialog(character: Character) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_support, null)

        dialogView.findViewById<android.widget.TextView>(R.id.characterNameSupport).text =
            "${character.name} - ${character.characterClass.displayName}"

        val supports = gameState.getCharacterSupports(character.id)
        val recyclerView = dialogView.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.supportRecyclerView)
        val noSupportsText = dialogView.findViewById<android.widget.TextView>(R.id.noSupportsText)

        if (supports.isEmpty()) {
            recyclerView.visibility = android.view.View.GONE
            noSupportsText.visibility = android.view.View.VISIBLE
        } else {
            recyclerView.visibility = android.view.View.VISIBLE
            noSupportsText.visibility = android.view.View.GONE

            val adapter = SupportAdapter(character, supports)
            recyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)
            recyclerView.adapter = adapter
        }

        val dialog =
            AlertDialog
                .Builder(this)
                .setView(dialogView)
                .create()

        dialogView.findViewById<android.widget.Button>(R.id.btnCloseSupport).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private inner class SupportAdapter(
        private val character: Character,
        private val supports: List<com.gameaday.opentactics.model.SupportRelationship>,
    ) : androidx.recyclerview.widget.RecyclerView.Adapter<SupportAdapter.SupportViewHolder>() {
        override fun onCreateViewHolder(
            parent: android.view.ViewGroup,
            viewType: Int,
        ): SupportViewHolder {
            val view = layoutInflater.inflate(R.layout.item_support, parent, false)
            return SupportViewHolder(view)
        }

        override fun onBindViewHolder(
            holder: SupportViewHolder,
            position: Int,
        ) {
            holder.bind(supports[position])
        }

        override fun getItemCount(): Int = supports.size

        inner class SupportViewHolder(
            view: android.view.View,
        ) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {
            fun bind(support: com.gameaday.opentactics.model.SupportRelationship) {
                val partnerId = support.getOtherCharacter(character.id)
                val partner = gameState.getAllCharacters().find { it.id == partnerId }

                itemView.findViewById<android.widget.TextView>(R.id.supportPartnerName).text =
                    partner?.name ?: "Unknown"

                itemView.findViewById<android.widget.TextView>(R.id.supportRank).text =
                    "Rank ${support.rank.name}"

                val bonuses = support.getBonuses()
                val bonusText =
                    buildString {
                        if (bonuses.attack > 0) append("+${bonuses.attack} ATK ")
                        if (bonuses.defense > 0) append("+${bonuses.defense} DEF ")
                        if (bonuses.speed > 0) append("+${bonuses.speed} SPD ")
                        if (bonuses.skill > 0) append("+${bonuses.skill} SKL ")
                        if (bonuses.luck > 0) append("+${bonuses.luck} LCK ")
                    }

                itemView.findViewById<android.widget.TextView>(R.id.supportBonuses).text =
                    if (bonusText.isNotEmpty()) bonusText.trim() else "No bonuses yet"
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
        val options = arrayOf("Save Game", "Load Game", "Settings", "Supports", "Help", "Quit to Menu")

        AlertDialog
            .Builder(this)
            .setTitle("Game Menu")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> performManualSave()
                    1 -> showLoadGameDialog()
                    2 -> showSettingsDialog()
                    3 -> showSupportsMenu()
                    4 -> showHelpDialog()
                    5 -> confirmQuitToMenu()
                }
            }.show()
    }

    private fun showSupportsMenu() {
        val playerCharacters = gameState.getAlivePlayerCharacters()
        if (playerCharacters.isEmpty()) {
            Toast.makeText(this, "No units available", Toast.LENGTH_SHORT).show()
            return
        }

        val characterNames = playerCharacters.map { it.name }.toTypedArray()

        AlertDialog
            .Builder(this)
            .setTitle("View Support Relationships")
            .setItems(characterNames) { _, which ->
                showSupportDialog(playerCharacters[which])
            }.setNegativeButton("Cancel", null)
            .show()
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
                        // Check if clicking on an adjacent ally for trading
                        if (targetCharacter.team == selectedCharacter.team &&
                            selectedCharacter.position.distanceTo(targetCharacter.position) == 1
                        ) {
                            // Offer trade option
                            AlertDialog
                                .Builder(this)
                                .setTitle("${targetCharacter.name}")
                                .setItems(arrayOf("Trade Items", "Cancel")) { _, which ->
                                    if (which == 0) {
                                        showTradeDialog(selectedCharacter, targetCharacter)
                                    }
                                }.show()
                            return
                        }

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
                    "${character.name} gained $expGained EXP! (${character.experience}/100)",
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
                    append("${character.name} reached Level $newLevel!\n")
                    append("(Level $oldLevel → $newLevel)\n\n")

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
                val aliveCount = gameState.getAlivePlayerCharacters().size
                val totalCount = gameState.getPlayerCharacters().size
                append("Units: $aliveCount/$totalCount survived")
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
