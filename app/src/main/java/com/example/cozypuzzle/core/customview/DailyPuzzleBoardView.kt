package com.example.cozypuzzle.core.customview

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import com.example.cozypuzzle.R
import com.example.cozypuzzle.core.customview.watercolor.WatercolorBrushRenderer
import com.example.cozypuzzle.core.path.model.StrokePoint
import com.example.cozypuzzle.core.ui.BrushColor
import kotlin.math.sqrt
import kotlin.random.Random

/**
 * Board for the Daily "draw the path" mode.
 *
 * Responsibilities are strictly visual: rendering the grid/obstacles/start &
 * goal, tracking raw touch input, and painting the cached watercolor stroke
 * with its flower decorations. All rules (start validity, continuity,
 * collision, completion) live in the domain layer; this view only reports
 * normalized stroke samples in board coordinates where the board spans
 * [0, size]² and one cell is 1×1.
 */
class DailyPuzzleBoardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    /** Raw drawing input in logical board units, forwarded to the ViewModel. */
    sealed interface DrawingInput {
        data class StrokeStarted(val point: StrokePoint) : DrawingInput
        data class StrokeExtended(val points: List<StrokePoint>) : DrawingInput
        data object StrokeEnded : DrawingInput
    }

    var onDrawingInput: ((DrawingInput) -> Unit)? = null

    private var boardSize: Int = DEFAULT_BOARD_SIZE
    private var startCell: Int = NO_CELL
    private var goalCell: Int = NO_CELL
    private var obstacleCells: Set<Int> = emptySet()
    private var brushColor: BrushColor? = null
    private var pathPoints: List<StrokePoint> = emptyList()
    private var isDrawingCursorVisible: Boolean = false
    private var isCompleted: Boolean = false
    private var inputEnabled: Boolean = false

    // --- Cached stroke layer -------------------------------------------------
    private var strokeBitmap: Bitmap? = null
    private var strokeCanvas: Canvas? = null
    private var renderedUpTo: Int = 0
    private var boardSignature: Int = 0
    private var flowerDistanceCarry: Float = 0f

    private val brushRenderer = WatercolorBrushRenderer()

    // --- Reusable paints -----------------------------------------------------
    private val paperPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.white)
        alpha = PAPER_ALPHA
    }
    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.cozy_brown_text)
        alpha = GRID_ALPHA
        strokeWidth = GRID_LINE_WIDTH_PX
    }
    private val obstaclePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.cozy_brown_text)
        alpha = OBSTACLE_ALPHA
    }
    private val obstacleShadePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.cozy_cream_dark)
        alpha = OBSTACLE_SHADE_ALPHA
    }
    private val cursorPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val cursorRimPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = ContextCompat.getColor(context, R.color.white)
        strokeWidth = CURSOR_RIM_WIDTH_PX
    }
    private val goalPulsePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = ContextCompat.getColor(context, R.color.cozy_green_dark)
        strokeWidth = GOAL_PULSE_STROKE_PX
    }
    private val flashPaint = Paint().apply { style = Paint.Style.FILL }
    private val emojiTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        textSize = EMOJI_TEXT_SIZE_SP * resources.displayMetrics.scaledDensity
    }

    // --- Failure feedback ----------------------------------------------------
    private var flashAlpha: Float = 0f
    private var completionPulse: Float = 0f
    private var pulseAnimator: android.animation.ValueAnimator? = null

    /** Reusable sample buffer so touch handling allocates only on growth. */
    private var sampleBuffer: FloatArray = FloatArray(INITIAL_SAMPLE_BUFFER)

    fun render(
        boardSize: Int,
        startCell: Int,
        goalCell: Int,
        obstacles: Set<Int>,
        pathPoints: List<StrokePoint>,
        brushColor: BrushColor,
        isDrawing: Boolean,
        isCompleted: Boolean,
        inputEnabled: Boolean
    ) {
        val signature = computeSignature(boardSize, startCell, goalCell, obstacles)
        val needsFullRebuild = signature != boardSignature || strokeBitmap == null

        this.boardSize = boardSize
        this.startCell = startCell
        this.goalCell = goalCell
        this.obstacleCells = obstacles
        this.brushColor = brushColor
        this.isCompleted = isCompleted
        this.inputEnabled = inputEnabled && !isCompleted
        contentDescription = context.getString(R.string.cd_daily_board)

        if (needsFullRebuild) {
            boardSignature = signature
            renderedUpTo = 0
            flowerDistanceCarry = 0f
            ensureStrokeLayer()
        } else if (pathPoints.size < renderedUpTo) {
            // Path was cleared (failure/reset): wipe the paint layer.
            renderedUpTo = 0
            flowerDistanceCarry = 0f
            eraseStrokeLayer()
        }

        this.pathPoints = pathPoints
        if (width > 0 && height > 0) {
            paintNewSegments(pathPoints)
        }
        isDrawingCursorVisible = isDrawing

        if (isCompleted) startCompletionPulse() else stopCompletionPulse()
        invalidate()
    }

    /** Shake + soft red flash + haptic tick after an obstacle hit. */
    fun playFailureFeedback() {
        performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
        animateShake()
        flashAlpha = FLASH_MAX_ALPHA
        invalidate()
    }

    // -------------------------------------------------------------------------
    // Drawing
    // -------------------------------------------------------------------------

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        // Recreate the cache layer and replay the whole stroke deterministically.
        strokeBitmap?.recycle()
        strokeBitmap = null
        strokeCanvas = null
        renderedUpTo = 0
        flowerDistanceCarry = 0f
        if (w > 0 && h > 0) {
            ensureStrokeLayer()
            paintNewSegments(pathPoints)
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        drawPaper(canvas)
        drawGrid(canvas)
        drawObstacles(canvas)
        drawMarkers(canvas)

        strokeBitmap?.let { canvas.drawBitmap(it, 0f, 0f, null) }

        if (isDrawingCursorVisible) drawCursor(canvas)
        if (isCompleted) drawGoalPulse(canvas)

        if (flashAlpha > 0f) {
            flashPaint.color = ContextCompat.getColor(context, R.color.cozy_blossom_dark)
            flashPaint.alpha = (flashAlpha * 255f).toInt()
            canvas.drawRoundRect(boardRect(), CORNER_RADIUS_PX, CORNER_RADIUS_PX, flashPaint)
            postInvalidateOnAnimation()
        }
    }

    private fun drawPaper(canvas: Canvas) {
        canvas.drawRoundRect(boardRect(), CORNER_RADIUS_PX, CORNER_RADIUS_PX, paperPaint)
    }

    private fun drawGrid(canvas: Canvas) {
        val rect = boardRect()
        val cell = rect.width() / boardSize
        for (index in 1 until boardSize) {
            val x = rect.left + cell * index
            canvas.drawLine(x, rect.top + GRID_INSET_PX, x, rect.bottom - GRID_INSET_PX, gridPaint)
            val y = rect.top + cell * index
            canvas.drawLine(rect.left + GRID_INSET_PX, y, rect.right - GRID_INSET_PX, y, gridPaint)
        }
    }

    private fun drawObstacles(canvas: Canvas) {
        val rect = boardRect()
        val cell = rect.width() / boardSize
        obstacleCells.forEach { cellIndex ->
            val col = cellIndex % boardSize
            val row = cellIndex / boardSize
            val inset = cell * OBSTACLE_INSET_FRACTION
            val left = rect.left + col * cell + inset
            val top = rect.top + row * cell + inset
            val size = cell - inset * 2
            obstaclePaint.alpha = OBSTACLE_ALPHA
            canvas.drawRoundRect(left, top, left + size, top + size, cell * 0.3f, cell * 0.3f, obstaclePaint)
            // Soft top-left highlight for a hand-painted pebble feel.
            obstacleShadePaint.alpha = OBSTACLE_SHADE_ALPHA
            canvas.drawOval(
                left + size * 0.2f,
                top + size * 0.16f,
                left + size * 0.62f,
                top + size * 0.5f,
                obstacleShadePaint
            )
        }
    }

    private fun drawMarkers(canvas: Canvas) {
        if (startCell != NO_CELL) drawMarker(canvas, startCell, START_EMOJI)
        if (goalCell != NO_CELL) drawMarker(canvas, goalCell, GOAL_EMOJI)
    }

    private fun drawMarker(canvas: Canvas, cell: Int, emoji: String) {
        val center = centerOf(cell)
        canvas.drawText(emoji, center[0], center[1] + emojiTextPaint.textSize / 3f, emojiTextPaint)
    }

    private fun drawCursor(canvas: Canvas) {
        val last = pathPoints.lastOrNull() ?: return
        val (x, y) = toPixels(last.x, last.y)
        val radius = cellPixelSize() * CURSOR_RADIUS_FRACTION
        cursorPaint.color = brushColor?.accent ?: ContextCompat.getColor(context, R.color.cozy_blossom_dark)
        cursorPaint.alpha = CURSOR_ALPHA
        canvas.drawCircle(x, y, radius, cursorPaint)
        canvas.drawCircle(x, y, radius, cursorRimPaint)
    }

    private fun drawGoalPulse(canvas: Canvas) {
        val (x, y) = centerOf(goalCell)
        val baseRadius = cellPixelSize() * GOAL_PULSE_BASE_FRACTION
        val radius = baseRadius + completionPulse * cellPixelSize() * GOAL_PULSE_GROWTH
        goalPulsePaint.alpha = ((1f - completionPulse) * GOAL_PULSE_MAX_ALPHA).toInt()
        canvas.drawCircle(x, y, radius, goalPulsePaint)
    }

    // -------------------------------------------------------------------------
    // Incremental watercolor painting
    // -------------------------------------------------------------------------

    private fun ensureStrokeLayer() {
        if (width <= 0 || height <= 0) return
        val bitmap = strokeBitmap ?: Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            .also {
                strokeBitmap = it
                strokeCanvas = Canvas(it)
            }
        bitmap.eraseColor(android.graphics.Color.TRANSPARENT)
    }

    private fun eraseStrokeLayer() {
        strokeBitmap?.eraseColor(android.graphics.Color.TRANSPARENT)
    }

    /**
     * Paints only segments not yet baked into the cache layer. Deterministic
     * per-segment seeds mean a full replay (after rotation) reproduces the
     * exact same watercolor texture.
     */
    private fun paintNewSegments(points: List<StrokePoint>) {
        val target = points.size
        if (target <= renderedUpTo) return
        val canvas = strokeCanvas ?: return
        val color = brushColor ?: return
        val cell = cellPixelSize()
        val radius = cell / 2f

        // Anchor dab so touch-down feedback is instant, before any movement.
        if (renderedUpTo == 0) {
            val (ax, ay) = toPixels(points[0].x, points[0].y)
            brushRenderer.drawSegment(
                canvas, ax, ay, ax, ay, radius,
                color.color,
                Random(boardSignature * SEGMENT_SEED_PRIME)
            )
            renderedUpTo = 1
        }

        var previous = points[renderedUpTo - 1]
        for (index in maxOf(renderedUpTo, 1)..points.lastIndex) {
            val current = points[index]
            val (x1, y1) = toPixels(previous.x, previous.y)
            val (x2, y2) = toPixels(current.x, current.y)
            val random = Random(index * SEGMENT_SEED_PRIME + boardSignature)
            brushRenderer.drawSegment(canvas, x1, y1, x2, y2, radius, color.color, random)
            maybePlantFlowers(canvas, previous, current, index, cell, color)
            previous = current
        }
        renderedUpTo = target
    }

    /** Scatters a few cute flowers along freshly painted distance. */
    private fun maybePlantFlowers(
        canvas: Canvas,
        from: StrokePoint,
        to: StrokePoint,
        segmentIndex: Int,
        cell: Float,
        color: BrushColor
    ) {
        val dx = to.x - from.x
        val dy = to.y - from.y
        val lengthUnits = sqrt(dx * dx + dy * dy)
        if (lengthUnits <= 0f) return

        val spacing = FLOWER_SPACING_CELLS
        var traveled = flowerDistanceCarry
        var t = traveled / lengthUnits
        while (t <= 1f) {
            val random = Random(segmentIndex * FLOWER_SEED_PRIME + (traveled * 1000f).toInt())
            val fxUnit = from.x + dx * t + random.nextFloat(-FLOWER_SPREAD, FLOWER_SPREAD)
            val fyUnit = from.y + dy * t + random.nextFloat(-FLOWER_SPREAD, FLOWER_SPREAD)
            if (fxUnit in 0f..boardSize.toFloat() && fyUnit in 0f..boardSize.toFloat()) {
                val (px, py) = toPixels(fxUnit, fyUnit)
                brushRenderer.drawFlower(
                    canvas = canvas,
                    cx = px,
                    cy = py,
                    sizePx = cell * random.nextFloat(FLOWER_SIZE_MIN, FLOWER_SIZE_MAX),
                    color = color.color,
                    accent = color.accent,
                    random = random
                )
            }
            traveled += spacing
            t = traveled / lengthUnits
        }
        flowerDistanceCarry = traveled - lengthUnits
        if (flowerDistanceCarry > spacing) flowerDistanceCarry %= spacing
    }

    // -------------------------------------------------------------------------
    // Touch input
    // -------------------------------------------------------------------------

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!inputEnabled) return false
        val listener = onDrawingInput ?: return false
        val rect = boardRect()
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                parent?.requestDisallowInterceptTouchEvent(true)
                val point = toBoardUnits(event.x, event.y, rect)
                listener(DrawingInput.StrokeStarted(point))
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                val totalSamples = (event.historySize + 1) * 2
                ensureSampleCapacity(totalSamples)
                var writeIndex = 0
                for (i in 0 until event.historySize) {
                    val p = toBoardUnits(event.getHistoricalX(i), event.getHistoricalY(i), rect)
                    sampleBuffer[writeIndex++] = p.x
                    sampleBuffer[writeIndex++] = p.y
                }
                val p = toBoardUnits(event.x, event.y, rect)
                sampleBuffer[writeIndex++] = p.x
                sampleBuffer[writeIndex++] = p.y
                listener(DrawingInput.StrokeExtended(toPointList(writeIndex)))
                return true
            }
            MotionEvent.ACTION_UP -> {
                listener(DrawingInput.StrokeEnded)
                performClick()
                return true
            }
            MotionEvent.ACTION_CANCEL -> {
                listener(DrawingInput.StrokeEnded)
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    // -------------------------------------------------------------------------
    // Animations
    // -------------------------------------------------------------------------

    private fun animateShake() {
        animate().translationX(FAIL_SHAKE_AMPLITUDE_PX).setDuration(FAIL_SHAKE_DURATION_MS)
            .withEndAction {
                animate().translationX(-FAIL_SHAKE_AMPLITUDE_PX).setDuration(FAIL_SHAKE_DURATION_MS)
                    .withEndAction {
                        animate().translationX(0f).setDuration(FAIL_SHAKE_DURATION_MS).start()
                    }
                    .start()
            }
            .start()
        decayFlash()
    }

    private fun decayFlash() {
        postDelayed({
            if (flashAlpha > 0f) {
                flashAlpha = (flashAlpha - FLASH_DECAY_STEP).coerceAtLeast(0f)
                invalidate()
                decayFlash()
            }
        }, FLASH_DECAY_DELAY_MS)
    }

    private fun startCompletionPulse() {
        if (pulseAnimator?.isRunning == true) return
        pulseAnimator = android.animation.ValueAnimator.ofFloat(0f, 1f).apply {
            duration = COMPLETION_PULSE_DURATION_MS
            repeatCount = android.animation.ValueAnimator.INFINITE
            repeatMode = android.animation.ValueAnimator.RESTART
            addUpdateListener { animator ->
                completionPulse = animator.animatedValue as Float
                invalidate()
            }
            start()
        }
    }

    private fun stopCompletionPulse() {
        pulseAnimator?.cancel()
        pulseAnimator = null
        completionPulse = 0f
    }

    // -------------------------------------------------------------------------
    // Geometry helpers
    // -------------------------------------------------------------------------

    private fun boardRect(): RectF {
        val pad = BOARD_PADDING_PX.dp
        val side = minOf(width, height).toFloat() - pad * 2
        val left = (width - side) / 2f
        val top = (height - side) / 2f
        return RectF(left, top, left + side, top + side)
    }

    private fun cellPixelSize(): Float = boardRect().width() / boardSize

    private fun toPixels(unitX: Float, unitY: Float): FloatArray {
        val rect = boardRect()
        val cell = rect.width() / boardSize
        return floatArrayOf(rect.left + unitX * cell, rect.top + unitY * cell)
    }

    private fun centerOf(cell: Int): FloatArray {
        val col = cell % boardSize
        val row = cell / boardSize
        return toPixels(col + 0.5f, row + 0.5f)
    }

    private fun toBoardUnits(px: Float, py: Float, rect: RectF): StrokePoint {
        val cell = rect.width() / boardSize
        val x = ((px - rect.left) / cell).coerceIn(0f, boardSize.toFloat())
        val y = ((py - rect.top) / cell).coerceIn(0f, boardSize.toFloat())
        return StrokePoint(x, y)
    }

    private fun ensureSampleCapacity(needed: Int) {
        if (sampleBuffer.size < needed) {
            sampleBuffer = FloatArray(maxOf(needed, sampleBuffer.size * 2))
        }
    }

    private fun toPointList(count: Int): List<StrokePoint> {
        val points = ArrayList<StrokePoint>(count / 2)
        var i = 0
        while (i + 1 < count) {
            points += StrokePoint(sampleBuffer[i], sampleBuffer[i + 1])
            i += 2
        }
        return points
    }

    private fun computeSignature(size: Int, start: Int, goal: Int, obstacles: Set<Int>): Int {
        var result = size
        result = result * 31 + start
        result = result * 31 + goal
        result = result * 31 + obstacles.hashCode()
        return result
    }

    private val Float.dp: Float
        get() = this * resources.displayMetrics.density

    private fun Random.nextFloat(min: Float, max: Float): Float =
        min + nextFloat() * (max - min)

    private companion object {
        const val DEFAULT_BOARD_SIZE = 5
        const val NO_CELL = -1
        const val INITIAL_SAMPLE_BUFFER = 32
        const val START_EMOJI = "🌱"
        const val GOAL_EMOJI = "🌼"

        const val PAPER_ALPHA = 235
        const val GRID_ALPHA = 28
        const val GRID_LINE_WIDTH_PX = 1.5f
        const val GRID_INSET_PX = 4f
        const val OBSTACLE_ALPHA = 210
        const val OBSTACLE_SHADE_ALPHA = 60
        const val OBSTACLE_INSET_FRACTION = 0.18f
        const val CURSOR_RADIUS_FRACTION = 0.22f
        const val CURSOR_RIM_WIDTH_PX = 2.5f
        const val CURSOR_ALPHA = 190
        const val CORNER_RADIUS_PX = 24f
        const val BOARD_PADDING_PX = 6f
        const val EMOJI_TEXT_SIZE_SP = 22f

        const val FLASH_MAX_ALPHA = 0.25f
        const val FLASH_DECAY_STEP = 0.05f
        const val FLASH_DECAY_DELAY_MS = 24L
        const val FAIL_SHAKE_DURATION_MS = 45L
        const val FAIL_SHAKE_AMPLITUDE_PX = 10f

        const val GOAL_PULSE_STROKE_PX = 5f
        const val GOAL_PULSE_BASE_FRACTION = 0.42f
        const val GOAL_PULSE_GROWTH = 0.35f
        const val GOAL_PULSE_MAX_ALPHA = 160
        const val COMPLETION_PULSE_DURATION_MS = 1200L

        const val SEGMENT_SEED_PRIME = 7919
        const val FLOWER_SEED_PRIME = 104_729
        const val FLOWER_SPACING_CELLS = 0.85f
        const val FLOWER_SPREAD = 0.32f
        const val FLOWER_SIZE_MIN = 0.13f
        const val FLOWER_SIZE_MAX = 0.24f
    }
}
