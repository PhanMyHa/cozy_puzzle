package com.example.cozypuzzle.core.slidethecat.model

import androidx.annotation.DrawableRes

enum class SlideCatDifficulty {
    EASY,
    MEDIUM,
    HARD
}

data class SlideCatLevel(
    val id: String,
    val number: Int,
    val title: String,
    val difficulty: SlideCatDifficulty,
    val gridSize: Int = 3,
    @DrawableRes val imageRes: Int,
    @DrawableRes val thumbnailRes: Int = imageRes,
    val shuffleMoves: Int = 15,
    val threeStarMoves: Int = 22,
    val twoStarMoves: Int = 38
)

data class SlideTile(
    val id: Int,
    val correctIndex: Int
)

data class SlideCatState(
    val level: SlideCatLevel,
    val board: List<SlideTile?>,
    val emptyIndex: Int,
    val movesCount: Int = 0,
    val hintsRemaining: Int = DEFAULT_HINTS,
    val hintsUsed: Int = 0,
    val isReferenceOverlayVisible: Boolean = false,
    val showTileNumbersHint: Boolean = false,
    val isCompleted: Boolean = false,
    val isPaused: Boolean = false,
    val elapsedTimeMs: Long = 0L,
    val starsEarned: Int = 0
) {
    val totalSlots: Int get() = level.gridSize * level.gridSize
    val isSolved: Boolean
        get() {
            if (emptyIndex != totalSlots - 1) return false
            for (i in 0 until totalSlots - 1) {
                val tile = board[i] ?: return false
                if (tile.correctIndex != i) return false
            }
            return true
        }

    companion object {
        const val DEFAULT_HINTS = 3
    }
}
