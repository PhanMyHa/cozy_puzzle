package com.example.cozypuzzle.core.customview

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Region
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.core.graphics.PathParser
import com.example.cozypuzzle.core.colorthecat.model.ColorCatLevel
import com.example.cozypuzzle.core.colorthecat.model.ColorableRegion
import com.example.cozypuzzle.core.colorthecat.model.DecorativePath
import kotlin.math.abs
import kotlin.math.min

class SvgColoringView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private data class ParsedRegion(
        val region: ColorableRegion,
        val path: Path,
        val bounds: RectF,
        val hitRegion: Region,
        val area: Float
    )

    private data class ParsedDecorative(
        val decorative: DecorativePath,
        val path: Path
    )

    private var parsedRegions: List<ParsedRegion> = emptyList()
    private var parsedDecoratives: List<ParsedDecorative> = emptyList()

    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
        color = DEFAULT_OUTLINE_COLOR
    }

    private val hintPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = DEFAULT_HINT_COLOR
    }

    private val hintStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
        color = 0xFFFFD54F.toInt()
        strokeWidth = 6f
    }

    private val decorativePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    private val drawMatrix = Matrix()
    private val inverseMatrix = Matrix()
    private val touchPoint = FloatArray(2)

    private var touchDownX = 0f
    private var touchDownY = 0f

    private var hintAlphaAnimator: ValueAnimator? = null
    private var hintAlpha = 0.5f

    var level: ColorCatLevel? = null
        set(value) {
            field = value
            parseLevelData(value)
            updateMatrix()
            invalidate()
        }

    var regionColors: Map<String, Int> = emptyMap()
        set(value) {
            field = value
            invalidate()
        }

    var highlightedRegionId: String? = null
        set(value) {
            val changed = field != value
            field = value
            if (changed) {
                if (value != null) {
                    startHintAnimation()
                } else {
                    stopHintAnimation()
                }
                invalidate()
            }
        }

    var onRegionClicked: ((String) -> Unit)? = null

    init {
        isClickable = true
        isFocusable = true
    }

    @Suppress("DEPRECATION")
    private fun parseLevelData(level: ColorCatLevel?) {
        if (level == null) {
            parsedRegions = emptyList()
            parsedDecoratives = emptyList()
            return
        }

        parsedRegions = level.regions.mapNotNull { region ->
            val path = runCatching {
                PathParser.createPathFromPathData(region.pathData)
            }.getOrNull() ?: return@mapNotNull null

            val bounds = RectF()
            path.computeBounds(bounds, true)

            val hitRegion = Region()
            val clipRect = Region(
                (bounds.left - 1).toInt(),
                (bounds.top - 1).toInt(),
                (bounds.right + 1).toInt(),
                (bounds.bottom + 1).toInt()
            )
            hitRegion.setPath(path, clipRect)

            val area = bounds.width() * bounds.height()
            ParsedRegion(region, path, bounds, hitRegion, area)
        }

        parsedDecoratives = level.decorativePaths.mapNotNull { dec ->
            val path = runCatching {
                PathParser.createPathFromPathData(dec.pathData)
            }.getOrNull() ?: return@mapNotNull null
            ParsedDecorative(dec, path)
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        updateMatrix()
    }

    private fun updateMatrix() {
        val currentLevel = level ?: return
        if (width <= 0 || height <= 0) return

        val vbWidth = currentLevel.viewBoxWidth.coerceAtLeast(1f)
        val vbHeight = currentLevel.viewBoxHeight.coerceAtLeast(1f)

        val padding = paddingLeft.toFloat()
        val availW = (width - paddingLeft - paddingRight).toFloat().coerceAtLeast(1f)
        val availH = (height - paddingTop - paddingBottom).toFloat().coerceAtLeast(1f)

        val scale = min(availW / vbWidth, availH / vbHeight)
        val dx = paddingLeft + (availW - vbWidth * scale) / 2f
        val dy = paddingTop + (availH - vbHeight * scale) / 2f

        drawMatrix.reset()
        drawMatrix.postScale(scale, scale)
        drawMatrix.postTranslate(dx, dy)

        drawMatrix.invert(inverseMatrix)
    }

    private fun startHintAnimation() {
        hintAlphaAnimator?.cancel()
        hintAlphaAnimator = ValueAnimator.ofFloat(0.25f, 0.85f).apply {
            duration = 500
            repeatMode = ValueAnimator.REVERSE
            repeatCount = ValueAnimator.INFINITE
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener { anim ->
                hintAlpha = anim.animatedValue as Float
                invalidate()
            }
            start()
        }
    }

    private fun stopHintAnimation() {
        hintAlphaAnimator?.cancel()
        hintAlphaAnimator = null
        hintAlpha = 0.5f
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isEnabled || level == null) return super.onTouchEvent(event)

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                touchDownX = event.x
                touchDownY = event.y
                return true
            }
            MotionEvent.ACTION_UP -> {
                val dx = abs(event.x - touchDownX)
                val dy = abs(event.y - touchDownY)
                if (dx <= TOUCH_SLOP && dy <= TOUCH_SLOP) {
                    handleTap(event.x, event.y)
                }
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    private fun handleTap(screenX: Float, screenY: Float) {
        touchPoint[0] = screenX
        touchPoint[1] = screenY
        inverseMatrix.mapPoints(touchPoint)

        val svgX = touchPoint[0].toInt()
        val svgY = touchPoint[1].toInt()

        // Find matching regions containing this point
        val matches = parsedRegions.filter { parsed ->
            parsed.hitRegion.contains(svgX, svgY)
        }

        if (matches.isNotEmpty()) {
            // Pick region with smallest area (so inner details/eyes take priority over face)
            val bestMatch = matches.minByOrNull { it.area } ?: matches.first()
            performClick()
            onRegionClicked?.invoke(bestMatch.region.id)
        }
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (parsedRegions.isEmpty()) return

        canvas.save()
        canvas.concat(drawMatrix)

        // 1. Draw Fills
        for (parsed in parsedRegions) {
            val color = regionColors[parsed.region.id] ?: parsed.region.defaultColor
            fillPaint.color = color
            canvas.drawPath(parsed.path, fillPaint)
        }

        // 2. Draw Hint Glow (if any region is highlighted)
        val highlighted = highlightedRegionId
        if (highlighted != null) {
            val target = parsedRegions.firstOrNull { it.region.id == highlighted }
            if (target != null) {
                // Pulse glow fill
                hintPaint.alpha = (hintAlpha * 255).toInt()
                canvas.drawPath(target.path, hintPaint)

                // Pulse glow stroke
                hintStrokePaint.alpha = (hintAlpha * 255).toInt()
                canvas.drawPath(target.path, hintStrokePaint)
            }
        }

        // 3. Draw Outlines
        for (parsed in parsedRegions) {
            strokePaint.strokeWidth = parsed.region.strokeWidth
            strokePaint.color = DEFAULT_OUTLINE_COLOR
            canvas.drawPath(parsed.path, strokePaint)
        }

        // 4. Draw Decorative Paths (whiskers, eyes, smile lines)
        for (dec in parsedDecoratives) {
            if (dec.decorative.fillColor != 0) {
                decorativePaint.style = Paint.Style.FILL
                decorativePaint.color = dec.decorative.fillColor
                canvas.drawPath(dec.path, decorativePaint)
            }
            if (dec.decorative.strokeColor != 0) {
                decorativePaint.style = Paint.Style.STROKE
                decorativePaint.color = dec.decorative.strokeColor
                decorativePaint.strokeWidth = dec.decorative.strokeWidth
                canvas.drawPath(dec.path, decorativePaint)
            }
        }

        canvas.restore()
    }

    override fun onDetachedFromWindow() {
        stopHintAnimation()
        super.onDetachedFromWindow()
    }

    companion object {
        private const val TOUCH_SLOP = 24f
        private const val DEFAULT_OUTLINE_COLOR = 0xFF1E3A5F.toInt() // Cozy navy outline
        private const val DEFAULT_HINT_COLOR = 0x80FFE68C.toInt()    // Semi-transparent yellow glow
    }
}
