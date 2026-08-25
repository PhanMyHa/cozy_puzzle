package com.example.cozypuzzle.core.path.solver

import com.example.cozypuzzle.core.path.model.CatBoard
import com.example.cozypuzzle.core.path.model.Direction
import com.example.cozypuzzle.core.path.model.PathTile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AStarPathFinderTest {

    private val finder = AStarPathFinder()

    /** 2x3 grid, straight corridor along the top row: cat(0) -> 1 -> home(2). */
    private fun corridorBoard(obstacle: Int? = null) = CatBoard(
        id = "corridor",
        rows = 2,
        columns = 3,
        tiles = mapOf(
            0 to PathTile(0, setOf(Direction.EAST)),
            1 to PathTile(1, setOf(Direction.WEST, Direction.EAST)),
            2 to PathTile(2, setOf(Direction.WEST))
        ),
        obstacles = obstacle?.let { setOf(it) } ?: emptySet(),
        catPosition = 0,
        homePosition = 2
    )

    @Test
    fun `finds a straight path`() {
        val result = finder.findPath(corridorBoard())

        assertTrue(result.found)
        assertEquals(listOf(0, 1, 2), result.cells)
    }

    @Test
    fun `reports not found when a tile is missing`() {
        val board = CatBoard(
            id = "broken",
            rows = 1,
            columns = 3,
            tiles = mapOf(
                0 to PathTile(0, setOf(Direction.EAST)),
                2 to PathTile(2, setOf(Direction.WEST))
            ),
            obstacles = emptySet(),
            catPosition = 0,
            homePosition = 2
        )

        assertFalse(finder.findPath(board).found)
    }

    @Test
    fun `reports not found when connections do not meet`() {
        val board = CatBoard(
            id = "misaligned",
            rows = 1,
            columns = 3,
            tiles = mapOf(
                0 to PathTile(0, setOf(Direction.NORTH)),
                1 to PathTile(1, setOf(Direction.SOUTH)),
                2 to PathTile(2, setOf(Direction.WEST))
            ),
            obstacles = emptySet(),
            catPosition = 0,
            homePosition = 2
        )

        assertFalse(finder.findPath(board).found)
    }

    @Test
    fun `routes around an obstacle using the second row`() {
        val board = CatBoard(
            id = "detour",
            rows = 2,
            columns = 3,
            tiles = mapOf(
                0 to PathTile(0, setOf(Direction.EAST, Direction.SOUTH)),
                1 to PathTile(1, setOf(Direction.WEST)),
                2 to PathTile(2, setOf(Direction.WEST, Direction.SOUTH)),
                3 to PathTile(3, setOf(Direction.NORTH, Direction.EAST)),
                4 to PathTile(4, setOf(Direction.WEST, Direction.EAST)),
                5 to PathTile(5, setOf(Direction.NORTH, Direction.WEST))
            ),
            obstacles = emptySet(),
            catPosition = 0,
            homePosition = 2
        ).let { it.copy(obstacles = setOf(1)) }

        val result = finder.findPath(board)

        assertTrue(result.found)
        assertEquals(listOf(0, 3, 4, 5, 2), result.cells)
    }

    @Test
    fun `obstacle on the only route blocks the cat`() {
        val blocked = CatBoard(
            id = "blocked",
            rows = 2,
            columns = 3,
            tiles = mapOf(
                0 to PathTile(0, setOf(Direction.SOUTH)),
                3 to PathTile(3, setOf(Direction.NORTH, Direction.EAST)),
                4 to PathTile(4, setOf(Direction.WEST, Direction.EAST)),
                5 to PathTile(5, setOf(Direction.WEST, Direction.NORTH))
            ),
            obstacles = setOf(4),
            catPosition = 0,
            homePosition = 5
        )

        assertFalse(finder.findPath(blocked).found)
    }

    @Test
    fun `cat already at home is trivially found`() {
        val board = CatBoard(
            id = "home",
            rows = 2,
            columns = 2,
            tiles = emptyMap(),
            obstacles = emptySet(),
            catPosition = 3,
            homePosition = 3
        )

        val result = finder.findPath(board)

        assertTrue(result.found)
        assertEquals(listOf(3), result.cells)
    }
}
