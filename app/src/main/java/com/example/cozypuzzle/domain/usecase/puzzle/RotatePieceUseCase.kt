package com.example.cozypuzzle.domain.usecase.puzzle

import com.example.cozypuzzle.core.puzzle.engine.GridPuzzleEngine
import com.example.cozypuzzle.core.puzzle.model.Puzzle
import javax.inject.Inject

class RotatePieceUseCase @Inject constructor(
    private val engine: GridPuzzleEngine
) {

    operator fun invoke(puzzle: Puzzle, pieceId: Int): Puzzle =
        engine.rotate(puzzle, pieceId)
}
