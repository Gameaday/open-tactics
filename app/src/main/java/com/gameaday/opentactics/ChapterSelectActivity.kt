package com.gameaday.opentactics

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gameaday.opentactics.databinding.ActivityChapterSelectBinding
import com.gameaday.opentactics.model.Chapter
import com.gameaday.opentactics.model.ChapterObjective
import com.gameaday.opentactics.model.ChapterRepository

class ChapterSelectActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChapterSelectBinding
    private var playerName: String = "Player"
    private var unlockedChapter: Int = 1

    companion object {
        const val EXTRA_PLAYER_NAME = "player_name"
        const val EXTRA_UNLOCKED_CHAPTER = "unlocked_chapter"
        private const val LOCKED_CHAPTER_ALPHA = 0.5f
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChapterSelectBinding.inflate(layoutInflater)
        setContentView(binding.root)

        playerName = intent.getStringExtra(EXTRA_PLAYER_NAME) ?: "Player"
        unlockedChapter = intent.getIntExtra(EXTRA_UNLOCKED_CHAPTER, 1)

        setupRecyclerView()
        setupButtons()
    }

    private fun setupRecyclerView() {
        val chapters = mutableListOf<Chapter>()
        for (i in 1..ChapterRepository.getTotalChapters()) {
            ChapterRepository.getChapter(i)?.let { chapters.add(it) }
        }

        val adapter =
            ChapterAdapter(chapters, unlockedChapter) { chapter ->
                startChapter(chapter)
            }

        binding.chapterRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.chapterRecyclerView.adapter = adapter
    }

    private fun setupButtons() {
        binding.btnBack.setOnClickListener {
            finish()
        }
    }

    private fun startChapter(chapter: Chapter) {
        val intent =
            Intent(this, GameActivity::class.java).apply {
                putExtra(GameActivity.EXTRA_PLAYER_NAME, playerName)
                putExtra(GameActivity.EXTRA_IS_NEW_GAME, true)
                putExtra(GameActivity.EXTRA_CHAPTER_NUMBER, chapter.number)
            }
        startActivity(intent)
        finish()
    }

    private class ChapterAdapter(
        private val chapters: List<Chapter>,
        private val unlockedChapter: Int,
        private val onChapterClick: (Chapter) -> Unit,
    ) : RecyclerView.Adapter<ChapterAdapter.ChapterViewHolder>() {
        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int,
        ): ChapterViewHolder {
            val view =
                LayoutInflater
                    .from(parent.context)
                    .inflate(R.layout.item_chapter, parent, false)
            return ChapterViewHolder(view)
        }

        override fun onBindViewHolder(
            holder: ChapterViewHolder,
            position: Int,
        ) {
            val chapter = chapters[position]
            val isLocked = chapter.number > unlockedChapter

            holder.title.text = "Chapter ${chapter.number}: ${chapter.title}"
            holder.objective.text = "Objective: ${getObjectiveText(chapter.objective)}"
            holder.description.text = chapter.description

            if (isLocked) {
                holder.status.visibility = View.VISIBLE
                holder.status.text = "🔒 Locked"
                holder.itemView.alpha = LOCKED_CHAPTER_ALPHA
                holder.itemView.isEnabled = false
            } else {
                holder.status.visibility = View.GONE
                holder.itemView.alpha = 1.0f
                holder.itemView.isEnabled = true
                holder.itemView.setOnClickListener {
                    onChapterClick(chapter)
                }
            }
        }

        override fun getItemCount(): Int = chapters.size

        private fun getObjectiveText(objective: ChapterObjective): String =
            when (objective) {
                ChapterObjective.DEFEAT_ALL_ENEMIES -> "Defeat all enemies"
                ChapterObjective.DEFEAT_BOSS -> "Defeat the boss"
                ChapterObjective.SEIZE_THRONE -> "Seize the throne"
                ChapterObjective.SURVIVE -> "Survive"
                ChapterObjective.ESCAPE -> "Escape"
                ChapterObjective.DEFEND -> "Defend"
            }

        class ChapterViewHolder(
            view: View,
        ) : RecyclerView.ViewHolder(view) {
            val title: TextView = view.findViewById(R.id.chapterTitle)
            val objective: TextView = view.findViewById(R.id.chapterObjective)
            val description: TextView = view.findViewById(R.id.chapterDescription)
            val status: TextView = view.findViewById(R.id.chapterStatus)
        }
    }
}
