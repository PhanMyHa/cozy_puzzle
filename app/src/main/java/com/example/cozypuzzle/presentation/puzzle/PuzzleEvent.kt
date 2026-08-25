package com.example.cozypuzzle.presentation.puzzle

sealed interface PuzzleEvent {

    data class PieceClicked(val pieceId: Int) : PuzzleEvent

    data class RotatePiece(val pieceId: Int) : PuzzleEvent

    data class SwapPieces(val firstId: Int, val secondId: Int) : PuzzleEvent

    data class MovePiece(val pieceId: Int, val position: Int) : PuzzleEvent

    data object Undo : PuzzleEvent

    data object Redo : PuzzleEvent

    data object Hint : PuzzleEvent

    data object Pause : PuzzleEvent

    data object Resume : PuzzleEvent

    data object Restart : PuzzleEvent

    data object Back : PuzzleEvent
}

sealed interface PuzzleEffect {

    data object NavigateBackToSelection : PuzzleEffect

    data object NavigateHome : PuzzleEffect
}
