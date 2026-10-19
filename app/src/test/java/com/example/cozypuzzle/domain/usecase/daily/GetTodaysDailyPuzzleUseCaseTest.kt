package com.example.cozypuzzle.domain.usecase.daily

import com.example.cozypuzzle.core.puzzle.model.Difficulty
import com.example.cozypuzzle.domain.model.DailyPuzzle
import com.example.cozypuzzle.domain.model.DailyPuzzleTheme
import com.example.cozypuzzle.domain.repository.DailyPuzzleRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class GetTodaysDailyPuzzleUseCaseTest {

    private fun puzzle(id: String) = DailyPuzzle(
        id = id,
        title = id,
        boardSize = 4,
        startCell = 0,
        goalCell = 15,
        obstacleCells = emptySet(),
        difficulty = Difficulty.EASY,
        theme = DailyPuzzleTheme.MEADOW
    )

    private class FakeRepository(private val puzzles: List<DailyPuzzle>) : DailyPuzzleRepository {
        override suspend fun getDailyPuzzles(): List<DailyPuzzle> = puzzles
        override suspend fun getDailyPuzzle(puzzleId: String): DailyPuzzle? =
            puzzles.firstOrNull { it.id == puzzleId }
    }

    @Test
    fun `selection is deterministic for a given day`() = runTest {
        val puzzles = listOf(puzzle("a"), puzzle("b"), puzzle("c"))
        val useCase = GetTodaysDailyPuzzleUseCase(
            repository = FakeRepository(puzzles),
            today = { LocalDate.of(2026, 8, 25) }
        )

        val first = useCase()
        val second = useCase()

        assertEquals(first.id, second.id)
    }

    @Test
    fun `selection cycles across days`() = runTest {
        val puzzles = listOf(puzzle("a"), puzzle("b"))
        var day = LocalDate.of(2026, 1, 1)
        val useCase = GetTodaysDailyPuzzleUseCase(
            repository = FakeRepository(puzzles),
            today = { day }
        )

        // dayOfYear 1 -> index 1 ("b"), 2 -> index 0 ("a"), 3 -> index 1 ("b").
        assertEquals("b", useCase().id)
        day = day.plusDays(1)
        assertEquals("a", useCase().id)
        day = day.plusDays(1)
        assertEquals("b", useCase().id)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `empty catalog fails loudly`() = runTest {
        GetTodaysDailyPuzzleUseCase(
            repository = FakeRepository(emptyList()),
            today = { LocalDate.of(2026, 8, 25) }
        )()
    }
}
