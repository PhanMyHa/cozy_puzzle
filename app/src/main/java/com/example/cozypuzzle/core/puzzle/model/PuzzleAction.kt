package com.example.cozypuzzle.core.puzzle.model

sealed interface PuzzleAction {

    data class Rotate(
        val pieceId: Int
    ) : PuzzleAction

    data class Swap(
        val firstId: Int,
        val secondId: Int
    ) : PuzzleAction

    data class Move(
        val pieceId: Int,
        val position: Int
    ) : PuzzleAction
}
