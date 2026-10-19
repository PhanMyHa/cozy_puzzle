package com.example.cozypuzzle.core.customview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.annotation.SuppressLint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import com.example.cozypuzzle.R
import com.example.cozypuzzle.core.ui.BrushColor
import kotlin.math.min

/**
 * Small, cute and reusable color palette: a row of round watercolor swatches.
 *
 * The palette is driven entirely by [BrushColor] entries so new colors can be
 * added in [com.example.cozypuzzle.core.ui.BrushPalette] without touching any
 * drawing code.
 */
class ColorPaletteView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var onColorSelected: ((BrushColor) -> Unit)? = null

    private var brushes: List<BrushColor> = emptyList()
    private var selectedId: String? = null

    private val swatchPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val rimPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = ContextCompat.getColor(context, R.color.cozy_brown_text)
        strokeWidth = SELECTED_RIM_WIDTH_PX
    }

    fun submit(brushes: List<BrushColor>, selectedId: String?) {
        this.brushes = brushes
        this.selectedId = selectedId ?: brushes.firstOrNull()?.id
        contentDescription = context.getString(R.string.cd_daily_palette)
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (brushes.isEmpty()) return
        val count = brushes.size
        val slot = width.toFloat() / count
        val radius = min(slot, height.toFloat()) * SWATCH_RADIUS_FRACTION
        val cy = height / 2f

        brushes.forEachIndexed { index, brush ->
            val cx = slot * index + slot / 2f
            swatchPaint.color = brush.color
            canvas.drawCircle(cx, cy, radius, swatchPaint)

            if (brush.id == selectedId) {
                // A soft hand-drawn ring around the active color.
                canvas.drawCircle(cx, cy, radius + SELECTED_RING_GAP_PX, rimPaint)
            }
        }
    }

    private fun swatchIndexAt(x: Float): Int {
        if (brushes.isEmpty() || width == 0) return -1
        val index = (x / (width.toFloat() / brushes.size)).toInt()
        return index.coerceIn(-1, brushes.size - 1).takeIf { it >= 0 } ?: -1
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> return true
            MotionEvent.ACTION_UP -> {
                val index = swatchIndexAt(event.x)
                val brush = brushes.getOrNull(index)
                if (brush != null) {
                    selectedId = brush.id
                    performClick()
                    invalidate()
                    onColorSelected?.invoke(brush)
                    return true
                }
            }
        }
        return super.onTouchEvent(event)
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    private companion object {
        const val SWATCH_RADIUS_FRACTION = 0.32f
        const val SELECTED_RIM_WIDTH_PX = 3f
        const val SELECTED_RING_GAP_PX = 4f
    }
}
