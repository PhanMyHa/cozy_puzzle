package com.example.cozypuzzle.domain.usecase.puzzle

import com.example.cozypuzzle.core.puzzle.model.Difficulty
import com.example.cozypuzzle.core.puzzle.model.PuzzleMode
import com.example.cozypuzzle.domain.model.PuzzleResult
import com.example.cozypuzzle.domain.repository.PuzzleRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GetBestPuzzleResultUseCaseTest {

    @Test
    fun `best result is the shortest completion time`() = runTest {
        val results = listOf(
            result(durationMs = 90_000L, completedAt = 1L),
            result(durationMs = 30_000L, completedAt = 2L),
            result(durationMs = 60_000L, completedAt = 3L)
        )
        val useCase = GetBestPuzzleResultUseCase(FakePuzzleRepository(results))

        assertEquals(30_000L, useCase("cute_cat")?.durationMs)
    }

    @Test
    fun `ties are broken by the earliest completion`() = runTest {
        val results = listOf(
            result(durationMs = 45_000L, completedAt = 10L),
            result(durationMs = 45_000L, completedAt = 5L)
        )
        val useCase = GetBestPuzzleResultUseCase(FakePuzzleRepository(results))

        assertEquals(5L, useCase("cute_cat")?.completedAtEpochMs)
    }

    @Test
    fun `only results of the same puzzle are considered`() = runTest {
        val results = listOf(
            result(puzzleId = "other", durationMs = 5_000L, completedAt = 1L),
            result(puzzleId = "cute_cat", durationMs = 50_000L, completedAt = 2L)
        )
        val useCase = GetBestPuzzleResultUseCase(FakePuzzleRepository(results))

        assertEquals(50_000L, useCase("cute_cat")?.durationMs)
    }

    @Test
    fun `grid size filter narrows candidates`() = runTest {
        val results = listOf(
            result(gridSize = 4, durationMs = 20_000L, completedAt = 1L),
            result(gridSize = 5, durationMs = 40_000L, completedAt = 2L)
        )
        val useCase = GetBestPuzzleResultUseCase(FakePuzzleRepository(results))

        assertEquals(40_000L, useCase("cute_cat", gridSize = 5)?.durationMs)
    }

    @Test
    fun `no results means no best result`() = runTest {
        val useCase = GetBestPuzzleResultUseCase(FakePuzzleRepository(emptyList()))

        assertNull(useCase("cute_cat"))
    }

    private fun result(
        puzzleId: String = "cute_cat",
        gridSize: Int = 4,
        durationMs: Long,
        completedAt: Long
    ): PuzzleResult = PuzzleResult(
        puzzleId = puzzleId,
        mode = PuzzleMode.BASIC,
        difficulty = Difficulty.EASY,
        rows = gridSize,
        columns = gridSize,
        durationMs = durationMs,
        moveCount = 10,
        hintsUsed = 0,
        completedAtEpochMs = completedAt
    )

    private class FakePuzzleRepository(
        private val results: List<PuzzleResult>
    ) : PuzzleRepository {

        override suspend fun saveResult(result: PuzzleResult): Long = 0L

        override suspend fun getResultsForPuzzle(puzzleId: String): List<PuzzleResult> =
            results.filter { it.puzzleId == puzzleId }
    }
}
