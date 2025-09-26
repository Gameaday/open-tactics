package com.gameaday.opentactics.view

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.graphics.drawable.VectorDrawable
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.core.content.ContextCompat
import com.gameaday.opentactics.R
import com.gameaday.opentactics.game.GameState
import com.gameaday.opentactics.model.*

class GameBoardView
    @JvmOverloads
    constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0,
    ) : View(context, attrs, defStyleAttr) {
        private var gameState: GameState? = null
        private var tileSize: Float = 0f
        private var boardOffsetX: Float = 0f
        private var boardOffsetY: Float = 0f

        private var highlightedMoves: List<Position> = emptyList()
        private var highlightedAttacks: List<Position> = emptyList()

        // Animation properties
        private var shakeOffsetX: Float = 0f
        private var shakeOffsetY: Float = 0f
        private var effectAlpha: Float = 0f
        private var animatingCharacter: Character? = null

        // Paints
        private val tilePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val gridPaint =
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.BLACK
                strokeWidth = 2f
                style = Paint.Style.STROKE
            }
        private val iconPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val textPaint =
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                textAlign = Paint.Align.CENTER
                textSize = 16f
                color = Color.WHITE
                typeface = Typeface.DEFAULT_BOLD
            }

        // Terrain Colors (more vibrant and distinct)
        private val plainColor = Color.parseColor("#4CAF50") // Green
        private val forestColor = Color.parseColor("#2E7D32") // Dark Green
        private val mountainColor = Color.parseColor("#616161") // Gray
        private val fortColor = Color.parseColor("#FFA000") // Amber
        private val villageColor = Color.parseColor("#1976D2") // Blue
        private val waterColor = Color.parseColor("#0288D1") // Light Blue

        // Team Colors (distinct and visible)
        private val playerColorPrimary = Color.parseColor("#1565C0") // Blue
        private val playerColorSecondary = Color.parseColor("#E3F2FD") // Light Blue
        private val enemyColorPrimary = Color.parseColor("#C62828") // Red
        private val enemyColorSecondary = Color.parseColor("#FFEBEE") // Light Red
        private val neutralColor = Color.parseColor("#424242") // Dark Gray

        // Highlight Colors
        private val moveHighlightColor = Color.parseColor("#80FFD700") // Gold with transparency
        private val attackHighlightColor = Color.parseColor("#80FF5722") // Orange-red with transparency
        private val selectedHighlightColor = Color.parseColor("#FF00BCD4") // Cyan

        // Icon resources
        private val iconMap =
            mapOf(
                CharacterClass.KNIGHT to R.drawable.ic_knight,
                CharacterClass.ARCHER to R.drawable.ic_archer,
                CharacterClass.MAGE to R.drawable.ic_mage,
                CharacterClass.HEALER to R.drawable.ic_healer,
                CharacterClass.THIEF to R.drawable.ic_thief,
            )

        // Gesture handling
        private val gestureDetector =
            GestureDetector(
                context,
                object : GestureDetector.SimpleOnGestureListener() {
                    override fun onSingleTapUp(e: MotionEvent): Boolean {
                        val position = screenToBoard(e.x, e.y)
                        position?.let { onTileClicked?.invoke(it) }
                        return true
                    }
                },
            )

        var onTileClicked: ((Position) -> Unit)? = null

        fun setGameState(gameState: GameState) {
            this.gameState = gameState
            calculateDimensions()
            invalidate()
        }

        fun highlightMovement(positions: List<Position>) {
            highlightedMoves = positions
            invalidate()
        }

        fun highlightAttacks(positions: List<Position>) {
            highlightedAttacks = positions
            invalidate()
        }

        fun clearHighlights() {
            highlightedMoves = emptyList()
            highlightedAttacks = emptyList()
            invalidate()
        }

        fun animateAttack(
            attacker: Character,
            target: Character,
            onComplete: () -> Unit = {},
        ) {
            animatingCharacter = attacker

            // Create shake animation for attacker
            val shakeX =
                ObjectAnimator.ofFloat(0f, -8f, 8f, -4f, 4f, 0f).apply {
                    duration = 300
                    addUpdateListener { animation ->
                        shakeOffsetX = animation.animatedValue as Float
                        invalidate()
                    }
                }

            val shakeY =
                ObjectAnimator.ofFloat(0f, -4f, 4f, -2f, 2f, 0f).apply {
                    duration = 300
                    addUpdateListener { animation ->
                        shakeOffsetY = animation.animatedValue as Float
                        invalidate()
                    }
                }

            // Create effect animation at target position
            val effectAnim =
                ValueAnimator.ofFloat(0f, 1f, 0f).apply {
                    duration = 500
                    addUpdateListener { animation ->
                        effectAlpha = animation.animatedValue as Float
                        invalidate()
                    }
                }

            AnimatorSet().apply {
                playTogether(shakeX, shakeY, effectAnim)
                interpolator = DecelerateInterpolator()
                addListener(
                    object : android.animation.AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: android.animation.Animator) {
                            animatingCharacter = null
                            shakeOffsetX = 0f
                            shakeOffsetY = 0f
                            effectAlpha = 0f
                            invalidate()
                            onComplete()
                        }
                    },
                )
                start()
            }
        }

        override fun onSizeChanged(
            w: Int,
            h: Int,
            oldw: Int,
            oldh: Int,
        ) {
            super.onSizeChanged(w, h, oldw, oldh)
            calculateDimensions()
        }

        private fun calculateDimensions() {
            val board = gameState?.board ?: return

            val availableWidth = width - paddingLeft - paddingRight
            val availableHeight = height - paddingTop - paddingBottom

            val tileWidth = availableWidth.toFloat() / board.width
            val tileHeight = availableHeight.toFloat() / board.height

            tileSize = minOf(tileWidth, tileHeight)

            boardOffsetX = paddingLeft + (availableWidth - tileSize * board.width) / 2f
            boardOffsetY = paddingTop + (availableHeight - tileSize * board.height) / 2f
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)

            val gameState = this.gameState ?: return
            val board = gameState.board

            // Draw tiles with terrain colors
            for (y in 0 until board.height) {
                for (x in 0 until board.width) {
                    val position = Position(x, y)
                    val tile = board.getTile(position) ?: continue

                    drawTile(canvas, tile, x, y)
                }
            }

            // Draw grid
            drawGrid(canvas, board)

            // Draw movement highlights
            drawHighlights(canvas, highlightedMoves, moveHighlightColor)

            // Draw attack highlights
            drawHighlights(canvas, highlightedAttacks, attackHighlightColor)

            // Draw selected character highlight
            gameState.selectedCharacter?.let { character ->
                drawHighlight(canvas, character.position, selectedHighlightColor)
            }

            // Draw characters with proper icons
            for (character in gameState.getAllCharacters()) {
                if (character.isAlive) {
                    drawCharacter(canvas, character)
                }
            }

            // Draw battle effects
            if (effectAlpha > 0f) {
                drawBattleEffect(canvas)
            }
        }

        private fun drawTile(
            canvas: Canvas,
            tile: Tile,
            x: Int,
            y: Int,
        ) {
            val left = boardOffsetX + x * tileSize
            val top = boardOffsetY + y * tileSize
            val right = left + tileSize
            val bottom = top + tileSize

            tilePaint.color =
                when (tile.terrain) {
                    TerrainType.PLAIN -> plainColor
                    TerrainType.FOREST -> forestColor
                    TerrainType.MOUNTAIN -> mountainColor
                    TerrainType.FORT -> fortColor
                    TerrainType.VILLAGE -> villageColor
                    TerrainType.WATER -> waterColor
                }

            canvas.drawRect(left, top, right, bottom, tilePaint)

            // Add terrain indicators
            if (tile.terrain != TerrainType.PLAIN) {
                textPaint.textSize = tileSize * 0.2f
                val terrainSymbol =
                    when (tile.terrain) {
                        TerrainType.FOREST -> "♠"
                        TerrainType.MOUNTAIN -> "▲"
                        TerrainType.FORT -> "⌂"
                        TerrainType.VILLAGE -> "⌂"
                        TerrainType.WATER -> "~"
                        else -> ""
                    }
                canvas.drawText(
                    terrainSymbol,
                    left + tileSize * 0.85f,
                    top + tileSize * 0.25f,
                    textPaint,
                )
            }
        }

        private fun drawGrid(
            canvas: Canvas,
            board: GameBoard,
        ) {
            // Vertical lines
            for (x in 0..board.width) {
                val lineX = boardOffsetX + x * tileSize
                canvas.drawLine(
                    lineX,
                    boardOffsetY,
                    lineX,
                    boardOffsetY + board.height * tileSize,
                    gridPaint,
                )
            }

            // Horizontal lines
            for (y in 0..board.height) {
                val lineY = boardOffsetY + y * tileSize
                canvas.drawLine(
                    boardOffsetX,
                    lineY,
                    boardOffsetX + board.width * tileSize,
                    lineY,
                    gridPaint,
                )
            }
        }

        private fun drawHighlights(
            canvas: Canvas,
            positions: List<Position>,
            color: Int,
        ) {
            for (position in positions) {
                drawHighlight(canvas, position, color)
            }
        }

        private fun drawHighlight(
            canvas: Canvas,
            position: Position,
            color: Int,
        ) {
            val left = boardOffsetX + position.x * tileSize
            val top = boardOffsetY + position.y * tileSize
            val right = left + tileSize
            val bottom = top + tileSize

            tilePaint.color = color
            canvas.drawRect(left, top, right, bottom, tilePaint)
        }

        private fun drawCharacter(
            canvas: Canvas,
            character: Character,
        ) {
            var centerX = boardOffsetX + character.position.x * tileSize + tileSize / 2f
            var centerY = boardOffsetY + character.position.y * tileSize + tileSize / 2f

            // Apply shake animation if this character is animating
            if (character == animatingCharacter) {
                centerX += shakeOffsetX
                centerY += shakeOffsetY
            }

            val tileRadius = tileSize * 0.4f

            // Draw character background circle
            val (primaryColor, secondaryColor) =
                when (character.team) {
                    Team.PLAYER -> Pair(playerColorPrimary, playerColorSecondary)
                    Team.ENEMY -> Pair(enemyColorPrimary, enemyColorSecondary)
                    Team.NEUTRAL -> Pair(neutralColor, Color.WHITE)
                }

            // Draw outer circle (primary color)
            tilePaint.color = primaryColor
            canvas.drawCircle(centerX, centerY, tileRadius, tilePaint)

            // Draw inner circle (secondary color)
            tilePaint.color = secondaryColor
            canvas.drawCircle(centerX, centerY, tileRadius * 0.8f, tilePaint)

            // Draw character class icon
            val iconResId = iconMap[character.characterClass]
            if (iconResId != null) {
                val drawable = ContextCompat.getDrawable(context, iconResId) as? VectorDrawable
                drawable?.let { icon ->
                    icon.setTint(primaryColor)
                    val iconSize = (tileRadius * 1.2f).toInt()
                    val iconLeft = (centerX - iconSize / 2f).toInt()
                    val iconTop = (centerY - iconSize / 2f).toInt()
                    icon.setBounds(iconLeft, iconTop, iconLeft + iconSize, iconTop + iconSize)
                    icon.draw(canvas)
                }
            }

            // Draw HP bar if damaged
            if (character.currentHp < character.maxHp) {
                drawHealthBar(canvas, character, centerX, centerY - tileRadius - 15f)
            }

            // Draw level indicator
            textPaint.textSize = tileSize * 0.2f
            textPaint.color = Color.WHITE
            canvas.drawText(
                "L${character.level}",
                centerX,
                centerY + tileRadius + 20f,
                textPaint,
            )
        }

        private fun drawHealthBar(
            canvas: Canvas,
            character: Character,
            x: Float,
            y: Float,
        ) {
            val barWidth = tileSize * 0.8f
            val barHeight = 8f
            val healthRatio = character.currentHp.toFloat() / character.maxHp

            // Background (damaged portion)
            tilePaint.color = Color.RED
            canvas.drawRect(x - barWidth / 2, y, x + barWidth / 2, y + barHeight, tilePaint)

            // Health (current portion)
            tilePaint.color = Color.GREEN
            canvas.drawRect(
                x - barWidth / 2,
                y,
                x - barWidth / 2 + barWidth * healthRatio,
                y + barHeight,
                tilePaint,
            )

            // Border
            gridPaint.color = Color.BLACK
            canvas.drawRect(x - barWidth / 2, y, x + barWidth / 2, y + barHeight, gridPaint)
        }

        private fun drawBattleEffect(canvas: Canvas) {
            // Simple explosion-like effect
            tilePaint.color = Color.argb((255 * effectAlpha).toInt(), 255, 255, 0) // Yellow flash
            val radius = tileSize * 0.6f * effectAlpha

            // Draw effect at all attack targets
            for (pos in highlightedAttacks) {
                val centerX = boardOffsetX + pos.x * tileSize + tileSize / 2f
                val centerY = boardOffsetY + pos.y * tileSize + tileSize / 2f
                canvas.drawCircle(centerX, centerY, radius, tilePaint)
            }
        }

        override fun onTouchEvent(event: MotionEvent): Boolean = gestureDetector.onTouchEvent(event) || super.onTouchEvent(event)

        private fun screenToBoard(
            screenX: Float,
            screenY: Float,
        ): Position? {
            val boardX = ((screenX - boardOffsetX) / tileSize).toInt()
            val boardY = ((screenY - boardOffsetY) / tileSize).toInt()

            val gameState = this.gameState ?: return null
            return if (gameState.board.isValidPosition(Position(boardX, boardY))) {
                Position(boardX, boardY)
            } else {
                null
            }
        }
    }
