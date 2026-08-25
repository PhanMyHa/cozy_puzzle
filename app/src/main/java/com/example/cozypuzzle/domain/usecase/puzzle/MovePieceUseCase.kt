package com.example.cozypuzzle.domain.usecase.puzzle

import com.example.cozypuzzle.core.puzzle.engine.GridPuzzleEngine
import com.example.cozypuzzle.core.puzzle.model.Puzzle
import javax.inject.Inject

class MovePieceUseCase @Inject constructor(
    private val engine: GridPuzzleEngine
) {

    operator fun invoke(puzzle: Puzzle, pieceId: Int, position: Int): Puzzle =
        engine.move(puzzle, pieceId, position)
}
