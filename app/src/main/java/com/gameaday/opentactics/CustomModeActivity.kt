@file:Suppress("MagicNumber", "TooManyFunctions")

package com.gameaday.opentactics

import android.content.Intent
import android.graphics.Canvas
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gameaday.opentactics.data.CustomMapConfig
import com.gameaday.opentactics.data.CustomUnitConfig
import com.gameaday.opentactics.data.CustomUnitTeam
import com.gameaday.opentactics.data.SaveGameManager
import com.gameaday.opentactics.factory.ItemFactory
import com.gameaday.opentactics.factory.WeaponFactory
import com.gameaday.opentactics.model.AIBehavior
import com.gameaday.opentactics.model.ChapterObjective
import com.gameaday.opentactics.model.CharacterClass
import com.gameaday.opentactics.model.MapLayout
import com.gameaday.opentactics.model.Position
import kotlinx.coroutines.launch

class CustomModeActivity : AppCompatActivity() {
    private lateinit var binding: com.gameaday.opentactics.databinding.ActivityCustomModeBinding
    private lateinit var saveGameManager: SaveGameManager

    private var currentConfig: CustomMapConfig = CustomMapConfig()
    private var playerName: String = "Player"

    private lateinit var playerAdapter: UnitListAdapter
    private lateinit var enemyAdapter: UnitListAdapter

    companion object {
        const val EXTRA_PLAYER_NAME = "player_name"

        // Human-readable display names for map layouts
        private val MAP_DISPLAY_NAMES =
            mapOf(
                MapLayout.TEST_MAP to "Tutorial Map",
                MapLayout.PLAINS_BATTLE to "Open Plains",
                MapLayout.FOREST_AMBUSH to "Forest Ambush",
                MapLayout.MOUNTAIN_PASS to "Mountain Pass",
                MapLayout.CASTLE_SIEGE to "Castle Siege",
                MapLayout.VILLAGE_DEFENSE to "Village Defense",
                MapLayout.RIVER_CROSSING to "River Crossing",
                MapLayout.BORDER_FORT to "Border Fort",
                MapLayout.COASTAL_RUINS to "Coastal Ruins",
                MapLayout.DARK_FOREST to "Dark Forest",
                MapLayout.FORTRESS_INTERIOR to "Fortress Interior",
                MapLayout.THRONE_ROOM to "Throne Room",
                MapLayout.DESERT_OUTPOST to "Desert Outpost",
                MapLayout.FROZEN_PASS to "Frozen Pass",
                MapLayout.CRIMSON_CAPITAL to "Crimson Capital",
                MapLayout.IMPERIAL_PALACE to "Imperial Palace",
                MapLayout.DRAGON_LAIR to "Dragon Lair",
            )

        private val OBJECTIVE_DISPLAY_NAMES =
            mapOf(
                ChapterObjective.DEFEAT_ALL_ENEMIES to "Defeat All Enemies",
                ChapterObjective.DEFEAT_BOSS to "Defeat the Boss",
                ChapterObjective.SEIZE_THRONE to "Seize the Throne",
                ChapterObjective.SURVIVE to "Survive",
                ChapterObjective.ESCAPE to "Escape",
                ChapterObjective.DEFEND to "Defend",
            )

        private val AI_DISPLAY_NAMES =
            mapOf(
                AIBehavior.AGGRESSIVE to "Aggressive — always charges",
                AIBehavior.DEFENSIVE to "Defensive — attacks when in range",
                AIBehavior.STATIONARY to "Stationary — holds position",
                AIBehavior.SUPPORT to "Support — prioritises healing",
            )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding =
            com.gameaday.opentactics.databinding.ActivityCustomModeBinding
                .inflate(layoutInflater)
        setContentView(binding.root)

        saveGameManager = SaveGameManager(this)
        playerName = intent.getStringExtra(EXTRA_PLAYER_NAME) ?: "Player"

        setupSpinners()
        setupRecyclerViews()
        setupClickListeners()
        updateConfigDisplay()
    }

    // ─── Spinners ────────────────────────────────────────────────────────────

    private fun setupSpinners() {
        val mapNames = MapLayout.values().map { MAP_DISPLAY_NAMES[it] ?: it.name }
        binding.spinnerMapLayout.adapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, mapNames)

        val objectiveNames = ChapterObjective.values().map { OBJECTIVE_DISPLAY_NAMES[it] ?: it.name }
        binding.spinnerObjective.adapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, objectiveNames)
    }

    // ─── RecyclerView + drag/swipe ──────────────────────────────────────────

    private fun setupRecyclerViews() {
        // Player units
        playerAdapter =
            UnitListAdapter(
                mutableListOf(),
                onRemove = { index -> removePlayerUnit(index) },
                onTap = { index -> showEditUnitDialog(CustomUnitTeam.PLAYER, index) },
            )
        binding.recyclerPlayerUnits.layoutManager = LinearLayoutManager(this)
        binding.recyclerPlayerUnits.adapter = playerAdapter
        attachSwipeToRemove(binding.recyclerPlayerUnits) { pos -> removePlayerUnit(pos) }

        // Enemy units
        enemyAdapter =
            UnitListAdapter(
                mutableListOf(),
                onRemove = { index -> removeEnemyUnit(index) },
                onTap = { index -> showEditUnitDialog(CustomUnitTeam.ENEMY, index) },
            )
        binding.recyclerEnemyUnits.layoutManager = LinearLayoutManager(this)
        binding.recyclerEnemyUnits.adapter = enemyAdapter
        attachSwipeToRemove(binding.recyclerEnemyUnits) { pos -> removeEnemyUnit(pos) }
    }

    private fun attachSwipeToRemove(
        recyclerView: RecyclerView,
        onSwiped: (Int) -> Unit,
    ) {
        val callback =
            object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
                override fun onMove(
                    rv: RecyclerView,
                    vh: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder,
                ): Boolean = false

                override fun onSwiped(
                    viewHolder: RecyclerView.ViewHolder,
                    direction: Int,
                ) {
                    onSwiped(viewHolder.adapterPosition)
                }

                override fun onChildDraw(
                    c: Canvas,
                    rv: RecyclerView,
                    vh: RecyclerView.ViewHolder,
                    dX: Float,
                    dY: Float,
                    actionState: Int,
                    isCurrentlyActive: Boolean,
                ) {
                    // Fade out on swipe
                    val alpha = 1f - (kotlin.math.abs(dX) / rv.width.toFloat())
                    vh.itemView.alpha = alpha
                    super.onChildDraw(c, rv, vh, dX, dY, actionState, isCurrentlyActive)
                }
            }
        ItemTouchHelper(callback).attachToRecyclerView(recyclerView)
    }

    // ─── Click listeners ────────────────────────────────────────────────────

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            finish()
            applyCloseTransition()
        }

        binding.btnAddPlayerUnit.setOnClickListener {
            if (currentConfig.playerUnits.size < CustomMapConfig.MAX_PLAYER_UNITS) {
                showAddUnitDialog(CustomUnitTeam.PLAYER)
            } else {
                Toast
                    .makeText(this, "Max ${CustomMapConfig.MAX_PLAYER_UNITS} player units", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        binding.btnAddEnemyUnit.setOnClickListener {
            if (currentConfig.enemyUnits.size < CustomMapConfig.MAX_ENEMY_UNITS) {
                showAddUnitDialog(CustomUnitTeam.ENEMY)
            } else {
                Toast
                    .makeText(this, "Max ${CustomMapConfig.MAX_ENEMY_UNITS} enemy units", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        binding.btnClearPlayerUnits.setOnClickListener {
            if (currentConfig.playerUnits.isNotEmpty()) {
                currentConfig = currentConfig.copy(playerUnits = emptyList())
                updateConfigDisplay()
            }
        }

        binding.btnClearEnemyUnits.setOnClickListener {
            if (currentConfig.enemyUnits.isNotEmpty()) {
                currentConfig = currentConfig.copy(enemyUnits = emptyList())
                updateConfigDisplay()
            }
        }

        binding.btnStartBattle.setOnClickListener {
            applySpinnerSelections()
            if (currentConfig.playerUnits.isEmpty()) {
                Toast.makeText(this, "Add at least one player unit", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (currentConfig.enemyUnits.isEmpty()) {
                Toast.makeText(this, "Add at least one enemy unit", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            startCustomGame()
        }

        binding.btnSaveConfig.setOnClickListener {
            applySpinnerSelections()
            showSaveConfigDialog()
        }

        binding.btnLoadConfig.setOnClickListener {
            showLoadConfigDialog()
        }
    }

    private fun applySpinnerSelections() {
        val mapLayout = MapLayout.values()[binding.spinnerMapLayout.selectedItemPosition]
        val objective = ChapterObjective.values()[binding.spinnerObjective.selectedItemPosition]
        currentConfig = currentConfig.copy(mapLayout = mapLayout, objective = objective)
    }

    // ─── Display refresh ────────────────────────────────────────────────────

    private fun updateConfigDisplay() {
        val pCount = currentConfig.playerUnits.size
        val eCount = currentConfig.enemyUnits.size
        binding.txtPlayerUnitCount.text = "Player Units: $pCount / ${CustomMapConfig.MAX_PLAYER_UNITS}"
        binding.txtEnemyUnitCount.text = "Enemy Units: $eCount / ${CustomMapConfig.MAX_ENEMY_UNITS}"

        binding.txtConfigName.text =
            if (currentConfig.name != "Custom Battle") {
                "\"${currentConfig.name}\""
            } else {
                getString(R.string.custom_mode_subtitle)
            }

        // Show/hide empty hints
        binding.txtPlayerEmpty.visibility = if (pCount == 0) View.VISIBLE else View.GONE
        binding.txtEnemyEmpty.visibility = if (eCount == 0) View.VISIBLE else View.GONE

        playerAdapter.updateUnits(currentConfig.playerUnits)
        enemyAdapter.updateUnits(currentConfig.enemyUnits)
    }

    // ─── Unit removal ───────────────────────────────────────────────────────

    private fun removePlayerUnit(index: Int) {
        if (index < 0 || index >= currentConfig.playerUnits.size) return
        val units = currentConfig.playerUnits.toMutableList()
        units.removeAt(index)
        currentConfig = currentConfig.copy(playerUnits = units)
        updateConfigDisplay()
    }

    private fun removeEnemyUnit(index: Int) {
        if (index < 0 || index >= currentConfig.enemyUnits.size) return
        val units = currentConfig.enemyUnits.toMutableList()
        units.removeAt(index)
        currentConfig = currentConfig.copy(enemyUnits = units)
        updateConfigDisplay()
    }

    // ─── Add / Edit unit dialog ─────────────────────────────────────────────

    @Suppress("LongMethod") // Dialog setup requires many view bindings
    private fun showAddUnitDialog(team: CustomUnitTeam) {
        showUnitDialog(team, null)
    }

    private fun showEditUnitDialog(
        team: CustomUnitTeam,
        index: Int,
    ) {
        val units = if (team == CustomUnitTeam.PLAYER) currentConfig.playerUnits else currentConfig.enemyUnits
        if (index < 0 || index >= units.size) return
        showUnitDialog(team, index)
    }

    @Suppress("LongMethod", "ComplexMethod") // Dialog with many controls
    private fun showUnitDialog(
        team: CustomUnitTeam,
        editIndex: Int?,
    ) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_unit, null)
        val nameInput = dialogView.findViewById<EditText>(R.id.editUnitName)
        val classSpinner = dialogView.findViewById<Spinner>(R.id.spinnerClass)
        val levelInput = dialogView.findViewById<EditText>(R.id.editLevel)
        val posXInput = dialogView.findViewById<EditText>(R.id.editPosX)
        val posYInput = dialogView.findViewById<EditText>(R.id.editPosY)
        val weaponSpinner = dialogView.findViewById<Spinner>(R.id.spinnerWeapon)
        val itemSpinner = dialogView.findViewById<Spinner>(R.id.spinnerItem)
        val aiTypeRow = dialogView.findViewById<LinearLayout>(R.id.aiTypeRow)
        val aiSpinner = dialogView.findViewById<Spinner>(R.id.spinnerAiType)
        val bossRow = dialogView.findViewById<LinearLayout>(R.id.bossToggleRow)
        val bossSwitch = dialogView.findViewById<SwitchCompat>(R.id.switchBoss)

        // Populate class spinner
        val selectableClasses = CharacterClass.values().filter { it != CharacterClass.DRAGON }
        val classNames = selectableClasses.map { it.displayName }
        classSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, classNames)

        // Populate weapon spinner
        val weaponIds = WeaponFactory.getAllWeaponIds()
        val weaponNames = weaponIds.map { formatId(it) }
        weaponSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, weaponNames)

        // Populate item spinner (None + all items)
        val itemIds = listOf("") + ItemFactory.getAllItemIds()
        val itemNames = listOf(getString(R.string.custom_item_none)) + ItemFactory.getAllItemIds().map { formatId(it) }
        itemSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, itemNames)

        // Populate AI spinner
        val aiBehaviors = AIBehavior.values()
        val aiNames = aiBehaviors.map { AI_DISPLAY_NAMES[it] ?: it.name }
        aiSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, aiNames)

        // Show/hide enemy-only fields
        val isEnemy = team == CustomUnitTeam.ENEMY
        bossRow.visibility = if (isEnemy) View.VISIBLE else View.GONE
        // AI row always visible (some users might want to see it for documentation)

        // Set defaults / load existing values
        val existing =
            editIndex?.let {
                val list =
                    if (team == CustomUnitTeam.PLAYER) {
                        currentConfig.playerUnits
                    } else {
                        currentConfig.enemyUnits
                    }
                list.getOrNull(it)
            }

        if (existing != null) {
            nameInput.setText(existing.name)
            classSpinner.setSelection(selectableClasses.indexOf(existing.characterClass).coerceAtLeast(0))
            levelInput.setText(existing.level.toString())
            posXInput.setText(existing.position.x.toString())
            posYInput.setText(existing.position.y.toString())
            weaponSpinner.setSelection(weaponIds.indexOf(existing.weaponIds.firstOrNull() ?: "").coerceAtLeast(0))
            itemSpinner.setSelection(itemIds.indexOf(existing.itemIds.firstOrNull() ?: "").coerceAtLeast(0))
            aiSpinner.setSelection(aiBehaviors.indexOf(existing.aiType).coerceAtLeast(0))
            bossSwitch.isChecked = existing.isBoss
        } else {
            val defaultName = if (team == CustomUnitTeam.PLAYER) "Player Unit" else "Enemy Unit"
            nameInput.setText(defaultName)
            levelInput.setText("1")
            posXInput.setText(if (team == CustomUnitTeam.PLAYER) "0" else "10")
            posYInput.setText(if (team == CustomUnitTeam.PLAYER) "7" else "1")
        }

        val title =
            if (editIndex != null) {
                if (team == CustomUnitTeam.PLAYER) "Edit Player Unit" else "Edit Enemy Unit"
            } else {
                if (team == CustomUnitTeam.PLAYER) "Add Player Unit" else "Add Enemy Unit"
            }
        val positiveLabel = if (editIndex != null) "Save" else "Add"

        AlertDialog
            .Builder(this)
            .setTitle(title)
            .setView(dialogView)
            .setPositiveButton(positiveLabel) { _, _ ->
                val defaultName = if (team == CustomUnitTeam.PLAYER) "Player Unit" else "Enemy Unit"
                val name =
                    nameInput.text
                        .toString()
                        .trim()
                        .ifEmpty { defaultName }
                val charClass = selectableClasses[classSpinner.selectedItemPosition]
                val level =
                    levelInput.text
                        .toString()
                        .toIntOrNull()
                        ?.coerceIn(1, CustomMapConfig.MAX_UNIT_LEVEL) ?: 1
                val posX =
                    posXInput.text
                        .toString()
                        .toIntOrNull()
                        ?.coerceAtLeast(0) ?: 0
                val posY =
                    posYInput.text
                        .toString()
                        .toIntOrNull()
                        ?.coerceAtLeast(0) ?: 0
                val weaponId = weaponIds[weaponSpinner.selectedItemPosition]
                val selectedItemId = itemIds[itemSpinner.selectedItemPosition]
                val selectedItemIds = if (selectedItemId.isNotEmpty()) listOf(selectedItemId) else emptyList()
                val aiType = aiBehaviors[aiSpinner.selectedItemPosition]
                val isBoss = isEnemy && bossSwitch.isChecked

                val unit =
                    CustomUnitConfig(
                        name = name,
                        characterClass = charClass,
                        level = level,
                        position = Position(posX, posY),
                        team = team,
                        weaponIds = listOf(weaponId),
                        itemIds = selectedItemIds,
                        isBoss = isBoss,
                        aiType = aiType,
                    )

                if (team == CustomUnitTeam.PLAYER) {
                    val units = currentConfig.playerUnits.toMutableList()
                    if (editIndex != null) units[editIndex] = unit else units.add(unit)
                    currentConfig = currentConfig.copy(playerUnits = units)
                } else {
                    val units = currentConfig.enemyUnits.toMutableList()
                    if (editIndex != null) units[editIndex] = unit else units.add(unit)
                    currentConfig = currentConfig.copy(enemyUnits = units)
                }
                updateConfigDisplay()
            }.setNegativeButton("Cancel", null)
            .show()
    }

    private fun formatId(id: String): String = id.replace("_", " ").replaceFirstChar { it.uppercase() }

    // ─── Save / Load / Delete dialogs ───────────────────────────────────────

    private fun showSaveConfigDialog() {
        val input =
            EditText(this).apply {
                setText(currentConfig.name)
                hint = "Configuration name"
            }

        AlertDialog
            .Builder(this)
            .setTitle("Save Custom Map")
            .setView(input)
            .setPositiveButton("Save") { _, _ ->
                val name =
                    input.text
                        .toString()
                        .trim()
                        .ifEmpty { "Custom Battle" }
                currentConfig = currentConfig.copy(name = name)
                lifecycleScope.launch {
                    saveGameManager
                        .saveCustomMap(currentConfig)
                        .fold(
                            onSuccess = {
                                Toast.makeText(this@CustomModeActivity, "Saved!", Toast.LENGTH_SHORT).show()
                                updateConfigDisplay()
                            },
                            onFailure = { e ->
                                Toast
                                    .makeText(this@CustomModeActivity, "Save failed: ${e.message}", Toast.LENGTH_SHORT)
                                    .show()
                            },
                        )
                }
            }.setNegativeButton("Cancel", null)
            .show()
    }

    private fun showLoadConfigDialog() {
        lifecycleScope.launch {
            val configs = saveGameManager.listCustomMaps()
            if (configs.isEmpty()) {
                AlertDialog
                    .Builder(this@CustomModeActivity)
                    .setTitle("No Saved Configurations")
                    .setMessage("Save a configuration first using the Save button.")
                    .setPositiveButton("OK", null)
                    .show()
                return@launch
            }

            val items =
                configs
                    .map { cfg ->
                        val mapName = MAP_DISPLAY_NAMES[cfg.mapLayout] ?: cfg.mapLayout.name
                        "${cfg.name}  •  $mapName  •  ${cfg.playerUnits.size}v${cfg.enemyUnits.size}"
                    }.toTypedArray()

            AlertDialog
                .Builder(this@CustomModeActivity)
                .setTitle("Load Custom Map")
                .setItems(items) { _, which ->
                    currentConfig = configs[which]
                    binding.spinnerMapLayout.setSelection(
                        MapLayout.values().indexOf(currentConfig.mapLayout),
                    )
                    binding.spinnerObjective.setSelection(
                        ChapterObjective.values().indexOf(currentConfig.objective),
                    )
                    updateConfigDisplay()
                    Toast.makeText(this@CustomModeActivity, "Loaded!", Toast.LENGTH_SHORT).show()
                }.setNegativeButton("Cancel", null)
                .setNeutralButton("Delete…") { _, _ ->
                    showDeleteConfigDialog(configs)
                }.show()
        }
    }

    private fun showDeleteConfigDialog(configs: List<CustomMapConfig>) {
        val items = configs.map { it.name }.toTypedArray()

        AlertDialog
            .Builder(this)
            .setTitle("Delete Custom Map")
            .setItems(items) { _, which ->
                val config = configs[which]
                AlertDialog
                    .Builder(this)
                    .setTitle("Confirm Delete")
                    .setMessage("Delete \"${config.name}\"?")
                    .setPositiveButton("Delete") { _, _ ->
                        lifecycleScope.launch {
                            saveGameManager
                                .deleteCustomMap(config.configId)
                                .fold(
                                    onSuccess = {
                                        Toast.makeText(this@CustomModeActivity, "Deleted!", Toast.LENGTH_SHORT).show()
                                    },
                                    onFailure = { e ->
                                        val msg = "Failed: ${e.message}"
                                        Toast
                                            .makeText(this@CustomModeActivity, msg, Toast.LENGTH_SHORT)
                                            .show()
                                    },
                                )
                        }
                    }.setNegativeButton("Cancel", null)
                    .show()
            }.setNegativeButton("Cancel", null)
            .show()
    }

    // ─── Launch game ────────────────────────────────────────────────────────

    private fun startCustomGame() {
        val intent =
            Intent(this, GameActivity::class.java).apply {
                putExtra(GameActivity.EXTRA_PLAYER_NAME, playerName)
                putExtra(GameActivity.EXTRA_IS_NEW_GAME, true)
                putExtra(GameActivity.EXTRA_CUSTOM_CONFIG, currentConfig)
            }
        startActivity(intent)
        finish()
    }

    private fun applyCloseTransition() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            overrideActivityTransition(OVERRIDE_TRANSITION_CLOSE, R.anim.fade_in, R.anim.fade_out)
        } else {
            @Suppress("DEPRECATION")
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        }
    }

    // ─── RecyclerView adapter ───────────────────────────────────────────────

    private class UnitListAdapter(
        private val units: MutableList<CustomUnitConfig>,
        private val onRemove: (Int) -> Unit,
        private val onTap: (Int) -> Unit,
    ) : RecyclerView.Adapter<UnitListAdapter.UnitViewHolder>() {
        fun updateUnits(newUnits: List<CustomUnitConfig>) {
            units.clear()
            units.addAll(newUnits)
            @Suppress("NotifyDataSetChanged")
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int,
        ): UnitViewHolder {
            val view =
                LayoutInflater
                    .from(parent.context)
                    .inflate(R.layout.item_custom_unit, parent, false)
            return UnitViewHolder(view)
        }

        override fun onBindViewHolder(
            holder: UnitViewHolder,
            position: Int,
        ) {
            val unit = units[position]
            holder.name.text = unit.name
            holder.details.text =
                "${unit.characterClass.displayName}  Lv.${unit.level}  " +
                "(${unit.position.x}, ${unit.position.y})"

            val weaponLabel = unit.weaponIds.firstOrNull()?.let { formatId(it) } ?: "—"
            val itemLabel = unit.itemIds.firstOrNull()?.let { formatId(it) } ?: ""
            val bossLabel = if (unit.isBoss) "  ★ Boss" else ""
            holder.weapon.text = "⚔ $weaponLabel${if (itemLabel.isNotEmpty()) "  •  $itemLabel" else ""}$bossLabel"

            // Team color accent
            val colorRes =
                if (unit.team == CustomUnitTeam.PLAYER) R.color.player_blue else R.color.enemy_red
            holder.teamBar.setBackgroundColor(ContextCompat.getColor(holder.itemView.context, colorRes))

            holder.btnRemove.setOnClickListener { onRemove(position) }
            holder.itemView.setOnClickListener { onTap(position) }
        }

        override fun getItemCount(): Int = units.size

        private fun formatId(id: String): String = id.replace("_", " ").replaceFirstChar { it.uppercase() }

        class UnitViewHolder(
            view: View,
        ) : RecyclerView.ViewHolder(view) {
            val name: TextView = view.findViewById(R.id.unitName)
            val details: TextView = view.findViewById(R.id.unitDetails)
            val weapon: TextView = view.findViewById(R.id.unitWeapon)
            val teamBar: View = view.findViewById(R.id.teamColorBar)
            val btnRemove: View = view.findViewById(R.id.btnRemoveUnit)
        }
    }
}
