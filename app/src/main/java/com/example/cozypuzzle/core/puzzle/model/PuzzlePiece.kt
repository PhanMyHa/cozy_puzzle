package com.example.cozypuzzle.core.puzzle.model

data class PuzzlePiece(
    val id: Int,
    val targetPosition: Int,
    val currentPosition: Int,
    val rotation: Int
)
