package com.example.cozypuzzle.core.customview

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import com.example.cozypuzzle.R
import com.example.cozypuzzle.core.path.model.CatBoard
import com.example.cozypuzzle.core.path.model.Direction
import androidx.core.content.ContextCompat

/**
 * Renders the cat path board and forwards raw user interactions.
 * Contains no gameplay rules, persistence or navigation logic.
 */
class CatBoardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var onCellTapped: ((cell: Int) -> Unit)? = null

    private var board: CatBoard? = null
    private var selectedCell: Int? = null
    private var hintCells: Set<Int> = emptySet()
    private var pathCells: List<Int> = emptyList()
    private var walkIndex: Int = NO_WALK_INDEX

    private val grassPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.cozy_green_container)
    }
    private val obstaclePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.cozy_brown_text)
        alpha = OBSTACLE_ALPHA
    }
    private val tilePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.cozy_green)
        style = Paint.Style.STROKE
        strokeWidth = TILE_STROKE_WIDTH_PX
        strokeCap = Paint.Cap.ROUND
    }
    private val selectedPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.cozy_blossom)
        style = Paint.Style.STROKE
        strokeWidth = SELECTION_STROKE_WIDTH_PX
    }
    private val hintPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.cozy_sun)
        style = Paint.Style.STROKE
        strokeWidth = SELECTION_STROKE_WIDTH_PX
    }
    private val pathPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.cozy_sky)
        alpha = PATH_ALPHA
    }
    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.cozy_brown_text)
        alpha = GRID_LINE_ALPHA
        strokeWidth = GRID_LINE_WIDTH_PX
    }
    private val emojiTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        textSize = EMOJI_TEXT_SIZE_SP * resources.displayMetrics.scaledDensity
    }

    fun render(
        board: CatBoard?,
        selectedCell: Int?,
        hintCells: List<Int>,
        pathCells: List<Int>,
        walkIndex: Int
    ) {
        this.board = board
        this.selectedCell = selectedCell
        this.hintCells = hintCells.toSet()
        this.pathCells = pathCells
        this.walkIndex = if (walkIndex >= 0) walkIndex else NO_WALK_INDEX
        contentDescription = context.getString(R.string.cd_cat_board)
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val currentBoard = board ?: return
        val boardRect = boardRect()
        val cellWidth = boardRect.width() / currentBoard.columns
        val cellHeight = boardRect.height() / currentBoard.rows

        for (cell in 0 until currentBoard.cellCount) {
            val left = boardRect.left + (cell % currentBoard.columns) * cellWidth
            val top = boardRect.top + (cell / currentBoard.columns) * cellHeight
            canvas.drawRect(left, top, left + cellWidth, top + cellHeight, grassPaint)
        }

        for (row in 1 until currentBoard.rows) {
            val y = boardRect.top + row * cellHeight
            canvas.drawLine(boardRect.left, y, boardRect.right, y, gridPaint)
        }
        for (col in 1 until currentBoard.columns) {
            val x = boardRect.left + col * cellWidth
            canvas.drawLine(x, boardRect.top, x, boardRect.bottom, gridPaint)
        }

        drawPathPreview(canvas, boardRect, cellWidth, cellHeight)

        currentBoard.obstacles.forEach { cell -> drawObstacle(canvas, cell, cellWidth, cellHeight) }

        currentBoard.tiles.forEach { (cell, _) ->
            drawTileConnections(canvas, cell, currentBoard.tileAt(cell)?.connections ?: emptySet(), cellWidth, cellHeight)

            val highlight = when {
                walkIndex != NO_WALK_INDEX && pathCells.getOrNull(walkIndex.coerceAtMost(pathCells.lastIndex)) == cell -> selectedPaint
                hintCells.contains(cell) -> hintPaint
                selectedCell == cell -> selectedPaint
                else -> null
            } ?: return@forEach
            drawCellHighlight(canvas, cell, highlight, cellWidth, cellHeight)
        }

        drawActor(canvas, currentBoard.catPosition, CAT_EMOJI, cellWidth, cellHeight)
        drawActor(canvas, currentBoard.homePosition, HOME_EMOJI, cellWidth, cellHeight)
    }

    private fun drawTileConnections(
        canvas: Canvas,
        cell: Int,
        connections: Set<Direction>,
        cellWidth: Float,
        cellHeight: Float
    ) {
        val rect = cellRect(cell, cellWidth, cellHeight)
        val centerX = rect.centerX()
        val centerY = rect.centerY()
        connections.forEach { direction ->
            val endX = when (direction) {
                Direction.EAST -> rect.right
                Direction.WEST -> rect.left
                else -> centerX
            }
            val endY = when (direction) {
                Direction.SOUTH -> rect.bottom
                Direction.NORTH -> rect.top
                else -> centerY
            }
            canvas.drawLine(centerX, centerY, endX, endY, tilePaint)
        }
    }

    private fun drawPathPreview(canvas: Canvas, boardRect: RectF, cellWidth: Float, cellHeight: Float) {
        if (walkIndex == NO_WALK_INDEX && pathCells.isEmpty()) return
        val visible = if (walkIndex == NO_WALK_INDEX) {
            pathCells
        } else {
            pathCells.take(walkIndex + 1)
        }
        visible.forEach { cell ->
            val rect = cellRect(cell, cellWidth, cellHeight)
            canvas.drawCircle(rect.centerX(), rect.centerY(), minOf(cellWidth, cellHeight) / 4f, pathPaint)
        }
    }

    private fun drawObstacle(canvas: Canvas, cell: Int, cellWidth: Float, cellHeight: Float) {
        val rect = cellRect(cell, cellWidth, cellHeight)
        val inset = rect.width() * 0.22f
        canvas.drawOval(
            RectF(rect.left + inset, rect.top + inset, rect.right - inset, rect.bottom - inset),
            obstaclePaint
        )
    }

    private fun drawActor(
        canvas: Canvas,
        cell: Int,
        emoji: String,
        cellWidth: Float,
        cellHeight: Float
    ) {
        val rect = cellRect(cell, cellWidth, cellHeight)
        canvas.drawText(emoji, rect.centerX(), rect.centerY() + emojiTextPaint.textSize / 3f, emojiTextPaint)
    }

    private fun drawCellHighlight(
        canvas: Canvas,
        cell: Int,
        paint: Paint,
        cellWidth: Float,
        cellHeight: Float
    ) {
        val inset = HIGHLIGHT_INSET_FRACTION * minOf(cellWidth, cellHeight)
        val rect = cellRect(cell, cellWidth, cellHeight)
        canvas.drawRoundRect(
            RectF(rect.left + inset, rect.top + inset, rect.right - inset, rect.bottom - inset),
            CORNER_RADIUS_PX,
            CORNER_RADIUS_PX,
            paint
        )
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: android.view.MotionEvent): Boolean {
        if (event.actionMasked == android.view.MotionEvent.ACTION_UP) {
            val cell = cellAt(event.x, event.y)
            if (cell != null) {
                onCellTapped?.invoke(cell)
                performClick()
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    private fun cellAt(x: Float, y: Float): Int? {
        val currentBoard = board ?: return null
        val rect = boardRect()
        if (!rect.contains(x, y)) return null
        val col = ((x - rect.left) / (rect.width() / currentBoard.columns))
            .toInt().coerceIn(0, currentBoard.columns - 1)
        val row = ((y - rect.top) / (rect.height() / currentBoard.rows))
            .toInt().coerceIn(0, currentBoard.rows - 1)
        return row * currentBoard.columns + col
    }

    private fun cellRect(cell: Int, cellWidth: Float, cellHeight: Float): RectF {
        val boardRect = boardRect()
        val left = boardRect.left + (cell % columns()) * cellWidth
        val top = boardRect.top + (cell / columns()) * cellHeight
        return RectF(left, top, left + cellWidth, top + cellHeight)
    }

    private fun columns(): Int = board?.columns ?: 1

    private fun boardRect(): RectF {
        val side = minOf(width, height).toFloat() - BOARD_PADDING_PX * 2
        val left = (width - side) / 2f
        val top = (height - side) / 2f
        return RectF(left, top, left + side, top + side)
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    private companion object {
        const val BOARD_PADDING_PX = 8f
        const val GRID_LINE_WIDTH_PX = 2f
        const val GRID_LINE_ALPHA = 60
        const val TILE_STROKE_WIDTH_PX = 10f
        const val SELECTION_STROKE_WIDTH_PX = 6f
        const val OBSTACLE_ALPHA = 200
        const val PATH_ALPHA = 160
        const val HIGHLIGHT_INSET_FRACTION = 0.08f
        const val CORNER_RADIUS_PX = 16f
        const val EMOJI_TEXT_SIZE_SP = 28f
        const val CAT_EMOJI = "🐱"
        const val HOME_EMOJI = "🏠"
        const val NO_WALK_INDEX = -1
    }
}
