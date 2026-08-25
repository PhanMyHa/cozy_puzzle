package com.example.cozypuzzle.domain.usecase.puzzle

import com.example.cozypuzzle.core.puzzle.engine.GridPuzzleEngine
import com.example.cozypuzzle.core.puzzle.model.Puzzle
import javax.inject.Inject

class SwapPiecesUseCase @Inject constructor(
    private val engine: GridPuzzleEngine
) {

    operator fun invoke(puzzle: Puzzle, firstId: Int, secondId: Int): Puzzle =
        engine.swap(puzzle, firstId, secondId)
}
