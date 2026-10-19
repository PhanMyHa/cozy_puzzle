package com.example.cozypuzzle.core.customview

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import com.example.cozypuzzle.R
import com.example.cozypuzzle.domain.model.GardenItem
import kotlin.math.abs
import kotlin.math.min

/**
 * Renders the cozy garden lawn and its decorations.
 * Items are stored with normalized coordinates (0..1) so the garden scales
 * across screen sizes. The view forwards raw interactions only; persistence
 * and rules live in the ViewModel/UseCase layer.
 */
class GardenCanvasView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var onItemSelected: ((String) -> Unit)? = null
    var onItemMoved: ((String, Float, Float) -> Unit)? = null
    var onEmptyTapped: (() -> Unit)? = null

    private var items: List<GardenItem> = emptyList()
    private var selectedItemId: String? = null
    private var isEditMode: Boolean = false

    private var draggedItemId: String? = null
    private var dragOffsetX = 0f
    private var dragOffsetY = 0f
    private var dragMoved = false

    private val lawnPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.cozy_green_container)
    }
    private val lawnStripePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.cozy_green)
        alpha = LAWN_STRIPE_ALPHA
    }
    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.cozy_brown_text)
        alpha = GRID_LINE_ALPHA
        strokeWidth = GRID_LINE_WIDTH_PX
    }
    private val selectionPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.cozy_blossom)
        style = Paint.Style.STROKE
        strokeWidth = SELECTION_STROKE_WIDTH_PX
    }
    private val emojiTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
    }

    fun render(items: List<GardenItem>, selectedItemId: String?, isEditMode: Boolean) {
        this.items = items
        this.selectedItemId = selectedItemId
        this.isEditMode = isEditMode
        if (!isEditMode) draggedItemId = null
        contentDescription = context.getString(R.string.cd_garden_canvas)
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val rect = lawnRect()
        canvas.drawRoundRect(rect, LAWN_CORNER_RADIUS_PX, LAWN_CORNER_RADIUS_PX, lawnPaint)

        for (row in 0 until LAWN_ROWS) {
            if (row % 2 == 0) continue
            val top = rect.top + row * (rect.height() / LAWN_ROWS)
            canvas.drawRect(
                rect.left, top, rect.right,
                min(top + rect.height() / LAWN_ROWS, rect.bottom),
                lawnStripePaint
            )
        }
        for (row in 1 until LAWN_ROWS) {
            val y = rect.top + row * (rect.height() / LAWN_ROWS)
            canvas.drawLine(rect.left, y, rect.right, y, gridPaint)
        }
        for (col in 1 until LAWN_COLUMNS) {
            val x = rect.left + col * (rect.width() / LAWN_COLUMNS)
            canvas.drawLine(x, rect.top, x, rect.bottom, gridPaint)
        }

        items.forEach { item ->
            val center = itemCenter(item)
            val size = emojiSize(item.scale)
            emojiTextPaint.textSize = size
            if (item.id == selectedItemId) {
                selectionPaint.strokeWidth =
                    SELECTION_STROKE_WIDTH_PX * (size / DEFAULT_EMOJI_SIZE_PX)
                canvas.drawCircle(center.x, center.y, size * SELECTION_RADIUS_FACTOR, selectionPaint)
            }
            canvas.save()
            canvas.rotate(item.rotation, center.x, center.y)
            canvas.drawText(
                itemTypeEmoji(item.itemType),
                center.x,
                center.y + size / EMOJI_BASELINE_FACTOR,
                emojiTextPaint
            )
            canvas.restore()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                val hit = hitItem(event.x, event.y)
                if (hit != null) {
                    draggedItemId = hit.id
                    dragMoved = false
                    val center = itemCenter(hit)
                    dragOffsetX = event.x - center.x
                    dragOffsetY = event.y - center.y
                    onItemSelected?.invoke(hit.id)
                    return true
                } else {
                    onEmptyTapped?.invoke()
                }
            }
            MotionEvent.ACTION_MOVE -> {
                val id = draggedItemId ?: return true
                if (!isEditMode) return true
                val item = items.firstOrNull { it.id == id } ?: return true
                val newX = ((event.x - dragOffsetX - lawnRect().left) / lawnRect().width())
                    .coerceIn(0f, 1f)
                val newY = ((event.y - dragOffsetY - lawnRect().top) / lawnRect().height())
                    .coerceIn(0f, 1f)
                if (abs(newX - item.x) > DRAG_THRESHOLD || abs(newY - item.y) > DRAG_THRESHOLD) {
                    dragMoved = true
                }
                onItemMoved?.invoke(id, newX, newY)
                return true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                draggedItemId = null
                performClick()
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    private fun hitItem(x: Float, y: Float): GardenItem? =
        items.maxByOrNull { item ->
            val center = itemCenter(item)
            val distance = Math.hypot((x - center.x).toDouble(), (y - center.y).toDouble())
            if (distance <= emojiSize(item.scale) * HIT_RADIUS_FACTOR) {
                -distance
            } else {
                Double.MAX_VALUE
            }
        }?.takeIf { item ->
            val center = itemCenter(item)
            val distance = Math.hypot((x - center.x).toDouble(), (y - center.y).toDouble())
            distance <= emojiSize(item.scale) * HIT_RADIUS_FACTOR
        }

    private fun itemCenter(item: GardenItem): RectFPoint {
        val rect = lawnRect()
        return RectFPoint(
            x = rect.left + item.x * rect.width(),
            y = rect.top + item.y * rect.height()
        )
    }

    private fun emojiSize(scale: Float): Float =
        DEFAULT_EMOJI_SIZE_PX * scale.coerceAtLeast(MIN_SCALE)

    private fun lawnRect(): RectF {
        val side = minOf(width, height).toFloat() - LAWN_MARGIN_PX * 2
        val left = (width - side) / 2f
        val top = (height - side) / 2f
        return RectF(left, top, left + side, top + side)
    }

    private fun itemTypeEmoji(itemType: String): String =
        ITEM_EMOJI[itemType] ?: DEFAULT_EMOJI

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    private data class RectFPoint(val x: Float, val y: Float)

    companion object {
        const val ITEM_FLOWER_TULIP = "flower_tulip"
        const val ITEM_FLOWER_SAKURA = "flower_sakura"
        const val ITEM_TREE = "tree"
        const val ITEM_BUSH = "bush"
        const val ITEM_MUSHROOM = "mushroom"
        const val ITEM_ROCK = "rock"
        const val ITEM_HOUSE = "house"

        val PALETTE_ITEMS: List<String> = listOf(
            ITEM_FLOWER_TULIP,
            ITEM_FLOWER_SAKURA,
            ITEM_TREE,
            ITEM_BUSH,
            ITEM_MUSHROOM,
            ITEM_ROCK,
            ITEM_HOUSE
        )

        const val DEFAULT_ITEM_SCALE = 1f
        const val MIN_SCALE = 0.4f

        private const val DEFAULT_EMOJI = "🌼"

        private val ITEM_EMOJI: Map<String, String> = mapOf(
            ITEM_FLOWER_TULIP to "🌷",
            ITEM_FLOWER_SAKURA to "🌸",
            ITEM_TREE to "🌳",
            ITEM_BUSH to "🌿",
            ITEM_MUSHROOM to "🍄",
            ITEM_ROCK to "🪨",
            ITEM_HOUSE to "🏡"
        )

        private const val LAWN_CORNER_RADIUS_PX = 24f
        private const val LAWN_MARGIN_PX = 16f
        private const val LAWN_STRIPE_ALPHA = 60
        private const val GRID_LINE_ALPHA = 40
        private const val GRID_LINE_WIDTH_PX = 1.5f
        private const val SELECTION_STROKE_WIDTH_PX = 3f
        private const val LAWN_ROWS = 6
        private const val LAWN_COLUMNS = 6
        private const val DEFAULT_EMOJI_SIZE_PX = 48f
        private const val EMOJI_BASELINE_FACTOR = 3f
        private const val SELECTION_RADIUS_FACTOR = 0.75f
        private const val HIT_RADIUS_FACTOR = 0.9f
        private const val DRAG_THRESHOLD = 0.005f
    }
}
