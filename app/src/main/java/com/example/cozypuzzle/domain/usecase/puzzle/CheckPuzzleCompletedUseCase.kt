package com.example.cozypuzzle.domain.usecase.puzzle

import com.example.cozypuzzle.core.puzzle.engine.GridPuzzleValidator
import com.example.cozypuzzle.core.puzzle.model.Puzzle
import javax.inject.Inject

class CheckPuzzleCompletedUseCase @Inject constructor(
    private val validator: GridPuzzleValidator
) {

    operator fun invoke(puzzle: Puzzle): Boolean = validator.isSolved(puzzle)
}
