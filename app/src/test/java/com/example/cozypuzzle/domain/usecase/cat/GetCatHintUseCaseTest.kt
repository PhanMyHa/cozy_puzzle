package com.example.cozypuzzle.domain.usecase.cat

import com.example.cozypuzzle.core.path.model.CatBoard
import com.example.cozypuzzle.core.path.model.Direction
import com.example.cozypuzzle.core.path.model.PathTile
import com.example.cozypuzzle.core.path.solver.AStarPathFinder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GetCatHintUseCaseTest {

    private val engine = com.example.cozypuzzle.core.path.engine.CatBoardEngine()
    private val useCase = GetCatHintUseCase(engine, AStarPathFinder())

    /**
     * 1x3 corridor. Cell 0 (cat) opens EAST, cell 1 is a rotatable straight
     * tile currently opening NORTH/SOUTH, cell 2 (home) opens WEST.
     * Rotating cell 1 once opens the corridor.
     */
    private fun board() = CatBoard(
        id = "hint",
        rows = 1,
        columns = 3,
        tiles = mapOf(
            0 to PathTile(0, setOf(Direction.EAST)),
            1 to PathTile(1, setOf(Direction.NORTH, Direction.SOUTH)),
            2 to PathTile(2, setOf(Direction.WEST))
        ),
        obstacles = emptySet(),
        catPosition = 0,
        homePosition = 2
    )

    @Test
    fun `hint finds the rotation that opens the path`() {
        val hint = useCase(board(), hintsRemaining = 2)

        assertEquals(listOf(1), hint?.cells)
        assertTrue(hint?.action is com.example.cozypuzzle.core.path.model.CatAction.Rotate)
    }

    @Test
    fun `hint never modifies the board`() {
        val original = board()

        useCase(original, hintsRemaining = 1)

        assertEquals(setOf(Direction.NORTH, Direction.SOUTH), original.tiles.getValue(1).connections)
    }

    @Test
    fun `no hint when no hints remain`() {
        assertNull(useCase(board(), hintsRemaining = 0))
    }

    @Test
    fun `no hint when path already exists`() {
        val solved = engine.rotate(board(), cell = 1)
        assertTrue(AStarPathFinder().findPath(solved).found)

        assertNull(useCase(solved, hintsRemaining = 3))
    }

    @Test
    fun `hint falls back to swaps when no rotation helps`() {
        // 2x2 board: cat(0) opens SOUTH towards cell 2, home(3) opens WEST.
        // The corner tile needed at cell 2 sits at cell 1; no single rotation
        // completes the route, but swapping cell 1 and cell 2 does.
        val swappedLayout = CatBoard(
            id = "swap-hint",
            rows = 2,
            columns = 2,
            tiles = mapOf(
                0 to PathTile(0, setOf(Direction.SOUTH)),
                1 to PathTile(1, setOf(Direction.NORTH, Direction.EAST)),
                2 to PathTile(2, setOf(Direction.WEST, Direction.EAST)),
                3 to PathTile(3, setOf(Direction.WEST))
            ),
            obstacles = emptySet(),
            catPosition = 0,
            homePosition = 3
        )

        val hint = useCase(swappedLayout, hintsRemaining = 1)

        assertTrue(hint?.action is com.example.cozypuzzle.core.path.model.CatAction.Swap)
    }
}
