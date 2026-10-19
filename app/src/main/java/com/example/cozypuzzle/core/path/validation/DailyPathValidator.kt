package com.example.cozypuzzle.core.path.validation

import com.example.cozypuzzle.core.path.model.StrokePoint
import com.example.cozypuzzle.domain.model.DailyPuzzle
import kotlin.math.abs

/**
 * Outcome of feeding new stroke samples to [DailyPathValidator].
 */
sealed interface DailyStrokeOutcome {

    /** All samples accepted; the stroke may continue (or just finished). */
    data class Accepted(val reachedGoal: Boolean) : DailyStrokeOutcome

    /** The stroke broke a rule; the current attempt must fail. */
    data class Failed(val reason: FailureReason) : DailyStrokeOutcome
}

enum class FailureReason {
    /** Touch-down did not happen close enough to the start point. */
    NOT_AT_START,

    /** Two consecutive samples are too far apart, i.e. a teleport. */
    DISCONTINUOUS,

    /** The thick stroke touched an obstacle cell. */
    OBSTACLE_HIT
}

/**
 * Pure path validation for the Daily "draw the path" mode.
 *
 * Geometry is expressed in board units where one grid cell is 1×1 and the
 * board spans [0, size]². The drawn stroke is treated as a capsule: a center
 * line with a thickness of exactly one cell ([Config.strokeWidth] = 1).
 * Collision tests inflate every obstacle square by half the stroke width and
 * check whether any stroke segment intersects the inflated rect — the exact
 * Minkowski-sum test for capsule-vs-square, minus conservative corner
 * rounding at the four corners of the inflation.
 *
 * This class contains no Android code and no rendering concerns.
 */
class DailyPathValidator(
    private val config: Config = Config()
) {

    data class Config(
        /** Full visual/logical stroke width in board units (one cell). */
        val strokeWidth: Float = 1f,

        /** Forgiveness margin so near-misses do not feel unfair. */
        val tolerance: Float = 0.08f,

        /** Max distance between two consecutive samples before it counts as a teleport. */
        val maxStepLength: Float = 0.75f,

        /** Touch-down must be within this distance of the start center. */
        val startCaptureRadius: Float = 0.9f,

        /** Stroke end must come within this distance of the goal center. */
        val goalCaptureRadius: Float = 0.55f
    ) {
        val halfStrokeWidth: Float get() = strokeWidth / 2f
    }

    /**
     * Validates the very first sample of an attempt.
     */
    fun validateStart(puzzle: DailyPuzzle, point: StrokePoint): DailyStrokeOutcome =
        if (point.distanceTo(puzzle.centerOf(puzzle.startCell)) <= config.startCaptureRadius) {
            DailyStrokeOutcome.Accepted(reachedGoal = false)
        } else {
            DailyStrokeOutcome.Failed(FailureReason.NOT_AT_START)
        }

    /**
     * Validates a batch of newly recorded samples against the existing path.
     * Samples are checked in order; the first violation fails the attempt.
     */
    fun validateExtension(
        puzzle: DailyPuzzle,
        currentPath: List<StrokePoint>,
        newPoints: List<StrokePoint>
    ): DailyStrokeOutcome {
        if (newPoints.isEmpty()) return DailyStrokeOutcome.Accepted(reachedGoal = false)

        if (currentPath.isNotEmpty() &&
            currentPath.last().distanceTo(newPoints.first()) > config.maxStepLength
        ) {
            return DailyStrokeOutcome.Failed(FailureReason.DISCONTINUOUS)
        }

        var previous = currentPath.lastOrNull() ?: newPoints.first()
        for (point in newPoints) {
            if (previous.distanceTo(point) > config.maxStepLength) {
                return DailyStrokeOutcome.Failed(FailureReason.DISCONTINUOUS)
            }
            if (segmentHitsObstacle(puzzle, previous, point)) {
                return DailyStrokeOutcome.Failed(FailureReason.OBSTACLE_HIT)
            }
            previous = point
        }

        // Goal counts as reached only when the final sample sits inside the
        // goal capture radius; obstacles were already cleared above.
        val reachedGoal = newPoints.last()
            .distanceTo(puzzle.centerOf(puzzle.goalCell)) <= config.goalCaptureRadius
        return DailyStrokeOutcome.Accepted(reachedGoal = reachedGoal)
    }

    /**
     * True when the capsule swept along segment [from]→[to] overlaps any
     * obstacle square.
     */
    fun segmentHitsObstacle(puzzle: DailyPuzzle, from: StrokePoint, to: StrokePoint): Boolean {
        val reach = config.halfStrokeWidth - config.tolerance
        puzzle.obstacleCells.forEach { cell ->
            val row = cell / puzzle.boardSize
            val column = cell % puzzle.boardSize
            val rect = BoardRect(
                left = column - reach,
                top = row - reach,
                right = column + 1f + reach,
                bottom = row + 1f + reach
            )
            if (segmentIntersectsRect(from, to, rect)) return true
        }
        return false
    }

    private fun segmentIntersectsRect(from: StrokePoint, to: StrokePoint, rect: BoardRect): Boolean {
        if (rect.contains(from.x, from.y) || rect.contains(to.x, to.y)) return true

        val dx = to.x - from.x
        val dy = to.y - from.y
        var tMin = 0f
        var tMax = 1f

        // X slab
        if (abs(dx) < EPSILON) {
            if (from.x < rect.left || from.x > rect.right) return false
        } else {
            var t1 = (rect.left - from.x) / dx
            var t2 = (rect.right - from.x) / dx
            if (t1 > t2) {
                val tmp = t1
                t1 = t2
                t2 = tmp
            }
            tMin = maxOf(tMin, t1)
            tMax = minOf(tMax, t2)
            if (tMin > tMax) return false
        }

        // Y slab
        if (abs(dy) < EPSILON) {
            if (from.y < rect.top || from.y > rect.bottom) return false
        } else {
            var t1 = (rect.top - from.y) / dy
            var t2 = (rect.bottom - from.y) / dy
            if (t1 > t2) {
                val tmp = t1
                t1 = t2
                t2 = tmp
            }
            tMin = maxOf(tMin, t1)
            tMax = minOf(tMax, t2)
            if (tMin > tMax) return false
        }

        return true
    }

    private data class BoardRect(val left: Float, val top: Float, val right: Float, val bottom: Float) {
        fun contains(x: Float, y: Float): Boolean =
            x >= left && x <= right && y >= top && y <= bottom
    }

    private companion object {
        const val EPSILON = 1e-6f
    }
}
