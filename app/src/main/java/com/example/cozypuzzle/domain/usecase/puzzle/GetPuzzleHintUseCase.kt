package com.example.cozypuzzle.domain.usecase.puzzle

import com.example.cozypuzzle.core.puzzle.engine.GridPuzzleValidator
import com.example.cozypuzzle.core.puzzle.model.Puzzle
import javax.inject.Inject

data class PuzzleHint(
    val pieceId: Int,
    val correctPosition: Int,
    val correctRotation: Int
)

class GetPuzzleHintUseCase @Inject constructor(
    private val validator: GridPuzzleValidator
) {

    operator fun invoke(puzzle: Puzzle, hintsRemaining: Int): PuzzleHint? {
        if (hintsRemaining <= 0) return null
        val misplaced = puzzle.pieces.firstOrNull { !validator.isPieceSolved(it) }
            ?: return null
        return PuzzleHint(
            pieceId = misplaced.id,
            correctPosition = misplaced.targetPosition,
            correctRotation = 0
        )
    }
}
