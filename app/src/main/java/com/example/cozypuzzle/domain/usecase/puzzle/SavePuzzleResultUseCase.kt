package com.example.cozypuzzle.domain.usecase.puzzle

import com.example.cozypuzzle.domain.model.PuzzleResult
import com.example.cozypuzzle.domain.repository.PuzzleRepository
import javax.inject.Inject

class SavePuzzleResultUseCase @Inject constructor(
    private val puzzleRepository: PuzzleRepository
) {

    suspend operator fun invoke(result: PuzzleResult): Long =
        puzzleRepository.saveResult(result)
}
