package com.gameaday.opentactics

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.gameaday.opentactics.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnNewGame.setOnClickListener {
            startGame()
        }

        binding.btnContinue.setOnClickListener {
            // TODO: Load saved game
        }

        binding.btnSettings.setOnClickListener {
            // TODO: Open settings
        }

        binding.btnAbout.setOnClickListener {
            // TODO: Show about dialog
        }

        binding.btnExit.setOnClickListener {
            finish()
        }
    }

    private fun startGame() {
        val intent = Intent(this, GameActivity::class.java)
        startActivity(intent)
    }
}