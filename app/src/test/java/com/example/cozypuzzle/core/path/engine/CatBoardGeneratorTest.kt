package com.example.cozypuzzle.core.path.engine

import com.example.cozypuzzle.core.path.solver.AStarPathFinder
import com.example.cozypuzzle.core.puzzle.model.Difficulty
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CatBoardGeneratorTest {

    private val generator = CatBoardGenerator()
    private val finder = AStarPathFinder()

    @Test
    fun `same seed produces the same board`() {
        val first = generate(seed = 42L)
        val second = generate(seed = 42L)

        assertEquals(first, second)
    }

    @Test
    fun `different seeds usually produce different boards`() {
        val first = generate(seed = 1L)
        val second = generate(seed = 2L)

        assertNotEquals(first, second)
    }

    @Test
    fun `cat and home cells always hold tiles and are not obstacles`() {
        repeat(20) { seed ->
            val board = generate(rows = 5, columns = 5, difficulty = Difficulty.EXPERT, seed = seed.toLong())

            assertTrue(board.tiles.containsKey(board.catPosition))
            assertTrue(board.tiles.containsKey(board.homePosition))
            assertTrue(!board.obstacles.contains(board.catPosition))
            assertTrue(!board.obstacles.contains(board.homePosition))
        }
    }

    @Test
    fun `generated board starts unsolved`() {
        repeat(10) { seed ->
            val board = generate(seed = seed.toLong())

            assertEquals(false, finder.findPath(board).found)
        }
    }

    @Test
    fun `obstacles stay inside the board bounds`() {
        val board = generate(rows = 4, columns = 4, difficulty = Difficulty.HARD, seed = 9L)

        board.obstacles.forEach { cell ->
            assertTrue(cell in 0 until board.cellCount)
        }
    }

    @Test
    fun `too small boards are rejected`() {
        org.junit.Assert.assertThrows(IllegalArgumentException::class.java) {
            generator.generate("tiny", rows = 1, columns = 3, difficulty = Difficulty.EASY, seed = 1L)
        }
    }

    @Test
    fun `scrambled tiles are rotated or misplaced relative to the carved solution`() {
        val board = generate(seed = 7L)

        val misplaced = board.tiles.any { (cell, tile) ->
            tile.rotation % 360 != 0 || cell != tile.id
        }
        assertTrue(misplaced)
    }

    private fun generate(
        rows: Int = 4,
        columns: Int = 4,
        difficulty: Difficulty = Difficulty.MEDIUM,
        seed: Long
    ) = generator.generate("level", rows, columns, difficulty, seed)
}
