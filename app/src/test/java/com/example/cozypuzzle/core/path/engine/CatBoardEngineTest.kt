package com.example.cozypuzzle.core.path.engine

import com.example.cozypuzzle.core.path.model.CatBoard
import com.example.cozypuzzle.core.path.model.Direction
import com.example.cozypuzzle.core.path.model.PathTile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class CatBoardEngineTest {

    private val engine = CatBoardEngine()

    private fun board() = CatBoard(
        id = "test",
        rows = 2,
        columns = 3,
        tiles = mapOf(
            0 to PathTile(0, setOf(Direction.EAST)),
            1 to PathTile(1, setOf(Direction.WEST, Direction.SOUTH)),
            4 to PathTile(4, setOf(Direction.NORTH))
        ),
        obstacles = setOf(2),
        catPosition = 0,
        homePosition = 5
    )

    @Test
    fun `rotate turns tile connections clockwise`() {
        val rotated = engine.rotate(board(), cell = 1)

        val connections = rotated.tiles.getValue(1).connections
        assertEquals(setOf(Direction.NORTH, Direction.WEST), connections)
    }

    @Test
    fun `four rotations restore the original tile`() {
        var current = board()
        repeat(4) { current = engine.rotate(current, cell = 1) }

        assertEquals(board().tiles.getValue(1), current.tiles.getValue(1))
    }

    @Test
    fun `rotate does not mutate the original board`() {
        val original = board()
        val originalTiles = original.tiles.toMap()

        engine.rotate(original, cell = 1)

        assertEquals(originalTiles, original.tiles)
    }

    @Test
    fun `swap exchanges the tiles of two cells`() {
        val swapped = engine.swap(board(), firstCell = 1, secondCell = 4)

        assertEquals(PathTile(4, setOf(Direction.NORTH)), swapped.tiles.getValue(1))
        assertEquals(PathTile(1, setOf(Direction.WEST, Direction.SOUTH)), swapped.tiles.getValue(4))
    }

    @Test
    fun `rotating an obstacle cell is rejected`() {
        assertThrows(IllegalArgumentException::class.java) {
            engine.rotate(board(), cell = 2)
        }
    }

    @Test
    fun `manipulating cat or home cells is rejected`() {
        assertThrows(IllegalArgumentException::class.java) { engine.rotate(board(), cell = 0) }
        assertThrows(IllegalArgumentException::class.java) { engine.swap(board(), 0, 5) }
    }

    @Test
    fun `swapping a cell without a tile is rejected`() {
        assertThrows(IllegalArgumentException::class.java) {
            engine.swap(board(), firstCell = 1, secondCell = 3)
        }
    }

    @Test
    fun `out of bounds cells are rejected`() {
        assertThrows(IllegalArgumentException::class.java) { engine.rotate(board(), cell = 6) }
        assertThrows(IllegalArgumentException::class.java) { engine.rotate(board(), cell = -1) }
    }

    @Test
    fun `applyAction dispatches rotate and swap`() {
        val puzzle = board()

        val viaRotateAction = engine.applyAction(puzzle, com.example.cozypuzzle.core.path.model.CatAction.Rotate(cell = 1))
        assertEquals(engine.rotate(puzzle, 1), viaRotateAction)

        val viaSwapAction = engine.applyAction(puzzle, com.example.cozypuzzle.core.path.model.CatAction.Swap(firstCell = 1, secondCell = 4))
        assertEquals(engine.swap(puzzle, 1, 4), viaSwapAction)
    }

    @Test
    fun `swap with itself is rejected`() {
        assertThrows(IllegalArgumentException::class.java) {
            engine.swap(board(), firstCell = 1, secondCell = 1)
        }
    }

    @Test
    fun `rotated board differs from original`() {
        assertNotEquals(board(), engine.rotate(board(), 1))
    }
}
