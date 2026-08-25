package com.example.cozypuzzle.core.puzzle.engine

import com.example.cozypuzzle.core.puzzle.model.Puzzle
import com.example.cozypuzzle.core.puzzle.model.PuzzlePiece

class GridPuzzleValidator : PuzzleValidator {

    override fun isSolved(puzzle: Puzzle): Boolean =
        puzzle.pieces.all(::isPieceSolved)

    override fun isPieceSolved(piece: PuzzlePiece): Boolean =
        piece.currentPosition == piece.targetPosition &&
            piece.rotation % FULL_ROTATION_DEGREES == 0

    override fun isBoardValid(puzzle: Puzzle): Boolean {
        val pieces = puzzle.pieces
        if (puzzle.config.rows <= 0 || puzzle.config.columns <= 0) return false
        if (pieces.size != puzzle.config.pieceCount) return false

        val ids = pieces.map { it.id }
        if (ids.toSet().size != pieces.size) return false

        val targetPositions = pieces.map { it.targetPosition }.toSet()
        if (targetPositions != pieces.indices.toSet()) return false

        val currentPositions = pieces.map { it.currentPosition }
        if (currentPositions.toSet().size != pieces.size) return false
        if (currentPositions.any { it < 0 || it >= pieces.size }) return false

        return pieces.all { it.rotation in VALID_ROTATIONS }
    }

    private companion object {
        const val FULL_ROTATION_DEGREES = 360
        val VALID_ROTATIONS = setOf(0, 90, 180, 270)
    }
}
