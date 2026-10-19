package com.example.cozypuzzle.core.customview

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.ClipData
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Point
import android.graphics.Rect
import android.graphics.RectF
import android.util.AttributeSet
import android.view.DragEvent
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import com.example.cozypuzzle.R
import com.example.cozypuzzle.core.catpuzzle.model.CatPiece
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin
import kotlin.random.Random

class CatPuzzleBoardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var onSlotTapped: ((slotIndex: Int) -> Unit)? = null
    var onPiecePlacedOrMoved: ((pieceId: Int, fromSlot: Int?, targetSlot: Int) -> Unit)? = null
    var onPieceDropped: ((pieceId: Int, slotIndex: Int) -> Unit)? = null

    private var slots: List<CatPiece?> = List(9) { null }
    private var boardBitmap: Bitmap? = null
    private var currentImageRes: Int? = null
    private var hoveredSlot: Int? = null

    // Touch & Board Dragging state
    private val touchSlop = ViewConfiguration.get(context).scaledTouchSlop.toFloat()
    private var downX = 0f
    private var downY = 0f
    private var downSlot: Int? = null
    private var isDraggingFromBoard = false
    private var draggingSlot: Int? = null

    // Paints
    private val boardBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = STROKE_WIDTH_PX
        color = ContextCompat.getColor(context, R.color.cozy_card_stroke)
    }

    private val emptySlotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = ContextCompat.getColor(context, R.color.white)
    }

    private val gridDividerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = DIVIDER_WIDTH_PX
        color = ContextCompat.getColor(context, R.color.cozy_pastel_blue_light)
    }

    private val hoverSlotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = ContextCompat.getColor(context, R.color.cozy_pastel_pink_light)
    }

    private val dimmedPiecePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        alpha = 80
    }

    private val normalPiecePaint = Paint(Paint.ANTI_ALIAS_FLAG)

    // Cute Correct Animation variables
    private val slotScales = FloatArray(9) { 1.0f }
    private val slotGlowProgress = FloatArray(9) { 0f }
    private val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 8f
    }
    private val particlePaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val clipPath = Path()
    private val activeParticles = mutableListOf<SparkleParticle>()
    private var particleAnimator: ValueAnimator? = null

    init {
        setupDragListener()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = MeasureSpec.getSize(heightMeasureSpec)
        val size = if (width in 1 until height) width else height
        val squareMeasure = MeasureSpec.makeMeasureSpec(size, MeasureSpec.EXACTLY)
        super.onMeasure(squareMeasure, squareMeasure)
    }

    fun setSlots(slots: List<CatPiece?>) {
        this.slots = slots
        invalidate()
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

    fun getPieceBitmapSlice(pieceId: Int): Bitmap? {
        val full = boardBitmap ?: return null
        val pieceW = full.width / 3
        val pieceH = full.height / 3
        val pieceCol = pieceId % 3
        val pieceRow = pieceId / 3
        val srcRect = Rect(
            pieceCol * pieceW,
            pieceRow * pieceH,
            (pieceCol + 1) * pieceW,
            (pieceRow + 1) * pieceH
        )
        val slice = Bitmap.createBitmap(pieceW, pieceH, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(slice)
        canvas.drawBitmap(full, srcRect, Rect(0, 0, pieceW, pieceH), null)
        return slice
    }

    // --- Touch Handling: Tap vs Drag from Board ---
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                downX = event.x
                downY = event.y
                downSlot = getSlotAt(event.x, event.y)
                isDraggingFromBoard = false
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                val slot = downSlot
                if (slot != null && !isDraggingFromBoard) {
                    val piece = slots.getOrNull(slot)
                    if (piece != null) {
                        val dist = hypot(event.x - downX, event.y - downY)
                        if (dist > touchSlop) {
                            isDraggingFromBoard = true
                            draggingSlot = slot
                            val slice = getPieceBitmapSlice(piece.id)
                            val cellWidthPx = (width / 3f).toInt()
                            val shadow = PieceDragShadowBuilder(this, slice, cellWidthPx)
                            val clipData = ClipData.newPlainText("cat_puzzle_piece", "BOARD:$slot:${piece.id}")
                            startDragAndDrop(clipData, shadow, null, 0)
                            invalidate()
                            return true
                        }
                    }
                }
            }
            MotionEvent.ACTION_UP -> {
                if (!isDraggingFromBoard && downSlot != null) {
                    onSlotTapped?.invoke(downSlot!!)
                    performClick()
                }
                downSlot = null
                isDraggingFromBoard = false
                return true
            }
            MotionEvent.ACTION_CANCEL -> {
                downSlot = null
                isDraggingFromBoard = false
            }
        }
        return super.onTouchEvent(event)
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    // --- Drag & Drop Target Listener ---
    private fun setupDragListener() {
        setOnDragListener { _, event ->
            when (event.action) {
                DragEvent.ACTION_DRAG_STARTED -> true
                DragEvent.ACTION_DRAG_LOCATION -> {
                    val slot = getSlotAt(event.x, event.y)
                    if (slot != hoveredSlot) {
                        hoveredSlot = slot
                        invalidate()
                    }
                    true
                }
                DragEvent.ACTION_DRAG_EXITED -> {
                    hoveredSlot = null
                    invalidate()
                    true
                }
                DragEvent.ACTION_DRAG_ENDED -> {
                    hoveredSlot = null
                    draggingSlot = null
                    invalidate()
                    true
                }
                DragEvent.ACTION_DROP -> {
                    val targetSlot = getSlotAt(event.x, event.y)
                    val clipText = event.clipData?.getItemAt(0)?.text?.toString()
                    hoveredSlot = null
                    draggingSlot = null
                    invalidate()

                    if (targetSlot != null && clipText != null) {
                        if (clipText.startsWith("BOARD:")) {
                            val parts = clipText.split(":")
                            val fromSlot = parts.getOrNull(1)?.toIntOrNull()
                            val pieceId = parts.getOrNull(2)?.toIntOrNull()
                            if (pieceId != null) {
                                onPiecePlacedOrMoved?.invoke(pieceId, fromSlot, targetSlot)
                                onPieceDropped?.invoke(pieceId, targetSlot)
                                true
                            } else {
                                false
                            }
                        } else if (clipText.startsWith("TRAY:")) {
                            val pieceId = clipText.removePrefix("TRAY:").toIntOrNull()
                            if (pieceId != null) {
                                onPiecePlacedOrMoved?.invoke(pieceId, null, targetSlot)
                                onPieceDropped?.invoke(pieceId, targetSlot)
                                true
                            } else {
                                false
                            }
                        } else {
                            val pieceId = clipText.toIntOrNull()
                            if (pieceId != null) {
                                onPiecePlacedOrMoved?.invoke(pieceId, null, targetSlot)
                                onPieceDropped?.invoke(pieceId, targetSlot)
                                true
                            } else {
                                false
                            }
                        }
                    } else {
                        false
                    }
                }
                else -> true
            }
        }
    }

    private fun getSlotAt(x: Float, y: Float): Int? {
        val cellWidth = width / 3f
        val cellHeight = height / 3f
        val col = (x / cellWidth).toInt()
        val row = (y / cellHeight).toInt()
        if (col in 0..2 && row in 0..2) {
            return row * 3 + col
        }
        return null
    }

    // --- Cute & Beautiful Correct Placement Animation ---
    fun playCorrectSlotAnimation(slotIndex: Int) {
        if (slotIndex !in 0..8) return
        performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)

        // 1. Squish & Bounce on slot
        ValueAnimator.ofFloat(1.0f, 1.15f, 0.95f, 1.0f).apply {
            duration = 380
            interpolator = OvershootInterpolator(2.2f)
            addUpdateListener { anim ->
                slotScales[slotIndex] = anim.animatedValue as Float
                invalidate()
            }
            start()
        }

        // 2. Pastel Glow Expanding Ring
        ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 450
            interpolator = DecelerateInterpolator()
            addUpdateListener { anim ->
                slotGlowProgress[slotIndex] = anim.animatedValue as Float
                invalidate()
            }
            start()
        }

        // 3. Cute Sparkle Particles (Stars ✨, Hearts 💖, Paws 🐾)
        val cellW = width / 3f
        val cellH = height / 3f
        val col = slotIndex % 3
        val row = slotIndex / 3
        val centerX = col * cellW + cellW / 2f
        val centerY = row * cellH + cellH / 2f

        val colors = intArrayOf(
            Color.parseColor("#FFD166"), // Star Gold
            Color.parseColor("#FF85A1"), // Pastel Heart Pink
            Color.parseColor("#83D4F7"), // Cozy Blue
            Color.parseColor("#FFB6D2"), // Blossom Pink
            Color.WHITE
        )

        val shapes = arrayOf(
            ParticleShape.STAR,
            ParticleShape.HEART,
            ParticleShape.PAW,
            ParticleShape.STAR,
            ParticleShape.CIRCLE
        )

        for (i in 0 until 12) {
            val angle = (i * (360f / 12f) + Random.nextFloat() * 20f) * (Math.PI / 180f)
            val speed = Random.nextFloat() * 120f + 80f
            val shape = shapes[i % shapes.size]
            val color = colors[Random.nextInt(colors.size)]
            val size = Random.nextFloat() * 14f + 16f

            activeParticles.add(
                SparkleParticle(
                    x = centerX,
                    y = centerY,
                    vx = (cos(angle) * speed).toFloat(),
                    vy = (sin(angle) * speed).toFloat() - 40f, // slight upward burst
                    color = color,
                    shape = shape,
                    baseSize = size,
                    rotation = Random.nextFloat() * 360f,
                    rotSpeed = (Random.nextFloat() - 0.5f) * 360f
                )
            )
        }

        particleAnimator?.cancel()
        particleAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 600
            interpolator = DecelerateInterpolator()
            var lastTime = System.currentTimeMillis()
            addUpdateListener {
                val now = System.currentTimeMillis()
                val dt = ((now - lastTime) / 1000f).coerceIn(0.001f, 0.05f)
                lastTime = now

                val iterator = activeParticles.iterator()
                while (iterator.hasNext()) {
                    val p = iterator.next()
                    p.age += dt
                    if (p.age >= p.lifeSpan) {
                        iterator.remove()
                    } else {
                        p.x += p.vx * dt
                        p.y += p.vy * dt
                        p.vy += 120f * dt // gentle gravity
                        p.rotation += p.rotSpeed * dt
                    }
                }
                invalidate()
            }
            start()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat()
        val h = height.toFloat()
        if (w <= 0 || h <= 0) return

        val cellW = w / 3f
        val cellH = h / 3f

        // Clip board with rounded corners
        val boardRect = RectF(0f, 0f, w, h)
        clipPath.reset()
        clipPath.addRoundRect(boardRect, CORNER_RADIUS_PX, CORNER_RADIUS_PX, Path.Direction.CW)
        canvas.save()
        canvas.clipPath(clipPath)

        val bitmap = boardBitmap
        val pieceBitmapW = (bitmap?.width ?: 1) / 3
        val pieceBitmapH = (bitmap?.height ?: 1) / 3

        for (row in 0 until 3) {
            for (col in 0 until 3) {
                val slotIndex = row * 3 + col
                val slotRect = RectF(
                    col * cellW,
                    row * cellH,
                    (col + 1) * cellW,
                    (row + 1) * cellH
                )

                val piece = slots.getOrNull(slotIndex)
                val scale = slotScales[slotIndex]

                if (scale != 1.0f) {
                    canvas.save()
                    canvas.scale(scale, scale, slotRect.centerX(), slotRect.centerY())
                }

                if (piece != null && bitmap != null) {
                    val pieceCol = piece.id % 3
                    val pieceRow = piece.id / 3
                    val srcRect = Rect(
                        pieceCol * pieceBitmapW,
                        pieceRow * pieceBitmapH,
                        (pieceCol + 1) * pieceBitmapW,
                        (pieceRow + 1) * pieceBitmapH
                    )
                    // If this slot is currently being dragged, draw it dimmed
                    val paint = if (draggingSlot == slotIndex) dimmedPiecePaint else normalPiecePaint
                    canvas.drawBitmap(bitmap, srcRect, slotRect, paint)
                } else {
                    val paint = if (hoveredSlot == slotIndex) hoverSlotPaint else emptySlotPaint
                    canvas.drawRect(slotRect, paint)
                }

                if (scale != 1.0f) {
                    canvas.restore()
                }

                // Draw pastel glow expanding ring if active
                val glow = slotGlowProgress[slotIndex]
                if (glow in 0.01f..0.99f) {
                    val glowInset = glow * 16f
                    val glowRect = RectF(
                        slotRect.left - glowInset,
                        slotRect.top - glowInset,
                        slotRect.right + glowInset,
                        slotRect.bottom + glowInset
                    )
                    glowPaint.color = Color.parseColor("#FFD166")
                    glowPaint.alpha = ((1f - glow) * 220).toInt().coerceIn(0, 255)
                    canvas.drawRoundRect(glowRect, 18f, 18f, glowPaint)
                }
            }
        }

        // Draw grid divider lines
        for (i in 1..2) {
            val x = i * cellW
            canvas.drawLine(x, 0f, x, h, gridDividerPaint)
            val y = i * cellH
            canvas.drawLine(0f, y, w, y, gridDividerPaint)
        }

        // Draw Active Particles (Stars, Hearts, Paws)
        if (activeParticles.isNotEmpty()) {
            for (p in activeParticles) {
                val progress = (p.age / p.lifeSpan).coerceIn(0f, 1f)
                val alpha = ((1f - progress) * 255).toInt().coerceIn(0, 255)
                val size = p.baseSize * (1f - progress * 0.3f)
                particlePaint.color = p.color
                particlePaint.alpha = alpha

                canvas.save()
                canvas.translate(p.x, p.y)
                canvas.rotate(p.rotation)

                when (p.shape) {
                    ParticleShape.STAR -> {
                        canvas.drawPath(createStarPath(0f, 0f, size), particlePaint)
                    }
                    ParticleShape.HEART -> {
                        canvas.drawPath(createHeartPath(0f, 0f, size), particlePaint)
                    }
                    ParticleShape.PAW -> {
                        drawCutePaw(canvas, 0f, 0f, size, particlePaint)
                    }
                    ParticleShape.CIRCLE -> {
                        canvas.drawCircle(0f, 0f, size * 0.4f, particlePaint)
                    }
                }
                canvas.restore()
            }
        }

        canvas.restore()

        // Draw board outer border
        canvas.drawRoundRect(boardRect, CORNER_RADIUS_PX, CORNER_RADIUS_PX, boardBorderPaint)
    }

    private fun createStarPath(cx: Float, cy: Float, size: Float): Path {
        val p = Path()
        val s = size / 2f
        p.moveTo(cx, cy - s)
        p.quadTo(cx, cy, cx + s, cy)
        p.quadTo(cx, cy, cx, cy + s)
        p.quadTo(cx, cy, cx - s, cy)
        p.quadTo(cx, cy, cx, cy - s)
        p.close()
        return p
    }

    private fun createHeartPath(cx: Float, cy: Float, size: Float): Path {
        val p = Path()
        val w = size * 0.7f
        val h = size * 0.7f
        p.moveTo(cx, cy + h * 0.45f)
        p.cubicTo(cx - w * 0.7f, cy - h * 0.1f, cx - w * 0.5f, cy - h * 0.7f, cx, cy - h * 0.25f)
        p.cubicTo(cx + w * 0.5f, cy - h * 0.7f, cx + w * 0.7f, cy - h * 0.1f, cx, cy + h * 0.45f)
        p.close()
        return p
    }

    private fun drawCutePaw(canvas: Canvas, cx: Float, cy: Float, size: Float, paint: Paint) {
        val padRadius = size * 0.28f
        canvas.drawCircle(cx, cy + padRadius * 0.4f, padRadius, paint)
        val toeRadius = size * 0.12f
        canvas.drawCircle(cx - padRadius * 0.85f, cy - padRadius * 0.5f, toeRadius, paint)
        canvas.drawCircle(cx, cy - padRadius * 0.95f, toeRadius, paint)
        canvas.drawCircle(cx + padRadius * 0.85f, cy - padRadius * 0.5f, toeRadius, paint)
    }

    private enum class ParticleShape {
        STAR,
        HEART,
        PAW,
        CIRCLE
    }

    private data class SparkleParticle(
        var x: Float,
        var y: Float,
        var vx: Float,
        var vy: Float,
        val color: Int,
        val shape: ParticleShape,
        val baseSize: Float,
        var rotation: Float,
        val rotSpeed: Float,
        var age: Float = 0f,
        val lifeSpan: Float = 0.55f
    )

    companion object {
        private const val BITMAP_SIZE = 1080
        private const val CORNER_RADIUS_PX = 48f
        private const val STROKE_WIDTH_PX = 4f
        private const val DIVIDER_WIDTH_PX = 2f
    }
}

class PieceDragShadowBuilder(
    view: View,
    private val pieceBitmap: Bitmap?,
    private val pieceSizePx: Int
) : View.DragShadowBuilder(view) {

    private val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 5f
        color = Color.parseColor("#83D4F7")
    }

    override fun onProvideShadowMetrics(outShadowSize: Point, outShadowTouchPoint: Point) {
        val size = (pieceSizePx * 1.06f).toInt().coerceAtLeast(100)
        outShadowSize.set(size, size)
        outShadowTouchPoint.set(size / 2, size / 2)
    }

    override fun onDrawShadow(canvas: Canvas) {
        val w = canvas.width.toFloat()
        val h = canvas.height.toFloat()
        val rect = RectF(4f, 4f, w - 4f, h - 4f)
        val clipPath = Path().apply {
            addRoundRect(rect, 24f, 24f, Path.Direction.CW)
        }
        canvas.save()
        canvas.clipPath(clipPath)
        if (pieceBitmap != null) {
            canvas.drawBitmap(pieceBitmap, null, rect, shadowPaint)
        } else {
            shadowPaint.color = Color.WHITE
            canvas.drawRect(rect, shadowPaint)
        }
        canvas.restore()
        canvas.drawRoundRect(rect, 24f, 24f, borderPaint)
    }
}
