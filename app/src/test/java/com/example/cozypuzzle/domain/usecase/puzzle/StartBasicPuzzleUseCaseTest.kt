package com.example.cozypuzzle.domain.usecase.puzzle

import com.example.cozypuzzle.core.puzzle.engine.GridPuzzleValidator
import com.example.cozypuzzle.core.puzzle.engine.SeededPuzzleGenerator
import com.example.cozypuzzle.core.puzzle.model.Difficulty
import com.example.cozypuzzle.data.datasource.BundledBasicPuzzleDataSource
import com.example.cozypuzzle.domain.model.BasicPuzzle
import com.example.cozypuzzle.domain.model.BasicPuzzleConfig
import com.example.cozypuzzle.domain.repository.BasicPuzzleRepository
import com.example.cozypuzzle.domain.repository.ConfigRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class StartBasicPuzzleUseCaseTest {

    private val validator = GridPuzzleValidator()

    @Test
    fun `starting a puzzle produces an unsolved board of the requested size`() = runTest {
        val game = startUseCase()("cute_cat", requestedSize = 3, seed = 42L)

        assertEquals(9, game.puzzle.pieces.size)
        assertEquals(3, game.gridSize)
        assertNotEquals(true, validator.isSolved(game.puzzle))
        assertTrue(validator.isBoardValid(game.puzzle))
    }

    @Test
    fun `puzzle configuration comes from the selected puzzle metadata`() = runTest {
        val game = startUseCase()("frog_pond", requestedSize = 4, seed = 1L)

        val config = game.puzzle.config
        assertEquals(Difficulty.HARD, config.difficulty)
        assertEquals("frog_pond", config.contentId)
        assertEquals("frog_pond", game.imageId)
        assertEquals("Frog Pond", game.puzzleTitle)
    }

    @Test
    fun `same seed and puzzle produce the same board`() = runTest {
        val useCase = startUseCase()

        val first = useCase("cute_cat", requestedSize = 3, seed = 7L)
        val second = useCase("cute_cat", requestedSize = 3, seed = 7L)

        assertEquals(first.puzzle, second.puzzle)
    }

    @Test
    fun `hint allowance comes from remote config`() = runTest {
        val game = startUseCase(maxHints = 2)("cute_cat", requestedSize = 3, seed = 1L)

        assertEquals(2, game.maxHints)
    }

    @Test
    fun `grid size outside the puzzle sizes is rejected`() = runTest {
        val useCase = startUseCase()

        assertThrows(IllegalArgumentException::class.java) {
            kotlinx.coroutines.runBlocking {
                useCase("cute_cat", requestedSize = 6, seed = 1L)
            }
        }
    }

    @Test
    fun `grid size disabled by remote config is rejected`() = runTest {
        val useCase = startUseCase(availableSizes = listOf(3, 4))

        assertThrows(IllegalArgumentException::class.java) {
            kotlinx.coroutines.runBlocking {
                useCase("little_garden", requestedSize = 5, seed = 1L)
            }
        }
    }

    @Test
    fun `unknown puzzle id is rejected`() = runTest {
        val useCase = startUseCase()

        assertThrows(IllegalArgumentException::class.java) {
            kotlinx.coroutines.runBlocking {
                useCase("nope", requestedSize = 3, seed = 1L)
            }
        }
    }

    @Test
    fun `basic mode disabled by remote config is rejected`() = runTest {
        val useCase = startUseCase(enabled = false)

        assertThrows(IllegalStateException::class.java) {
            kotlinx.coroutines.runBlocking {
                useCase("cute_cat", requestedSize = 3, seed = 1L)
            }
        }
    }

    private fun startUseCase(
        enabled: Boolean = true,
        availableSizes: List<Int> = listOf(3, 4, 5, 6),
        maxHints: Int = 3
    ): StartBasicPuzzleUseCase {
        val catalog = BundledBasicPuzzleDataSource()
        val configFlow = MutableStateFlow(
            BasicPuzzleConfig(
                enabled = enabled,
                availableSizes = availableSizes,
                defaultSize = availableSizes.first(),
                categories = emptyList(),
                allowRotate = true,
                allowSwap = true,
                allowDrag = true,
                maxHints = maxHints
            )
        )
        return StartBasicPuzzleUseCase(
            basicPuzzleRepository = object : BasicPuzzleRepository {
                override suspend fun getBasicPuzzles(): List<BasicPuzzle> = catalog.getPuzzles()

                override suspend fun getBasicPuzzle(puzzleId: String): BasicPuzzle? =
                    catalog.getPuzzle(puzzleId)
            },
            configRepository = object : ConfigRepository {
                override val basicPuzzleConfig: MutableStateFlow<BasicPuzzleConfig> = configFlow

                override suspend fun refreshBasicPuzzleConfig() = Unit
            },
            generator = SeededPuzzleGenerator()
        )
    }
}
