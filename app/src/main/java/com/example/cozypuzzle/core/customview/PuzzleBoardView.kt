package com.example.cozypuzzle.core.customview

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import com.example.cozypuzzle.R
import com.example.cozypuzzle.core.puzzle.model.Puzzle
import com.example.cozypuzzle.core.puzzle.model.PuzzlePiece

class PuzzleBoardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var onPieceTapped: ((pieceId: Int) -> Unit)? = null
    var onPieceDropped: ((pieceId: Int, targetPosition: Int) -> Unit)? = null

    private var puzzle: Puzzle? = null
    private var selectedPieceId: Int? = null
    private var hintPieceId: Int? = null

    private val boardPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = GRID_LINE_WIDTH_PX
        color = ContextCompat.getColor(context, R.color.cozy_brown_text)
        alpha = GRID_LINE_ALPHA
    }
    private val selectedPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = SELECTION_STROKE_WIDTH_PX
        color = ContextCompat.getColor(context, R.color.cozy_blossom)
    }
    private val hintPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = SELECTION_STROKE_WIDTH_PX
        color = ContextCompat.getColor(context, R.color.cozy_sun)
    }
    private val dropTargetPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = ContextCompat.getColor(context, R.color.cozy_green)
        alpha = DROP_TARGET_ALPHA
    }

    private var boardImage: Bitmap? = null
    private var currentImageRes: Int? = null
    private var dragPieceId: Int? = null
    private var dragOriginPosition: Int? = null
    private var dragHoverPosition: Int? = null

    fun render(puzzle: Puzzle?, selectedPieceId: Int?, hintPieceId: Int?) {
        this.puzzle = puzzle
        this.selectedPieceId = selectedPieceId
        this.hintPieceId = hintPieceId
        contentDescription = context.getString(R.string.cd_puzzle_board)
        invalidate()
    }

    fun setImageResource(@DrawableRes resId: Int?) {
        if (resId == currentImageRes) return
        currentImageRes = resId
        boardImage = resId?.let { decodeDrawableToBitmap(it) }
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val currentPuzzle = puzzle ?: return
        val board = boardRect()
        val columns = currentPuzzle.config.columns
        val rows = currentPuzzle.config.rows
        val cellWidth = board.width() / columns.toFloat()
        val cellHeight = board.height() / rows.toFloat()
        val image = boardImage

        drawDropHighlight(canvas, board, cellWidth, cellHeight)

        currentPuzzle.pieces.forEach { piece ->
            val col = piece.currentPosition % columns
            val row = piece.currentPosition / columns
            val left = board.left + col * cellWidth
            val top = board.top + row * cellHeight
            if (image != null) {
                drawPieceSlice(canvas, image, piece, left, top, cellWidth, cellHeight)
            } else {
                canvas.drawRect(left, top, left + cellWidth, top + cellHeight, dropTargetPaint)
            }

            when (piece.id) {
                hintPieceId -> canvas.drawRect(
                    left + INSET_PX,
                    top + INSET_PX,
                    left + cellWidth - INSET_PX,
                    top + cellHeight - INSET_PX,
                    hintPaint
                )
                selectedPieceId, dragPieceId -> canvas.drawRect(
                    left + INSET_PX,
                    top + INSET_PX,
                    left + cellWidth - INSET_PX,
                    top + cellHeight - INSET_PX,
                    selectedPaint
                )
            }
        }

        drawGridLines(canvas, board, columns, rows, cellWidth, cellHeight)
    }

    private fun drawPieceSlice(
        canvas: Canvas,
        image: Bitmap,
        piece: PuzzlePiece,
        left: Float,
        top: Float,
        cellWidth: Float,
        cellHeight: Float
    ) {
        val config = puzzle?.config ?: return
        val srcCol = piece.targetPosition % config.columns
        val srcRow = piece.targetPosition / config.columns
        val src = Rect(
            (srcCol * image.width / config.columns),
            (srcRow * image.height / config.rows),
            ((srcCol + 1) * image.width / config.columns),
            ((srcRow + 1) * image.height / config.rows)
        )
        val cx = left + cellWidth / 2f
        val cy = top + cellHeight / 2f
        canvas.save()
        canvas.rotate(piece.rotation.toFloat(), cx, cy)
        canvas.drawBitmap(
            image,
            src,
            RectF(left, top, left + cellWidth, top + cellHeight),
            null
        )
        canvas.restore()
    }

    private fun drawGridLines(
        canvas: Canvas,
        board: RectF,
        columns: Int,
        rows: Int,
        cellWidth: Float,
        cellHeight: Float
    ) {
        for (col in 1 until columns) {
            val x = board.left + col * cellWidth
            canvas.drawLine(x, board.top, x, board.bottom, boardPaint)
        }
        for (row in 1 until rows) {
            val y = board.top + row * cellHeight
            canvas.drawLine(board.left, y, board.right, y, boardPaint)
        }
        canvas.drawRect(board, boardPaint)
    }

    private fun drawDropHighlight(
        canvas: Canvas,
        board: RectF,
        cellWidth: Float,
        cellHeight: Float
    ) {
        val hover = dragHoverPosition ?: return
        val origin = dragOriginPosition
        if (hover == origin) return
        val columns = puzzle?.config?.columns ?: return
        val left = board.left + (hover % columns) * cellWidth
        val top = board.top + (hover / columns) * cellHeight
        canvas.drawRect(left, top, left + cellWidth, top + cellHeight, dropTargetPaint)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val currentPuzzle = puzzle ?: return false
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                val position = positionAt(event.x, event.y) ?: return false
                dragPieceId = currentPuzzle.pieces
                    .firstOrNull { it.currentPosition == position }?.id
                dragOriginPosition = position
                dragHoverPosition = position
                parent?.requestDisallowInterceptTouchEvent(true)
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                val id = dragPieceId ?: return false
                val hover = positionAt(event.x, event.y)
                if (hover != dragHoverPosition) {
                    dragHoverPosition = hover
                    invalidate()
                }
                return true
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                val id = dragPieceId
                val origin = dragOriginPosition
                val target = dragHoverPosition
                dragPieceId = null
                dragOriginPosition = null
                dragHoverPosition = null
                if (event.actionMasked == MotionEvent.ACTION_CANCEL || id == null || origin == null) {
                    invalidate()
                    return true
                }
                if (target != null && target != origin) {
                    onPieceDropped?.invoke(id, target)
                } else {
                    onPieceTapped?.invoke(id)
                }
                invalidate()
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    private fun positionAt(x: Float, y: Float): Int? {
        val currentPuzzle = puzzle ?: return null
        val board = boardRect()
        if (!board.contains(x, y)) return null
        val columns = currentPuzzle.config.columns
        val rows = currentPuzzle.config.rows
        val col = ((x - board.left) / (board.width() / columns)).toInt().coerceIn(0, columns - 1)
        val row = ((y - board.top) / (board.height() / rows)).toInt().coerceIn(0, rows - 1)
        return row * columns + col
    }

    private fun boardRect(): RectF {
        val side = minOf(width, height).toFloat() - BOARD_PADDING_PX * 2
        val left = (width - side) / 2f
        val top = (height - side) / 2f
        return RectF(left, top, left + side, top + side)
    }

    private fun decodeDrawableToBitmap(@DrawableRes resId: Int): Bitmap {
        val size = BITMAP_RESOLUTION_PX
        val drawable = ContextCompat.getDrawable(context, resId)
            ?: throw IllegalArgumentException("Unknown puzzle image resource $resId")
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, size, size)
        drawable.draw(canvas)
        return bitmap
    }

    private companion object {
        const val BOARD_PADDING_PX = 8f
        const val GRID_LINE_WIDTH_PX = 2f
        const val GRID_LINE_ALPHA = 90
        const val SELECTION_STROKE_WIDTH_PX = 6f
        const val INSET_PX = 6f
        const val DROP_TARGET_ALPHA = 60
        const val BITMAP_RESOLUTION_PX = 1024
    }
}
