package com.example.cozypuzzle.domain.usecase.puzzle

import com.example.cozypuzzle.core.puzzle.model.Difficulty
import com.example.cozypuzzle.core.puzzle.model.PuzzleMode
import org.junit.Assert.assertEquals
import org.junit.Test

class CreatePuzzleResultUseCaseTest {

    private val fixedNowMs = 1_700_000_000_000L
    private val useCase = CreatePuzzleResultUseCase(clock = { fixedNowMs })

    @Test
    fun `result contains all completion stats`() {
        val result = useCase(
            CreatePuzzleResultUseCase.Stats(
                puzzleId = "cute_cat",
                mode = PuzzleMode.BASIC,
                difficulty = Difficulty.EASY,
                gridSize = 4,
                durationMs = 65_000L,
                moveCount = 12,
                hintsUsed = 1
            )
        )

        assertEquals("cute_cat", result.puzzleId)
        assertEquals(PuzzleMode.BASIC, result.mode)
        assertEquals(Difficulty.EASY, result.difficulty)
        assertEquals(4, result.rows)
        assertEquals(4, result.columns)
        assertEquals(65_000L, result.durationMs)
        assertEquals(12, result.moveCount)
        assertEquals(1, result.hintsUsed)
        assertEquals(fixedNowMs, result.completedAtEpochMs)
    }

    @Test
    fun `zero hints used is a perfect game`() {
        val result = useCase(
            CreatePuzzleResultUseCase.Stats(
                puzzleId = "picnic",
                mode = PuzzleMode.BASIC,
                difficulty = Difficulty.EXPERT,
                gridSize = 5,
                durationMs = 120_000L,
                moveCount = 30,
                hintsUsed = 0
            )
        )

        assertEquals(true, result.isPerfect)
    }

    @Test
    fun `hints used breaks perfection`() {
        val result = useCase(
            CreatePuzzleResultUseCase.Stats(
                puzzleId = "picnic",
                mode = PuzzleMode.BASIC,
                difficulty = Difficulty.EXPERT,
                gridSize = 5,
                durationMs = 120_000L,
                moveCount = 30,
                hintsUsed = 2
            )
        )

        assertEquals(false, result.isPerfect)
        assertEquals(2, result.hintsUsed)
    }
}
