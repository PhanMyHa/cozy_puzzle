package com.example.cozypuzzle.domain.usecase.puzzle

import com.example.cozypuzzle.core.puzzle.history.PuzzleHistory
import com.example.cozypuzzle.core.puzzle.model.Puzzle
import javax.inject.Inject

class UndoPuzzleUseCase @Inject constructor() {

    operator fun invoke(history: PuzzleHistory): Puzzle? = history.undo()
}
