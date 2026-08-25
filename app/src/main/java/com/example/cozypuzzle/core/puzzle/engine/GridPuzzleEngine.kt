package com.example.cozypuzzle.core.puzzle.engine

import com.example.cozypuzzle.core.puzzle.model.Puzzle
import com.example.cozypuzzle.core.puzzle.model.PuzzleAction
import com.example.cozypuzzle.core.puzzle.model.PuzzlePiece

class GridPuzzleEngine : PuzzleEngine {

    override fun rotate(puzzle: Puzzle, pieceId: Int): Puzzle =
        updatePiece(puzzle, pieceId) { piece ->
            piece.copy(rotation = (piece.rotation + ROTATION_STEP_DEGREES) % FULL_ROTATION_DEGREES)
        }

    override fun swap(puzzle: Puzzle, firstId: Int, secondId: Int): Puzzle {
        require(firstId != secondId) { "Cannot swap piece $firstId with itself" }
        val first = findPiece(puzzle, firstId)
        val second = findPiece(puzzle, secondId)
        return replacePieces(
            puzzle = puzzle,
            replacements = listOf(
                first.copy(currentPosition = second.currentPosition),
                second.copy(currentPosition = first.currentPosition)
            )
        )
    }

    override fun move(puzzle: Puzzle, pieceId: Int, position: Int): Puzzle {
        require(position in puzzle.pieces.indices) {
            "Target position $position is outside the board of size ${puzzle.pieces.size}"
        }
        val piece = findPiece(puzzle, pieceId)
        if (piece.currentPosition == position) return puzzle
        val occupant = puzzle.pieces.firstOrNull { it.currentPosition == position }
            ?: return replacePieces(
                puzzle = puzzle,
                replacements = listOf(piece.copy(currentPosition = position))
            )
        return swap(puzzle, piece.id, occupant.id)
    }

    override fun applyAction(puzzle: Puzzle, action: PuzzleAction): Puzzle = when (action) {
        is PuzzleAction.Rotate -> rotate(puzzle, action.pieceId)
        is PuzzleAction.Swap -> swap(puzzle, action.firstId, action.secondId)
        is PuzzleAction.Move -> move(puzzle, action.pieceId, action.position)
    }

    private fun updatePiece(
        puzzle: Puzzle,
        pieceId: Int,
        transform: (PuzzlePiece) -> PuzzlePiece
    ): Puzzle {
        val piece = findPiece(puzzle, pieceId)
        return replacePieces(puzzle, listOf(transform(piece)))
    }

    private fun findPiece(puzzle: Puzzle, pieceId: Int): PuzzlePiece =
        puzzle.pieces.firstOrNull { it.id == pieceId }
            ?: throw IllegalArgumentException("Unknown piece id $pieceId in puzzle ${puzzle.id}")

    private fun replacePieces(
        puzzle: Puzzle,
        replacements: List<PuzzlePiece>
    ): Puzzle {
        val replacementById = replacements.associateBy(PuzzlePiece::id)
        val updatedPieces = puzzle.pieces.map { piece ->
            replacementById[piece.id] ?: piece
        }
        return puzzle.copy(pieces = updatedPieces)
    }

    private companion object {
        const val ROTATION_STEP_DEGREES = 90
        const val FULL_ROTATION_DEGREES = 360
    }
}
