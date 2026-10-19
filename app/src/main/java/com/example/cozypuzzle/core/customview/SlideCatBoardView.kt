package com.example.cozypuzzle.core.customview

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import com.example.cozypuzzle.R
import com.example.cozypuzzle.core.slidethecat.model.SlideTile
import kotlin.math.abs

class SlideCatBoardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var onTileClicked: ((slotIndex: Int) -> Unit)? = null

    var gridSize: Int = 3
        set(value) {
            if (field != value) {
                field = value
                invalidate()
            }
        }

    private var board: List<SlideTile?> = emptyList()
    private var boardBitmap: Bitmap? = null
    private var currentImageRes: Int? = null

    var isCompleted: Boolean = false
        set(value) {
            field = value
            invalidate()
        }

    var showTileNumbers: Boolean = false
        set(value) {
            field = value
            invalidate()
        }

    // Animation state
    private var animatingTile: SlideTile? = null
    private var animFromRect = RectF()
    private var animToRect = RectF()
    private var animCurrentRect = RectF()
    private var isAnimating = false
    private var animator: ValueAnimator? = null

    private var previousBoard: List<SlideTile?> = emptyList()

    private val boardBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 3f
        color = ContextCompat.getColor(context, R.color.cozy_card_stroke)
    }

    private val emptySlotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = ContextCompat.getColor(context, R.color.cozy_pastel_blue_container)
    }

    private val emptySlotBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2f
        color = ContextCompat.getColor(context, R.color.cozy_pastel_blue_light)
    }

    private val tileBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2f
        color = ContextCompat.getColor(context, R.color.cozy_card_stroke)
    }

    private val numberBadgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = 0xCC1E3A5F.toInt()
    }

    private val numberTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFFFFFFF.toInt()
        textSize = 28f
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
    }

    private val tileClipPath = Path()
    private val boardClipPath = Path()

    private var touchDownX = 0f
    private var touchDownY = 0f

    init {
        isClickable = true
        isFocusable = true
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = MeasureSpec.getSize(heightMeasureSpec)
        val size = if (width in 1 until height) width else height
        val squareMeasure = MeasureSpec.makeMeasureSpec(size, MeasureSpec.EXACTLY)
        super.onMeasure(squareMeasure, squareMeasure)
    }

    fun setImageResource(@DrawableRes resId: Int) {
        if (resId == currentImageRes && boardBitmap != null) return
        currentImageRes = resId

        val drawable = ContextCompat.getDrawable(context, resId) ?: return
        val bitmap = Bitmap.createBitmap(BITMAP_SIZE, BITMAP_SIZE, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, BITMAP_SIZE, BITMAP_SIZE)
        drawable.draw(canvas)
        this.boardBitmap = bitmap
        invalidate()
    }

    fun setBoardState(newBoard: List<SlideTile?>, animate: Boolean = true) {
        if (animate && previousBoard.isNotEmpty() && previousBoard.size == newBoard.size) {
            detectAndStartAnimation(previousBoard, newBoard)
        }
        this.previousBoard = newBoard
        this.board = newBoard
        invalidate()
    }

    private fun detectAndStartAnimation(oldBoard: List<SlideTile?>, newBoard: List<SlideTile?>) {
        val oldEmpty = oldBoard.indexOfFirst { it == null }
        val newEmpty = newBoard.indexOfFirst { it == null }

        if (oldEmpty != -1 && newEmpty != -1 && oldEmpty != newEmpty) {
            val movedTile = newBoard[oldEmpty]
            if (movedTile != null && width > 0 && height > 0) {
                val fromSlot = newEmpty
                val toSlot = oldEmpty

                computeSlotRect(fromSlot, animFromRect)
                computeSlotRect(toSlot, animToRect)
                animatingTile = movedTile
                isAnimating = true

                animator?.cancel()
                animator = ValueAnimator.ofFloat(0f, 1f).apply {
                    duration = 120L
                    interpolator = DecelerateInterpolator()
                    addUpdateListener { va ->
                        val fraction = va.animatedFraction
                        animCurrentRect.set(
                            lerp(animFromRect.left, animToRect.left, fraction),
                            lerp(animFromRect.top, animToRect.top, fraction),
                            lerp(animFromRect.right, animToRect.right, fraction),
                            lerp(animFromRect.bottom, animToRect.bottom, fraction)
                        )
                        invalidate()
                    }
                    start()
                }
            }
        }
    }

    private fun computeSlotRect(slotIndex: Int, outRect: RectF) {
        val total = gridSize
        val w = width.toFloat()
        val h = height.toFloat()
        val cellW = w / total
        val cellH = h / total

        val row = slotIndex / total
        val col = slotIndex % total

        val left = col * cellW + TILE_GAP
        val top = row * cellH + TILE_GAP
        val right = (col + 1) * cellW - TILE_GAP
        val bottom = (row + 1) * cellH - TILE_GAP
        outRect.set(left, top, right, bottom)
    }

    private fun lerp(start: Float, end: Float, fraction: Float): Float =
        start + (end - start) * fraction

    private var hasSwipedInGesture = false

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isEnabled || isCompleted) return super.onTouchEvent(event)

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                touchDownX = event.x
                touchDownY = event.y
                hasSwipedInGesture = false
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                if (!hasSwipedInGesture) {
                    val dx = event.x - touchDownX
                    val dy = event.y - touchDownY
                    val distance = kotlin.math.hypot(dx, dy)
                    val swipeThreshold = 26f * resources.displayMetrics.density
                    if (distance >= swipeThreshold) {
                        hasSwipedInGesture = true
                        handleSwipeGesture(dx, dy)
                    }
                }
                return true
            }
            MotionEvent.ACTION_UP -> {
                if (!hasSwipedInGesture) {
                    val dx = abs(event.x - touchDownX)
                    val dy = abs(event.y - touchDownY)
                    if (dx <= TOUCH_SLOP && dy <= TOUCH_SLOP) {
                        val slot = getSlotAt(event.x, event.y)
                        if (slot != null) {
                            performClick()
                            onTileClicked?.invoke(slot)
                        }
                    }
                }
                hasSwipedInGesture = false
                return true
            }
            MotionEvent.ACTION_CANCEL -> {
                hasSwipedInGesture = false
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    private fun handleSwipeGesture(dx: Float, dy: Float) {
        val emptyIndex = board.indexOfFirst { it == null }
        if (emptyIndex == -1 || gridSize <= 0) return

        val emptyRow = emptyIndex / gridSize
        val emptyCol = emptyIndex % gridSize

        val targetSlot: Int? = if (abs(dx) > abs(dy)) {
            // Horizontal swipe:
            // Swiping right moves the tile to the left of empty space into it
            if (dx > 0 && emptyCol > 0) {
                emptyRow * gridSize + (emptyCol - 1)
            } else if (dx < 0 && emptyCol < gridSize - 1) {
                // Swiping left moves the tile to the right of empty space into it
                emptyRow * gridSize + (emptyCol + 1)
            } else {
                null
            }
        } else {
            // Vertical swipe:
            // Swiping down moves the tile above empty space into it
            if (dy > 0 && emptyRow > 0) {
                (emptyRow - 1) * gridSize + emptyCol
            } else if (dy < 0 && emptyRow < gridSize - 1) {
                // Swiping up moves the tile below empty space into it
                (emptyRow + 1) * gridSize + emptyCol
            } else {
                null
            }
        }

        if (targetSlot != null) {
            performClick()
            onTileClicked?.invoke(targetSlot)
        }
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    private fun getSlotAt(x: Float, y: Float): Int? {
        if (width <= 0 || height <= 0) return null
        val cellW = width.toFloat() / gridSize
        val cellH = height.toFloat() / gridSize

        val col = (x / cellW).toInt()
        val row = (y / cellH).toInt()

        if (col in 0 until gridSize && row in 0 until gridSize) {
            return row * gridSize + col
        }
        return null
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat()
        val h = height.toFloat()
        if (w <= 0 || h <= 0) return

        val total = gridSize
        val cellW = w / total
        val cellH = h / total

        // Draw board container rounded clipping
        val boardRect = RectF(0f, 0f, w, h)
        boardClipPath.reset()
        boardClipPath.addRoundRect(boardRect, BOARD_CORNER_RADIUS, BOARD_CORNER_RADIUS, Path.Direction.CW)
        canvas.save()
        canvas.clipPath(boardClipPath)

        val bitmap = boardBitmap
        val pieceBitmapW = (bitmap?.width ?: BITMAP_SIZE) / total
        val pieceBitmapH = (bitmap?.height ?: BITMAP_SIZE) / total

        val targetTileRect = RectF()
        val srcRect = Rect()

        for (row in 0 until total) {
            for (col in 0 until total) {
                val slotIndex = row * total + col
                val tile = board.getOrNull(slotIndex)

                val left = col * cellW + TILE_GAP
                val top = row * cellH + TILE_GAP
                val right = (col + 1) * cellW - TILE_GAP
                val bottom = (row + 1) * cellH - TILE_GAP
                targetTileRect.set(left, top, right, bottom)

                if (tile == null) {
                    if (isCompleted && bitmap != null) {
                        // Solved: draw the missing final tile piece so the picture is 100% complete!
                        val lastId = total * total - 1
                        val lastCol = lastId % total
                        val lastRow = lastId / total
                        srcRect.set(
                            lastCol * pieceBitmapW,
                            lastRow * pieceBitmapH,
                            (lastCol + 1) * pieceBitmapW,
                            (lastRow + 1) * pieceBitmapH
                        )
                        drawTile(canvas, bitmap, srcRect, targetTileRect, lastId)
                    } else {
                        // Empty slot
                        canvas.drawRoundRect(targetTileRect, TILE_CORNER_RADIUS, TILE_CORNER_RADIUS, emptySlotPaint)
                        canvas.drawRoundRect(targetTileRect, TILE_CORNER_RADIUS, TILE_CORNER_RADIUS, emptySlotBorderPaint)
                    }
                } else {
                    // Skip drawing if this tile is currently animating (drawn separately on top)
                    if (isAnimating && animatingTile?.id == tile.id) {
                        // Draw empty placeholder beneath animating tile
                        canvas.drawRoundRect(targetTileRect, TILE_CORNER_RADIUS, TILE_CORNER_RADIUS, emptySlotPaint)
                        continue
                    }

                    if (bitmap != null) {
                        val pieceCol = tile.id % total
                        val pieceRow = tile.id / total
                        srcRect.set(
                            pieceCol * pieceBitmapW,
                            pieceRow * pieceBitmapH,
                            (pieceCol + 1) * pieceBitmapW,
                            (pieceRow + 1) * pieceBitmapH
                        )
                        drawTile(canvas, bitmap, srcRect, targetTileRect, tile.id)
                    }
                }
            }
        }

        // Draw animating tile on top
        if (isAnimating && animatingTile != null && bitmap != null) {
            val tile = animatingTile!!
            val pieceCol = tile.id % total
            val pieceRow = tile.id / total
            srcRect.set(
                pieceCol * pieceBitmapW,
                pieceRow * pieceBitmapH,
                (pieceCol + 1) * pieceBitmapW,
                (pieceRow + 1) * pieceBitmapH
            )
            drawTile(canvas, bitmap, srcRect, animCurrentRect, tile.id)
        }

        canvas.restore()

        // Outer board border
        canvas.drawRoundRect(boardRect, BOARD_CORNER_RADIUS, BOARD_CORNER_RADIUS, boardBorderPaint)
    }

    private fun drawTile(
        canvas: Canvas,
        bitmap: Bitmap,
        srcRect: Rect,
        destRect: RectF,
        tileId: Int
    ) {
        tileClipPath.reset()
        tileClipPath.addRoundRect(destRect, TILE_CORNER_RADIUS, TILE_CORNER_RADIUS, Path.Direction.CW)
        canvas.save()
        canvas.clipPath(tileClipPath)

        canvas.drawBitmap(bitmap, srcRect, destRect, null)
        canvas.drawRoundRect(destRect, TILE_CORNER_RADIUS, TILE_CORNER_RADIUS, tileBorderPaint)

        // Draw tile number badge if Hint is enabled
        if (showTileNumbers) {
            val badgeSize = 36f
            val badgeRect = RectF(
                destRect.left + 6f,
                destRect.top + 6f,
                destRect.left + 6f + badgeSize,
                destRect.top + 6f + badgeSize
            )
            canvas.drawRoundRect(badgeRect, 10f, 10f, numberBadgePaint)
            val numStr = (tileId + 1).toString()
            val textY = badgeRect.centerY() - (numberTextPaint.descent() + numberTextPaint.ascent()) / 2f
            canvas.drawText(numStr, badgeRect.centerX(), textY, numberTextPaint)
        }

        canvas.restore()
    }

    override fun onDetachedFromWindow() {
        animator?.cancel()
        animator = null
        super.onDetachedFromWindow()
    }

    companion object {
        private const val BITMAP_SIZE = 1080
        private const val BOARD_CORNER_RADIUS = 32f
        private const val TILE_CORNER_RADIUS = 18f
        private const val TILE_GAP = 3.5f
        private const val TOUCH_SLOP = 24f
    }
}
