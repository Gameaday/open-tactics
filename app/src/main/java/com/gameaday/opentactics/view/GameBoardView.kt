package com.gameaday.opentactics.view

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import com.gameaday.opentactics.R
import com.gameaday.opentactics.game.GameState
import com.gameaday.opentactics.model.*

class GameBoardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var gameState: GameState? = null
    private var tileSize: Float = 0f
    private var boardOffsetX: Float = 0f
    private var boardOffsetY: Float = 0f
    
    private var highlightedMoves: List<Position> = emptyList()
    private var highlightedAttacks: List<Position> = emptyList()
    
    // Paints
    private val tilePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.GRAY
        strokeWidth = 2f
        style = Paint.Style.STROKE
    }
    private val characterPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        textSize = 24f
        color = Color.WHITE
    }
    
    // Colors
    private val plainColor = ContextCompat.getColor(context, R.color.neutral_gray)
    private val forestColor = Color.GREEN
    private val mountainColor = Color.GRAY
    private val fortColor = Color.YELLOW
    private val villageColor = Color.CYAN
    private val waterColor = Color.BLUE
    
    private val playerColor = ContextCompat.getColor(context, R.color.player_blue)
    private val enemyColor = ContextCompat.getColor(context, R.color.enemy_red)
    private val neutralColor = ContextCompat.getColor(context, R.color.neutral_gray)
    
    private val moveHighlightColor = ContextCompat.getColor(context, R.color.movement_highlight)
    private val attackHighlightColor = ContextCompat.getColor(context, R.color.attack_highlight)
    private val selectedHighlightColor = ContextCompat.getColor(context, R.color.selected_highlight)
    
    // Gesture handling
    private val gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
        override fun onSingleTapUp(e: MotionEvent): Boolean {
            val position = screenToBoard(e.x, e.y)
            position?.let { onTileClicked?.invoke(it) }
            return true
        }
    })
    
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
    
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
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
        
        // Draw tiles and terrain
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
        
        // Draw characters
        for (character in gameState.getAllCharacters()) {
            if (character.isAlive) {
                drawCharacter(canvas, character)
            }
        }
    }
    
    private fun drawTile(canvas: Canvas, tile: Tile, x: Int, y: Int) {
        val left = boardOffsetX + x * tileSize
        val top = boardOffsetY + y * tileSize
        val right = left + tileSize
        val bottom = top + tileSize
        
        tilePaint.color = when (tile.terrain) {
            TerrainType.PLAIN -> plainColor
            TerrainType.FOREST -> forestColor
            TerrainType.MOUNTAIN -> mountainColor
            TerrainType.FORT -> fortColor
            TerrainType.VILLAGE -> villageColor
            TerrainType.WATER -> waterColor
        }
        
        canvas.drawRect(left, top, right, bottom, tilePaint)
    }
    
    private fun drawGrid(canvas: Canvas, board: GameBoard) {
        // Vertical lines
        for (x in 0..board.width) {
            val lineX = boardOffsetX + x * tileSize
            canvas.drawLine(
                lineX, boardOffsetY,
                lineX, boardOffsetY + board.height * tileSize,
                gridPaint
            )
        }
        
        // Horizontal lines
        for (y in 0..board.height) {
            val lineY = boardOffsetY + y * tileSize
            canvas.drawLine(
                boardOffsetX, lineY,
                boardOffsetX + board.width * tileSize, lineY,
                gridPaint
            )
        }
    }
    
    private fun drawHighlights(canvas: Canvas, positions: List<Position>, color: Int) {
        for (position in positions) {
            drawHighlight(canvas, position, color)
        }
    }
    
    private fun drawHighlight(canvas: Canvas, position: Position, color: Int) {
        val left = boardOffsetX + position.x * tileSize
        val top = boardOffsetY + position.y * tileSize
        val right = left + tileSize
        val bottom = top + tileSize
        
        tilePaint.color = color
        canvas.drawRect(left, top, right, bottom, tilePaint)
    }
    
    private fun drawCharacter(canvas: Canvas, character: Character) {
        val centerX = boardOffsetX + character.position.x * tileSize + tileSize / 2f
        val centerY = boardOffsetY + character.position.y * tileSize + tileSize / 2f
        val radius = tileSize * 0.3f
        
        characterPaint.color = when (character.team) {
            Team.PLAYER -> playerColor
            Team.ENEMY -> enemyColor
            Team.NEUTRAL -> neutralColor
        }
        
        canvas.drawCircle(centerX, centerY, radius, characterPaint)
        
        // Draw character class initial
        val initial = character.characterClass.displayName.first().toString()
        canvas.drawText(initial, centerX, centerY + textPaint.textSize / 4f, textPaint)
        
        // Draw HP bar if damaged
        if (character.currentHp < character.maxHp) {
            drawHealthBar(canvas, character, centerX, centerY - radius - 10f)
        }
    }
    
    private fun drawHealthBar(canvas: Canvas, character: Character, x: Float, y: Float) {
        val barWidth = tileSize * 0.6f
        val barHeight = 6f
        val healthRatio = character.currentHp.toFloat() / character.maxHp
        
        // Background
        tilePaint.color = Color.RED
        canvas.drawRect(x - barWidth/2, y, x + barWidth/2, y + barHeight, tilePaint)
        
        // Health
        tilePaint.color = Color.GREEN
        canvas.drawRect(x - barWidth/2, y, x - barWidth/2 + barWidth * healthRatio, y + barHeight, tilePaint)
    }
    
    override fun onTouchEvent(event: MotionEvent): Boolean {
        return gestureDetector.onTouchEvent(event) || super.onTouchEvent(event)
    }
    
    private fun screenToBoard(screenX: Float, screenY: Float): Position? {
        val boardX = ((screenX - boardOffsetX) / tileSize).toInt()
        val boardY = ((screenY - boardOffsetY) / tileSize).toInt()
        
        val gameState = this.gameState ?: return null
        return if (gameState.board.isValidPosition(Position(boardX, boardY))) {
            Position(boardX, boardY)
        } else null
    }
}