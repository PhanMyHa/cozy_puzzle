package com.example.cozypuzzle.core.puzzle.model

data class Puzzle(
    val id: String,
    val config: PuzzleConfig,
    val pieces: List<PuzzlePiece>
)
