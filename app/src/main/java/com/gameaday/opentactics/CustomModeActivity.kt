@file:Suppress("MagicNumber", "TooManyFunctions")

package com.gameaday.opentactics

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gameaday.opentactics.data.CustomMapConfig
import com.gameaday.opentactics.data.CustomUnitConfig
import com.gameaday.opentactics.data.CustomUnitTeam
import com.gameaday.opentactics.data.SaveGameManager
import com.gameaday.opentactics.databinding.ActivityCustomModeBinding
import com.gameaday.opentactics.model.AIBehavior
import com.gameaday.opentactics.model.ChapterObjective
import com.gameaday.opentactics.model.CharacterClass
import com.gameaday.opentactics.model.MapLayout
import com.gameaday.opentactics.model.Position
import kotlinx.coroutines.launch

class CustomModeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCustomModeBinding
    private lateinit var saveGameManager: SaveGameManager

    private var currentConfig: CustomMapConfig = CustomMapConfig()
    private var playerName: String = "Player"

    companion object {
        const val EXTRA_PLAYER_NAME = "player_name"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCustomModeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        saveGameManager = SaveGameManager(this)
        playerName = intent.getStringExtra(EXTRA_PLAYER_NAME) ?: "Player"

        setupSpinners()
        setupClickListeners()
        updateConfigDisplay()
    }

    private fun setupSpinners() {
        // Map layout spinner
        val mapNames = MapLayout.values().map { it.name.replace("_", " ") }
        binding.spinnerMapLayout.adapter =
            ArrayAdapter(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                mapNames,
            )

        // Objective spinner
        val objectiveNames = ChapterObjective.values().map { it.name.replace("_", " ") }
        binding.spinnerObjective.adapter =
            ArrayAdapter(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                objectiveNames,
            )
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener { finish() }

        binding.btnAddPlayerUnit.setOnClickListener {
            if (currentConfig.playerUnits.size < CustomMapConfig.MAX_PLAYER_UNITS) {
                showAddUnitDialog(CustomUnitTeam.PLAYER)
            } else {
                Toast
                    .makeText(
                        this,
                        "Maximum ${CustomMapConfig.MAX_PLAYER_UNITS} player units",
                        Toast.LENGTH_SHORT,
                    ).show()
            }
        }

        binding.btnAddEnemyUnit.setOnClickListener {
            if (currentConfig.enemyUnits.size < CustomMapConfig.MAX_ENEMY_UNITS) {
                showAddUnitDialog(CustomUnitTeam.ENEMY)
            } else {
                Toast
                    .makeText(
                        this,
                        "Maximum ${CustomMapConfig.MAX_ENEMY_UNITS} enemy units",
                        Toast.LENGTH_SHORT,
                    ).show()
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
        currentConfig =
            currentConfig.copy(
                mapLayout = mapLayout,
                objective = objective,
            )
    }

    private fun showAddUnitDialog(team: CustomUnitTeam) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_unit, null)
        val nameInput = dialogView.findViewById<EditText>(R.id.editUnitName)
        val classSpinner = dialogView.findViewById<Spinner>(R.id.spinnerClass)
        val levelInput = dialogView.findViewById<EditText>(R.id.editLevel)
        val posXInput = dialogView.findViewById<EditText>(R.id.editPosX)
        val posYInput = dialogView.findViewById<EditText>(R.id.editPosY)
        val weaponSpinner = dialogView.findViewById<Spinner>(R.id.spinnerWeapon)

        // Exclude DRAGON class from user selection (it's a transform target)
        val selectableClasses = CharacterClass.values().filter { it != CharacterClass.DRAGON }
        val classNames = selectableClasses.map { it.displayName }
        classSpinner.adapter =
            ArrayAdapter(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                classNames,
            )

        val weaponIds =
            com.gameaday.opentactics.factory.WeaponFactory
                .getAllWeaponIds()
        val weaponNames = weaponIds.map { it.replace("_", " ").replaceFirstChar { c -> c.uppercase() } }
        weaponSpinner.adapter =
            ArrayAdapter(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                weaponNames,
            )

        // Set defaults
        val defaultName = if (team == CustomUnitTeam.PLAYER) "Player Unit" else "Enemy Unit"
        nameInput.setText(defaultName)
        levelInput.setText("1")
        posXInput.setText(if (team == CustomUnitTeam.PLAYER) "0" else "10")
        posYInput.setText(if (team == CustomUnitTeam.PLAYER) "7" else "1")

        AlertDialog
            .Builder(this)
            .setTitle(if (team == CustomUnitTeam.PLAYER) "Add Player Unit" else "Add Enemy Unit")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
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

                val unit =
                    CustomUnitConfig(
                        name = name,
                        characterClass = charClass,
                        level = level,
                        position = Position(posX, posY),
                        team = team,
                        weaponIds = listOf(weaponId),
                        itemIds = if (team == CustomUnitTeam.PLAYER) listOf("vulnerary") else emptyList(),
                        aiType = if (team == CustomUnitTeam.ENEMY) AIBehavior.AGGRESSIVE else AIBehavior.AGGRESSIVE,
                    )

                if (team == CustomUnitTeam.PLAYER) {
                    currentConfig =
                        currentConfig.copy(
                            playerUnits = currentConfig.playerUnits + unit,
                        )
                } else {
                    currentConfig =
                        currentConfig.copy(
                            enemyUnits = currentConfig.enemyUnits + unit,
                        )
                }
                updateConfigDisplay()
            }.setNegativeButton("Cancel", null)
            .show()
    }

    private fun updateConfigDisplay() {
        binding.txtPlayerUnitCount.text = "Player Units: ${currentConfig.playerUnits.size}"
        binding.txtEnemyUnitCount.text = "Enemy Units: ${currentConfig.enemyUnits.size}"

        // Update player units list
        binding.recyclerPlayerUnits.layoutManager = LinearLayoutManager(this)
        binding.recyclerPlayerUnits.adapter =
            UnitListAdapter(
                currentConfig.playerUnits,
            ) { index -> removePlayerUnit(index) }

        // Update enemy units list
        binding.recyclerEnemyUnits.layoutManager = LinearLayoutManager(this)
        binding.recyclerEnemyUnits.adapter =
            UnitListAdapter(
                currentConfig.enemyUnits,
            ) { index -> removeEnemyUnit(index) }
    }

    private fun removePlayerUnit(index: Int) {
        val units = currentConfig.playerUnits.toMutableList()
        units.removeAt(index)
        currentConfig = currentConfig.copy(playerUnits = units)
        updateConfigDisplay()
    }

    private fun removeEnemyUnit(index: Int) {
        val units = currentConfig.enemyUnits.toMutableList()
        units.removeAt(index)
        currentConfig = currentConfig.copy(enemyUnits = units)
        updateConfigDisplay()
    }

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
                    saveGameManager.saveCustomMap(currentConfig).fold(
                        onSuccess = {
                            Toast
                                .makeText(
                                    this@CustomModeActivity,
                                    "Configuration saved!",
                                    Toast.LENGTH_SHORT,
                                ).show()
                        },
                        onFailure = { error ->
                            Toast
                                .makeText(
                                    this@CustomModeActivity,
                                    "Save failed: ${error.message}",
                                    Toast.LENGTH_SHORT,
                                ).show()
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
                    .setMessage("No custom map configurations found.")
                    .setPositiveButton("OK", null)
                    .show()
                return@launch
            }

            val items =
                configs
                    .map { "${it.name} — ${it.mapLayout.name.replace("_", " ")}" }
                    .toTypedArray()

            AlertDialog
                .Builder(this@CustomModeActivity)
                .setTitle("Load Custom Map")
                .setItems(items) { _, which ->
                    currentConfig = configs[which]
                    // Sync spinners with loaded config
                    binding.spinnerMapLayout.setSelection(
                        MapLayout.values().indexOf(currentConfig.mapLayout),
                    )
                    binding.spinnerObjective.setSelection(
                        ChapterObjective.values().indexOf(currentConfig.objective),
                    )
                    updateConfigDisplay()
                    Toast.makeText(this@CustomModeActivity, "Configuration loaded!", Toast.LENGTH_SHORT).show()
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
                            saveGameManager.deleteCustomMap(config.configId).fold(
                                onSuccess = {
                                    Toast
                                        .makeText(
                                            this@CustomModeActivity,
                                            "Deleted!",
                                            Toast.LENGTH_SHORT,
                                        ).show()
                                },
                                onFailure = { error ->
                                    Toast
                                        .makeText(
                                            this@CustomModeActivity,
                                            "Delete failed: ${error.message}",
                                            Toast.LENGTH_SHORT,
                                        ).show()
                                },
                            )
                        }
                    }.setNegativeButton("Cancel", null)
                    .show()
            }.setNegativeButton("Cancel", null)
            .show()
    }

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

    /**
     * Adapter for displaying unit configurations in a RecyclerView.
     */
    private class UnitListAdapter(
        private val units: List<CustomUnitConfig>,
        private val onRemove: (Int) -> Unit,
    ) : RecyclerView.Adapter<UnitListAdapter.UnitViewHolder>() {
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
            holder.details.text = "${unit.characterClass.displayName} Lv.${unit.level} " +
                "(${unit.position.x},${unit.position.y})"
            holder.btnRemove.setOnClickListener { onRemove(position) }
        }

        override fun getItemCount(): Int = units.size

        class UnitViewHolder(
            view: View,
        ) : RecyclerView.ViewHolder(view) {
            val name: TextView = view.findViewById(R.id.unitName)
            val details: TextView = view.findViewById(R.id.unitDetails)
            val btnRemove: View = view.findViewById(R.id.btnRemoveUnit)
        }
    }
}
