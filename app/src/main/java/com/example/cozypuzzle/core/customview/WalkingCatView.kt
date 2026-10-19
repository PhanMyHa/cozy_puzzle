package com.example.cozypuzzle.core.customview

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.Choreographer
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import androidx.core.content.ContextCompat
import com.example.cozypuzzle.R
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Shared interactive 2D cat mascot that walks horizontally along the bottom edge of the usable screen.
 * Supports tap reaction, horizontal dragging, and upward drag with realistic gravity fall and landing bounce.
 */
class WalkingCatView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val density = resources.displayMetrics.density
    private val catSize = (48f * density).toInt()
    private val touchPadding = 20f * density
    private val touchSlop = ViewConfiguration.get(context).scaledTouchSlop.toFloat()

    // Cat sprite drawable
    private val catDrawable: Drawable? = ContextCompat.getDrawable(context, R.drawable.ic_walking_cat_mascot)

    // Floor shadow paint
    private val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#2530445C")
        style = Paint.Style.FILL
    }
    private val shadowRect = RectF()

    // Coordinates and boundaries
    var catX: Float = 0f
        private set
    var catY: Float = 0f
        private set

    private var minX = 8f * density
    private var maxX = 0f
    private var minY = 16f * density
    private var floorY = 0f

    // Walking movement state
    private var walkDirection: Float = 1f // 1f = right, -1f = left
    private val walkSpeed = 38f * density // 38dp per second
    private var walkPhase: Float = 0f
    private var bobY: Float = 0f
    private var tiltAngle: Float = 0f

    // Squash & stretch on landing
    private var squashX: Float = 1f
    private var squashY: Float = 1f

    // Touch and drag tracking
    private var isDragging = false
    private var isFalling = false
    private var isHopping = false
    var isWalkingPaused: Boolean = false
        private set

    private var touchDownX = 0f
    private var touchDownY = 0f
    private var dragOffsetX = 0f
    private var dragOffsetY = 0f
    private var hasExceededSlop = false

    // Animators
    private var fallAnimator: ValueAnimator? = null
    private var hopAnimator: ValueAnimator? = null
    private var squashAnimator: ValueAnimator? = null

    // Frame callback for continuous 60fps walking animation
    private var lastFrameTimeNanos: Long = 0L
    private var isAttached = false

    private val frameCallback = object : Choreographer.FrameCallback {
        override fun doFrame(frameTimeNanos: Long) {
            if (!isAttached || visibility != VISIBLE) return

            if (lastFrameTimeNanos != 0L && !isWalkingPaused && !isDragging && !isFalling && !isHopping) {
                val dt = ((frameTimeNanos - lastFrameTimeNanos) / 1_000_000_000f).coerceIn(0.001f, 0.05f)
                updateWalking(dt)
            }
            lastFrameTimeNanos = frameTimeNanos

            if (!isWalkingPaused && visibility == VISIBLE) {
                invalidate()
                Choreographer.getInstance().postFrameCallback(this)
            }
        }
    }

    init {
        contentDescription = "Walking cat mascot"
        isClickable = true
        isFocusable = true
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (w <= 0 || h <= 0) return

        minX = 8f * density
        maxX = (w - minX - catSize).coerceAtLeast(minX)
        minY = 16f * density
        floorY = (h - catSize - 6f * density).coerceAtLeast(minY)

        // Initial placement or clamp to new bounds
        if (oldw == 0 || oldh == 0) {
            catX = ((w - catSize) / 2f).coerceIn(minX, maxX)
            catY = floorY
        } else {
            catX = catX.coerceIn(minX, maxX)
            if (!isDragging && !isFalling && !isHopping) {
                catY = floorY
            } else {
                catY = catY.coerceIn(minY, floorY)
            }
        }
        invalidate()
    }

    private fun updateWalking(dt: Float) {
        if (maxX <= minX) return

        catX += walkSpeed * walkDirection * dt

        if (catX >= maxX) {
            catX = maxX
            walkDirection = -1f
        } else if (catX <= minX) {
            catX = minX
            walkDirection = 1f
        }

        walkPhase += dt * 6.5f
        bobY = sin(walkPhase) * (2.2f * density)
        tiltAngle = sin(walkPhase) * 3f
        catY = floorY
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isEnabled || visibility != VISIBLE) return false

        val x = event.x
        val y = event.y

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                val catTouchRect = RectF(
                    catX - touchPadding,
                    catY - touchPadding,
                    catX + catSize + touchPadding,
                    catY + catSize + touchPadding
                )

                if (!catTouchRect.contains(x, y)) {
                    // Touch is outside cat bounds -> let underlying Fragment/RecyclerView handle it!
                    return false
                }

                cancelAllAnimators()
                isDragging = true
                isFalling = false
                isHopping = false
                hasExceededSlop = false

                touchDownX = x
                touchDownY = y
                dragOffsetX = x - catX
                dragOffsetY = y - catY

                parent?.requestDisallowInterceptTouchEvent(true)
                invalidate()
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                if (!isDragging) return false

                val dx = x - touchDownX
                val dy = y - touchDownY
                if (!hasExceededSlop && hypot(dx, dy) > touchSlop) {
                    hasExceededSlop = true
                }

                if (hasExceededSlop) {
                    catX = (x - dragOffsetX).coerceIn(minX, maxX)
                    catY = (y - dragOffsetY).coerceIn(minY, floorY)
                    invalidate()
                }
                return true
            }

            MotionEvent.ACTION_UP -> {
                if (!isDragging) return false
                isDragging = false
                parent?.requestDisallowInterceptTouchEvent(false)

                if (!hasExceededSlop) {
                    // Tap gesture! Trigger playful hop
                    performClick()
                    triggerTapHop()
                } else {
                    // Drag release
                    if (catY < floorY - 3f * density) {
                        startGravityFall()
                    } else {
                        catY = floorY
                        resumeWalkingLoopIfNeeded()
                    }
                }
                return true
            }

            MotionEvent.ACTION_CANCEL -> {
                if (isDragging) {
                    isDragging = false
                    parent?.requestDisallowInterceptTouchEvent(false)
                    if (catY < floorY - 3f * density) {
                        startGravityFall()
                    } else {
                        catY = floorY
                        resumeWalkingLoopIfNeeded()
                    }
                }
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    /**
     * Tapping makes the cat do a joyful little jump towards a new spot.
     */
    private fun triggerTapHop() {
        if (isHopping || isFalling || isDragging) return
        cancelAllAnimators()

        isHopping = true
        val jumpDist = (70f * density)
        // Pick new position away from current spot
        val targetX = if (catX + jumpDist <= maxX && (catX - jumpDist < minX || walkDirection > 0)) {
            (catX + jumpDist).coerceIn(minX, maxX)
        } else {
            (catX - jumpDist).coerceIn(minX, maxX)
        }

        walkDirection = if (targetX >= catX) 1f else -1f
        val startX = catX
        val hopHeight = 36f * density

        hopAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 440L
            interpolator = DecelerateInterpolator()
            addUpdateListener { va ->
                val fraction = va.animatedFraction
                catX = (startX + (targetX - startX) * fraction).coerceIn(minX, maxX)
                val arc = sin(fraction * Math.PI.toFloat())
                catY = floorY - arc * hopHeight
                invalidate()
            }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    isHopping = false
                    catY = floorY
                    triggerSquashAndStretch()
                    resumeWalkingLoopIfNeeded()
                }
            })
            start()
        }
    }

    /**
     * Animate the cat falling back down to the walking baseline when released in midair.
     */
    private fun startGravityFall() {
        cancelAllAnimators()
        isFalling = true

        val startY = catY
        val fallDistance = (floorY - startY).coerceAtLeast(1f)
        val maxFall = (floorY - minY).coerceAtLeast(1f)
        val durationMs = (sqrt(fallDistance / maxFall) * 420L).toLong().coerceIn(180L, 460L)

        fallAnimator = ValueAnimator.ofFloat(startY, floorY).apply {
            duration = durationMs
            interpolator = AccelerateInterpolator(1.8f)
            addUpdateListener { va ->
                catY = (va.animatedValue as Float).coerceIn(minY, floorY)
                invalidate()
            }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    isFalling = false
                    catY = floorY
                    triggerSquashAndStretch()
                    resumeWalkingLoopIfNeeded()
                }
            })
            start()
        }
    }

    /**
     * Gentle squash & stretch on landing for a cute cartoon feel.
     */
    private fun triggerSquashAndStretch() {
        squashAnimator?.cancel()
        squashAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 240L
            addUpdateListener { va ->
                val f = va.animatedFraction
                if (f < 0.35f) {
                    val p = f / 0.35f
                    squashX = 1f + 0.22f * p
                    squashY = 1f - 0.22f * p
                } else {
                    val p = (f - 0.35f) / 0.65f
                    val overshoot = sin(p * Math.PI.toFloat()) * 0.12f
                    squashX = 1f - overshoot
                    squashY = 1f + overshoot
                }
                invalidate()
            }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    squashX = 1f
                    squashY = 1f
                    invalidate()
                }
            })
            start()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (width <= 0 || height <= 0) return

        // 1. Draw floor shadow beneath the cat
        val currentBob = if (!isDragging && !isFalling && !isHopping) bobY else 0f
        val elevation = (floorY - catY).coerceAtLeast(0f)
        val maxElevation = (floorY - minY).coerceAtLeast(1f)
        val elevationRatio = (elevation / maxElevation).coerceIn(0f, 1f)

        // Shadow stays on the floor, shrinking and fading with height
        val shadowW = (catSize * 0.75f) * (1f - elevationRatio * 0.45f)
        val shadowH = (7f * density) * (1f - elevationRatio * 0.5f)
        val shadowCenterX = catX + catSize / 2f
        val shadowCenterY = floorY + catSize - (2f * density)

        shadowRect.set(
            shadowCenterX - shadowW / 2f,
            shadowCenterY - shadowH / 2f,
            shadowCenterX + shadowW / 2f,
            shadowCenterY + shadowH / 2f
        )
        shadowPaint.alpha = ((1f - elevationRatio * 0.6f) * 36).toInt().coerceIn(6, 40)
        canvas.drawOval(shadowRect, shadowPaint)

        // 2. Draw Cat Sprite with direction flip and bounce
        val catDraw = catDrawable ?: return
        canvas.save()

        val drawCenterX = catX + catSize / 2f
        val drawCenterY = catY + currentBob + catSize / 2f

        canvas.translate(drawCenterX, drawCenterY)
        canvas.scale(walkDirection * squashX, squashY)

        val currentTilt = if (!isDragging && !isFalling && !isHopping) tiltAngle else 0f
        canvas.rotate(currentTilt)

        catDraw.setBounds(
            -catSize / 2,
            -catSize / 2,
            catSize / 2,
            catSize / 2
        )
        catDraw.draw(canvas)

        canvas.restore()
    }

    fun pauseWalking() {
        if (isWalkingPaused) return
        isWalkingPaused = true
        cancelAllAnimators()
        Choreographer.getInstance().removeFrameCallback(frameCallback)
    }

    fun resumeWalking() {
        if (!isWalkingPaused) return
        isWalkingPaused = false
        lastFrameTimeNanos = 0L
        if (isAttached && visibility == VISIBLE) {
            Choreographer.getInstance().removeFrameCallback(frameCallback)
            Choreographer.getInstance().postFrameCallback(frameCallback)
        }
    }

    private fun resumeWalkingLoopIfNeeded() {
        if (!isWalkingPaused && isAttached && visibility == VISIBLE) {
            Choreographer.getInstance().removeFrameCallback(frameCallback)
            Choreographer.getInstance().postFrameCallback(frameCallback)
        }
    }

    private fun cancelAllAnimators() {
        fallAnimator?.cancel()
        fallAnimator = null
        hopAnimator?.cancel()
        hopAnimator = null
        squashAnimator?.cancel()
        squashAnimator = null
        squashX = 1f
        squashY = 1f
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        isAttached = true
        lastFrameTimeNanos = 0L
        if (!isWalkingPaused && visibility == VISIBLE) {
            Choreographer.getInstance().postFrameCallback(frameCallback)
        }
    }

    override fun onDetachedFromWindow() {
        isAttached = false
        Choreographer.getInstance().removeFrameCallback(frameCallback)
        cancelAllAnimators()
        super.onDetachedFromWindow()
    }
}
