package com.example.cozypuzzle.core.puzzle.engine

import com.example.cozypuzzle.core.puzzle.model.Difficulty
import com.example.cozypuzzle.core.puzzle.model.PuzzleConfig
import com.example.cozypuzzle.core.puzzle.model.PuzzleMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class SeededPuzzleGeneratorTest {

    private val generator = SeededPuzzleGenerator()
    private val validator = GridPuzzleValidator()

    @Test
    fun `same seed produces identical puzzle`() {
        val config = PuzzleConfig(4, 4, PuzzleMode.BASIC, Difficulty.EXPERT)

        val first = generator.generate(config, seed = 12345L)
        val second = generator.generate(config, seed = 12345L)

        assertEquals(first, second)
        assertEquals(first.id, second.id)
    }

    @Test
    fun `different seeds produce different puzzles`() {
        val config = PuzzleConfig(4, 4, PuzzleMode.BASIC, Difficulty.EASY)

        val first = generator.generate(config, seed = 1L)
        val second = generator.generate(config, seed = 2L)

        assertNotEquals(first, second)
    }

    @Test
    fun `generated puzzle is never accidentally solved`() {
        val difficulties = Difficulty.entries

        for (difficulty in difficulties) {
            val config = PuzzleConfig(3, 3, PuzzleMode.FROG_POND, difficulty)
            for (seed in 1L..50L) {
                val puzzle = generator.generate(config, seed)
                assertTrue(
                    "difficulty=$difficulty seed=$seed unexpectedly solved",
                    !validator.isSolved(puzzle)
                )
            }
        }
    }

    @Test
    fun `generator respects different grid sizes`() {
        listOf(2 to 2, 3 to 3, 3 to 4, 4 to 4, 5 to 5, 6 to 6).forEach { (rows, columns) ->
            val config = PuzzleConfig(rows, columns, PuzzleMode.BASIC, Difficulty.MEDIUM)
            val puzzle = generator.generate(config, seed = 99L)

            assertEquals(rows * columns, puzzle.pieces.size)
            assertEquals((0 until rows * columns).toSet(), puzzle.pieces.map { it.id }.toSet())
            assertTrue(
                "rows=$rows columns=$columns board invalid",
                validator.isBoardValid(puzzle)
            )
        }
    }

    @Test
    fun `easy difficulty never rotates pieces`() {
        for (seed in 1L..20L) {
            val puzzle = generator.generate(PuzzleConfig(4, 4, PuzzleMode.BASIC, Difficulty.EASY), seed)

            assertTrue(
                "seed=$seed has rotated pieces on EASY",
                puzzle.pieces.all { it.rotation == 0 }
            )
        }
    }

    @Test
    fun `expert difficulty rotates most pieces`() {
        val puzzle = generator.generate(PuzzleConfig(4, 4, PuzzleMode.BASIC, Difficulty.EXPERT), seed = 7L)
        val rotatedCount = puzzle.pieces.count { it.rotation != 0 }

        assertTrue("expected rotated pieces but found none", rotatedCount > 0)
        assertTrue(rotatedCount >= puzzle.pieces.size / 2)
    }

    @Test
    fun `all rotations are right angle multiples`() {
        val puzzle = generator.generate(PuzzleConfig(5, 5, PuzzleMode.COZY_GARDEN, Difficulty.HARD), seed = 3L)

        assertTrue(puzzle.pieces.all { it.rotation % 90 == 0 && it.rotation in 0..270 })
    }

    @Test
    fun `explicitly requested solved puzzle is solved`() {
        val config = PuzzleConfig(4, 4, PuzzleMode.BASIC, Difficulty.HARD)

        val puzzle = generator.generateSolved(config)

        assertTrue(validator.isSolved(puzzle))
        assertTrue(validator.isBoardValid(puzzle))
    }

    @Test
    fun `invalid grid sizes are rejected`() {
        listOf(
            PuzzleConfig(1, 4, PuzzleMode.BASIC, Difficulty.EASY),
            PuzzleConfig(4, 0, PuzzleMode.BASIC, Difficulty.EASY),
            PuzzleConfig(-2, 3, PuzzleMode.BASIC, Difficulty.EASY)
        ).forEach { config ->
            assertThrows(IllegalArgumentException::class.java) {
                generator.generate(config, seed = 1L)
            }
            assertThrows(IllegalArgumentException::class.java) {
                generator.generateSolved(config)
            }
        }
    }

    @Test
    fun `daily style generation uses epoch day as deterministic seed`() {
        val config = PuzzleConfig(4, 4, PuzzleMode.BASIC, Difficulty.MEDIUM)
        val dailySeed = 20000L

        assertEquals(
            generator.generate(config, dailySeed),
            generator.generate(config, dailySeed)
        )
    }
}
