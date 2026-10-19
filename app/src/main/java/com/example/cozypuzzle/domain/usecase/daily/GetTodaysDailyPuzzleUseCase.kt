package com.example.cozypuzzle.domain.usecase.daily

import com.example.cozypuzzle.domain.model.DailyPuzzle
import com.example.cozypuzzle.domain.repository.DailyPuzzleRepository
import java.time.LocalDate
import javax.inject.Inject

/**
 * Picks today's daily puzzle from the local catalog. The selection is
 * deterministic per calendar day so every device sees the same puzzle without
 * any backend involvement. When Firebase arrives only this use case needs to
 * change.
 */
class GetTodaysDailyPuzzleUseCase @Inject constructor(
    private val repository: DailyPuzzleRepository,
    private val today: () -> LocalDate = { LocalDate.now() }
) {

    suspend operator fun invoke(): DailyPuzzle {
        val puzzles = repository.getDailyPuzzles()
        require(puzzles.isNotEmpty()) { "No daily puzzles available" }
        val index = Math.floorMod(today().dayOfYear, puzzles.size)
        return puzzles[index]
    }
}
