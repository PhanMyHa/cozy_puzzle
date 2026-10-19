package com.example.cozypuzzle.data.datasource

import com.example.cozypuzzle.core.puzzle.model.Difficulty
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BundledDailyPuzzleDataSourceTest {

    private val dataSource = BundledDailyPuzzleDataSource()

    @Test
    fun `loads the bundled catalog`() = runTest {
        val puzzles = dataSource.getPuzzles()

        assertEquals(7, puzzles.size)
    }

    @Test
    fun `puzzle ids are unique`() = runTest {
        val puzzles = dataSource.getPuzzles()

        assertEquals(puzzles.size, puzzles.map { it.id }.toSet().size)
    }

    @Test
    fun `every puzzle has metadata and a recommended brush`() = runTest {
        dataSource.getPuzzles().forEach { puzzle ->
            assertTrue(puzzle.id.isNotBlank())
            assertTrue(puzzle.title.isNotBlank())
            assertNotNull(puzzle.difficulty)
            assertTrue(puzzle.recommendedBrushId != null)
        }
    }

    @Test
    fun `difficulties cover the full range`() = runTest {
        val difficulties = dataSource.getPuzzles().map { it.difficulty }.toSet()

        assertEquals(Difficulty.entries.toSet(), difficulties)
    }

    /**
     * Every bundled board must be solvable. The stroke travels between cell
     * centers, so 4-connected reachability through free cells guarantees a
     * collision-free route exists (diagonal squeezes are intentionally not
     * allowed: they would clip the thick stroke).
     */
    @Test
    fun `every puzzle is solvable from start to goal`() = runTest {
        dataSource.getPuzzles().forEach { puzzle ->
            assertTrue(
                "Puzzle '${puzzle.id}' has no path from start to goal",
                isSolvable(puzzle)
            )
        }
    }

    @Test
    fun `getPuzzle returns matching puzzle by id`() = runTest {
        val puzzle = dataSource.getPuzzle("morning_meadow")

        assertNotNull(puzzle)
        assertEquals("Morning Meadow", puzzle?.title)
    }

    @Test
    fun `getPuzzle returns null for unknown id`() = runTest {
        assertNull(dataSource.getPuzzle("does_not_exist"))
    }

    private fun isSolvable(puzzle: com.example.cozypuzzle.domain.model.DailyPuzzle): Boolean {
        val size = puzzle.boardSize
        val visited = BooleanArray(puzzle.cellCount)
        val queue = ArrayDeque<Int>()
        queue += puzzle.startCell
        visited[puzzle.startCell] = true

        while (queue.isNotEmpty()) {
            val cell = queue.removeFirst()
            if (cell == puzzle.goalCell) return true
            val row = cell / size
            val column = cell % size
            listOf(cell - 1 to (column > 0), cell + 1 to (column < size - 1),
                cell - size to (row > 0), cell + size to (row < size - 1))
                .forEach { (neighbor, inBounds) ->
                    if (inBounds && !visited[neighbor] && neighbor !in puzzle.obstacleCells) {
                        visited[neighbor] = true
                        queue += neighbor
                    }
                }
        }
        return false
    }
}
