package com.example.cozypuzzle.core.path.model

/**
 * A single sample of the player's stroke in logical board coordinates.
 *
 * Board units are expressed in cell sizes: the board spans [0, boardSize] on
 * both axes and one cell is exactly 1×1. Keeping this class free of Android
 * types lets the domain layer run on any platform/JVM.
 */
data class StrokePoint(val x: Float, val y: Float) {

    fun distanceTo(other: StrokePoint): Float {
        val dx = x - other.x
        val dy = y - other.y
        return kotlin.math.sqrt(dx * dx + dy * dy)
    }

    companion object {
        val ORIGIN = StrokePoint(0f, 0f)
    }
}
