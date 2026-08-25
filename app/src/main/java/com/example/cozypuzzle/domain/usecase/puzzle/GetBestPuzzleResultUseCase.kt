package com.example.cozypuzzle.domain.usecase.puzzle

import com.example.cozypuzzle.domain.model.PuzzleResult
import com.example.cozypuzzle.domain.repository.PuzzleRepository
import javax.inject.Inject

class GetBestPuzzleResultUseCase @Inject constructor(
    private val puzzleRepository: PuzzleRepository
) {

    suspend operator fun invoke(puzzleId: String, gridSize: Int? = null): PuzzleResult? =
        puzzleRepository.getResultsForPuzzle(puzzleId)
            .asSequence()
            .filter { gridSize == null || it.rows == gridSize }
            .minWithOrNull(compareBy({ it.durationMs }, { it.completedAtEpochMs }))
}
