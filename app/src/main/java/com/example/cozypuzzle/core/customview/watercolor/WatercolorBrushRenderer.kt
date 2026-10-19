package com.example.cozypuzzle.core.customview.watercolor

import android.graphics.Canvas
import android.graphics.Paint
import androidx.core.graphics.ColorUtils
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Lightweight watercolor brush used by the Daily Draw board.
 *
 * A stroke segment is rendered as a series of overlapping translucent "dabs":
 * a denser core pass plus sparse, larger, very faint bleed dabs. Layered low
 * alpha circles read as soft, organic watercolor pigment without any bitmap
 * filtering cost.
 *
 * The class is stateless; callers supply their own seeded [Random] so strokes
 * can be rebuilt deterministically (e.g. after a size change).
 */
class WatercolorBrushRenderer {

    private val dabPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val petalPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val centerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }

    /**
     * Paints the watercolor capsule for one logical stroke segment.
     *
     * @param radiusPx half of the desired visual stroke width in pixels
     * (one grid cell).
     */
    fun drawSegment(
        canvas: Canvas,
        x1: Float,
        y1: Float,
        x2: Float,
        y2: Float,
        radiusPx: Float,
        color: Int,
        random: Random
    ) {
        val dx = x2 - x1
        val dy = y2 - y1
        val length = kotlin.math.sqrt(dx * dx + dy * dy)
        val step = maxOf(radiusPx * DAB_SPACING_FRACTION, MIN_STEP_PX)
        val count = (length / step).toInt().coerceAtLeast(1)

        for (index in 0..count) {
            val t = index.toFloat() / count
            val cx = x1 + dx * t
            val cy = y1 + dy * t

            // Core pigment: soft irregular body.
            val coreRadius = radiusPx * random.nextFloat(CORE_MIN_SCALE, CORE_MAX_SCALE)
            dabPaint.color = jitter(color, random)
            dabPaint.alpha = (random.nextFloat(CORE_ALPHA_MIN, CORE_ALPHA_MAX) * 255f).toInt()
            canvas.drawCircle(cx, cy, coreRadius, dabPaint)

            // Occasional bleeding halo slightly outside the stroke.
            if (random.nextFloat() < BLEED_PROBABILITY) {
                val bleedRadius = radiusPx * random.nextFloat(BLEED_MIN_SCALE, BLEED_MAX_SCALE)
                val angle = random.nextFloat(0f, 2f * PI.toFloat())
                val distance = radiusPx * random.nextFloat(0f, BLEED_SPREAD)
                dabPaint.color = jitter(color, random)
                dabPaint.alpha = (random.nextFloat(BLEED_ALPHA_MIN, BLEED_ALPHA_MAX) * 255f).toInt()
                canvas.drawCircle(
                    cx + cos(angle) * distance,
                    cy + sin(angle) * distance,
                    bleedRadius,
                    dabPaint
                )
            }

            // Sparse lighter blooms give the wash its uneven water look.
            if (random.nextFloat() < BLOOM_PROBABILITY) {
                dabPaint.color = ColorUtils.blendARGB(color, WHITE, BLOOM_LIGHTEN)
                dabPaint.alpha = (random.nextFloat(BLOOM_ALPHA_MIN, BLOOM_ALPHA_MAX) * 255f).toInt()
                canvas.drawCircle(cx, cy, radiusPx * BLOOM_SCALE, dabPaint)
            }
        }
    }

    /**
     * Draws one small decorative flower. Petal count, rotation and size vary
     * per call so flowers never look like identical copies.
     */
    fun drawFlower(
        canvas: Canvas,
        cx: Float,
        cy: Float,
        sizePx: Float,
        color: Int,
        accent: Int,
        random: Random
    ) {
        val petals = random.nextInt(PETAL_COUNT_MIN, PETAL_COUNT_MAX + 1)
        val rotation = random.nextFloat(0f, 2f * PI.toFloat())
        val alpha = (random.nextFloat(FLOWER_ALPHA_MIN, FLOWER_ALPHA_MAX) * 255f).toInt()
        val petalRadius = sizePx * random.nextFloat(PETAL_SIZE_MIN, PETAL_SIZE_MAX)
        val orbit = petalRadius * PETAL_ORBIT

        petalPaint.color = ColorUtils.blendARGB(
            color,
            WHITE,
            random.nextFloat(PETAL_LIGHTEN_MIN, PETAL_LIGHTEN_MAX)
        )
        petalPaint.alpha = alpha
        centerPaint.color = accent
        centerPaint.alpha = (alpha * CENTER_ALPHA_FACTOR).toInt().coerceAtMost(MAX_ALPHA)

        var angle = rotation
        repeat(petals) {
            val px = cx + cos(angle) * orbit
            val py = cy + sin(angle) * orbit
            canvas.drawCircle(px, py, petalRadius, petalPaint)
            angle += 2f * PI.toFloat() / petals
        }
        canvas.drawCircle(cx, cy, petalRadius * CENTER_RADIUS, centerPaint)
    }

    /** Nudges the base color toward white or its accent for subtle variation. */
    private fun jitter(color: Int, random: Random): Int = when (random.nextInt(3)) {
        0 -> ColorUtils.blendARGB(color, WHITE, random.nextFloat(0f, JITTER_LIGHTEN))
        1 -> ColorUtils.blendARGB(color, BLACK, random.nextFloat(0f, JITTER_DARKEN))
        else -> color
    }

    private fun Random.nextFloat(min: Float, max: Float): Float =
        min + nextFloat() * (max - min)

    private companion object {
        const val MIN_STEP_PX = 2f
        const val WHITE = 0xFFFFFFFF.toInt()
        const val BLACK = 0xFF000000.toInt()
        const val MAX_ALPHA = 255

        const val DAB_SPACING_FRACTION = 0.3f
        const val CORE_MIN_SCALE = 0.72f
        const val CORE_MAX_SCALE = 1f
        const val CORE_ALPHA_MIN = 0.10f
        const val CORE_ALPHA_MAX = 0.16f

        const val BLEED_PROBABILITY = 0.35f
        const val BLEED_MIN_SCALE = 1.05f
        const val BLEED_MAX_SCALE = 1.45f
        const val BLEED_SPREAD = 0.45f
        const val BLEED_ALPHA_MIN = 0.03f
        const val BLEED_ALPHA_MAX = 0.06f

        const val BLOOM_PROBABILITY = 0.18f
        const val BLOOM_LIGHTEN = 0.55f
        const val BLOOM_ALPHA_MIN = 0.04f
        const val BLOOM_ALPHA_MAX = 0.08f
        const val BLOOM_SCALE = 0.85f

        const val JITTER_LIGHTEN = 0.12f
        const val JITTER_DARKEN = 0.10f

        const val PETAL_COUNT_MIN = 4
        const val PETAL_COUNT_MAX = 6
        const val PETAL_SIZE_MIN = 0.5f
        const val PETAL_SIZE_MAX = 0.8f
        const val PETAL_ORBIT = 0.9f
        const val PETAL_LIGHTEN_MIN = 0.05f
        const val PETAL_LIGHTEN_MAX = 0.35f
        const val FLOWER_ALPHA_MIN = 0.55f
        const val FLOWER_ALPHA_MAX = 0.9f
        const val CENTER_ALPHA_FACTOR = 0.8f
        const val CENTER_RADIUS = 0.45f
    }
}
