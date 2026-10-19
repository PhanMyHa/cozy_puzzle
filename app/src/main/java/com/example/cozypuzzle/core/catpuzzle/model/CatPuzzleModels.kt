package com.example.cozypuzzle.core.catpuzzle.model

import androidx.annotation.DrawableRes

enum class CatPuzzleDifficulty {
    BASIC,
    INTERMEDIATE,
    HARD
}

data class CatPuzzleLevel(
    val id: String,
    val number: Int,
    val title: String,
    val difficulty: CatPuzzleDifficulty,
    @DrawableRes val imageRes: Int,
    val gridSize: Int = 3
)

data class CatPiece(
    val id: Int,
    val correctSlot: Int,
    val isPlaced: Boolean = false,
    val currentSlot: Int? = null
)

sealed class PlacementResult {
    data class Correct(
        val pieceId: Int,
        val slotIndex: Int,
        val isCompleted: Boolean,
        val fromSlot: Int? = null
    ) : PlacementResult()

    data class Incorrect(
        val pieceId: Int,
        val slotIndex: Int,
        val fromSlot: Int? = null
    ) : PlacementResult()

    data class ReturnedToTray(
        val pieceId: Int,
        val fromSlot: Int
    ) : PlacementResult()
}

data class CatPuzzleState(
    val level: CatPuzzleLevel,
    val slots: List<CatPiece?>,
    val trayPieces: List<CatPiece>,
    val selectedPieceId: Int? = null,
    val hintsRemaining: Int = DEFAULT_HINTS,
    val hintsUsed: Int = 0,
    val movesCount: Int = 0,
    val isCompleted: Boolean = false,
    val isPaused: Boolean = false,
    val elapsedTimeMs: Long = 0L,
    val starsEarned: Int = 0
) {
    val totalSlots: Int get() = level.gridSize * level.gridSize
    val placedCount: Int get() = slots.count { it != null }
    val correctlyPlacedCount: Int get() = slots.filterIndexed { index, piece -> piece != null && piece.correctSlot == index }.size

    companion object {
        const val DEFAULT_HINTS = 3
    }
}
