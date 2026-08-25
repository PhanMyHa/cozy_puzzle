package com.example.cozypuzzle.domain.usecase.puzzle

import com.example.cozypuzzle.core.puzzle.model.Difficulty
import com.example.cozypuzzle.core.puzzle.model.PuzzleMode
import com.example.cozypuzzle.domain.model.BasicPuzzle
import com.example.cozypuzzle.domain.repository.BasicPuzzleRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GetBasicPuzzlesUseCaseTest {

    @Test
    fun `returns all puzzles from repository`() = runTest {
        val catalog = listOf(
            BasicPuzzle("a", "A", "a", Difficulty.EASY, listOf(3, 4)),
            BasicPuzzle("b", "B", "b", Difficulty.HARD, listOf(4, 5))
        )
        val useCase = GetBasicPuzzlesUseCase(FakeBasicPuzzleRepository(catalog))

        assertEquals(catalog, useCase())
    }

    @Test
    fun `selection by id returns the requested puzzle`() = runTest {
        val catalog = listOf(
            BasicPuzzle("cat", "Cute Cat", "cat", Difficulty.EASY, listOf(3, 4)),
            BasicPuzzle("pond", "Frog Pond", "pond", Difficulty.HARD, listOf(4, 5))
        )
        val useCase = GetBasicPuzzleUseCase(FakeBasicPuzzleRepository(catalog))

        val selected = useCase("pond")

        assertEquals("Frog Pond", selected?.title)
    }

    @Test
    fun `selection by unknown id returns null`() = runTest {
        val useCase = GetBasicPuzzleUseCase(
            FakeBasicPuzzleRepository(
                listOf(BasicPuzzle("cat", "Cute Cat", "cat", Difficulty.EASY, listOf(3, 4)))
            )
        )

        assertNull(useCase("missing"))
    }

    private class FakeBasicPuzzleRepository(
        private val puzzles: List<BasicPuzzle>
    ) : BasicPuzzleRepository {

        override suspend fun getBasicPuzzles(): List<BasicPuzzle> = puzzles

        override suspend fun getBasicPuzzle(puzzleId: String): BasicPuzzle? =
            puzzles.firstOrNull { it.id == puzzleId }
    }
}
