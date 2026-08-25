package com.example.cozypuzzle.domain.usecase.cat

import com.example.cozypuzzle.core.puzzle.model.Difficulty
import com.example.cozypuzzle.domain.model.CatLevel
import com.example.cozypuzzle.domain.repository.CatLevelRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class StartCatLevelUseCaseTest {

    private val levels = listOf(
        CatLevel("first_steps", "First Steps", 4, 4, Difficulty.EASY),
        CatLevel("long_way", "Long Way Home", 6, 6, Difficulty.EXPERT)
    )

    private fun useCase() = StartCatLevelUseCase(
        catLevelRepository = object : CatLevelRepository {
            override suspend fun getCatLevels(): List<CatLevel> = levels

            override suspend fun getCatLevel(levelId: String): CatLevel? =
                levels.firstOrNull { it.id == levelId }
        },
        generator = com.example.cozypuzzle.core.path.engine.CatBoardGenerator()
    )

    @Test
    fun `starting a level builds a board with the level configuration`() = runTest {
        val game = useCase()("first_steps", seed = 5L)

        assertEquals("First Steps", game.levelTitle)
        assertEquals(Difficulty.EASY, game.difficulty)
        assertEquals(16, game.board.cellCount)
        assertEquals(3, game.maxHints)
        assertTrue(game.board.tiles.containsKey(game.board.catPosition))
        assertTrue(game.board.tiles.containsKey(game.board.homePosition))
    }

    @Test
    fun `same seed produces the same board`() = runTest {
        val first = useCase()("first_steps", seed = 11L)
        val second = useCase()("first_steps", seed = 11L)

        assertEquals(first.board, second.board)
    }

    @Test
    fun `unknown level id is rejected`() = runTest {
        assertThrows(IllegalArgumentException::class.java) {
            kotlinx.coroutines.runBlocking { useCase()("missing", seed = 1L) }
        }
    }
}
