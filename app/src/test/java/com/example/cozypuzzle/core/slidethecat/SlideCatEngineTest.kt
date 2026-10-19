package com.example.cozypuzzle.core.slidethecat

import com.example.cozypuzzle.R
import com.example.cozypuzzle.core.slidethecat.engine.SlideCatEngine
import com.example.cozypuzzle.core.slidethecat.model.SlideCatDifficulty
import com.example.cozypuzzle.core.slidethecat.model.SlideCatLevel
import com.example.cozypuzzle.core.slidethecat.model.SlideCatState
import com.example.cozypuzzle.core.slidethecat.model.SlideTile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SlideCatEngineTest {

    private lateinit var engine: SlideCatEngine
    private lateinit var easyLevel: SlideCatLevel
    private lateinit var mediumLevel: SlideCatLevel

    @Before
    fun setUp() {
        engine = SlideCatEngine()
        easyLevel = SlideCatLevel(
            id = "slide_1",
            number = 1,
            title = "Level 1",
            difficulty = SlideCatDifficulty.EASY,
            gridSize = 3,
            imageRes = R.drawable.img_puzzle_cute_cat,
            shuffleMoves = 15,
            threeStarMoves = 22,
            twoStarMoves = 38
        )
        mediumLevel = SlideCatLevel(
            id = "slide_med_1",
            number = 1,
            title = "Level 1",
            difficulty = SlideCatDifficulty.MEDIUM,
            gridSize = 4,
            imageRes = R.drawable.img_puzzle_cute_cat,
            shuffleMoves = 30,
            threeStarMoves = 48,
            twoStarMoves = 80
        )
    }

    @Test
    fun `initial board has correct size, exactly one empty slot and is not solved`() {
        val state = engine.createInitialState(easyLevel, seed = 42L)
        assertEquals(9, state.board.size)
        assertEquals(1, state.board.count { it == null })
        assertEquals(8, state.board.filterNotNull().map { it.id }.distinct().size)
        assertEquals(0, state.movesCount)
        assertFalse(state.isCompleted)
        assertFalse(state.isPaused)
        assertEquals(3, state.hintsRemaining)
    }

    @Test
    fun `only tiles adjacent to empty slot can move`() {
        val state = engine.createInitialState(easyLevel, seed = 42L)
        val emptyIndex = state.emptyIndex
        val adjacent = engine.getAdjacentIndices(emptyIndex, 3)

        for (i in 0 until 9) {
            if (i in adjacent) {
                assertTrue("Slot $i should be movable", engine.canMove(state, i))
            } else {
                assertFalse("Slot $i should not be movable", engine.canMove(state, i))
            }
        }
    }

    @Test
    fun `moving adjacent tile swaps tile with empty slot and increments moves`() {
        val state = engine.createInitialState(easyLevel, seed = 42L)
        val emptyIndex = state.emptyIndex
        val movingIndex = engine.getAdjacentIndices(emptyIndex, 3).first()
        val movingTile = state.board[movingIndex]
        assertNotNull(movingTile)

        val nextState = engine.moveTile(state, movingIndex)
        assertEquals(1, nextState.movesCount)
        assertEquals(movingIndex, nextState.emptyIndex)
        assertNull(nextState.board[movingIndex])
        assertEquals(movingTile, nextState.board[emptyIndex])
    }

    @Test
    fun `moving non-adjacent tile does not change board or moves count`() {
        val state = engine.createInitialState(easyLevel, seed = 42L)
        val emptyIndex = state.emptyIndex
        val nonAdjacent = (0 until 9).first { it != emptyIndex && !engine.isAdjacent(it, emptyIndex, 3) }

        val nextState = engine.moveTile(state, nonAdjacent)
        assertEquals(state, nextState)
        assertEquals(0, nextState.movesCount)
    }

    @Test
    fun `completing the puzzle awards stars according to move thresholds`() {
        // Construct a board that is 1 move away from solved
        // Solved state: 0, 1, 2, 3, 4, 5, 6, 7, null
        // 1 move away: 0, 1, 2, 3, 4, 5, 6, null, 7 (empty is at 7, 7 is at 8)
        val nearSolvedBoard = mutableListOf<SlideTile?>()
        for (i in 0..6) nearSolvedBoard.add(SlideTile(i, i))
        nearSolvedBoard.add(null) // empty at slot 7
        nearSolvedBoard.add(SlideTile(7, 7)) // tile 7 at slot 8

        val almostState = SlideCatState(
            level = easyLevel,
            board = nearSolvedBoard,
            emptyIndex = 7,
            movesCount = 10
        )

        // Move tile 7 from slot 8 to slot 7
        val solvedState = engine.moveTile(almostState, 8)
        assertTrue(solvedState.isCompleted)
        assertEquals(11, solvedState.movesCount)
        assertEquals(8, solvedState.emptyIndex)
        assertEquals(3, solvedState.starsEarned)
    }

    @Test
    fun `stars awarded decreases when moves exceed thresholds`() {
        assertEquals(3, engine.calculateStars(20, easyLevel))
        assertEquals(2, engine.calculateStars(30, easyLevel))
        assertEquals(1, engine.calculateStars(50, easyLevel))

        assertEquals(3, engine.calculateStars(45, mediumLevel))
        assertEquals(2, engine.calculateStars(65, mediumLevel))
        assertEquals(1, engine.calculateStars(90, mediumLevel))
    }

    @Test
    fun `hint decrements remaining hints and sets showTileNumbersHint`() {
        var state = engine.createInitialState(easyLevel)
        assertEquals(3, state.hintsRemaining)
        assertFalse(state.showTileNumbersHint)

        state = engine.useHint(state)
        assertEquals(2, state.hintsRemaining)
        assertEquals(1, state.hintsUsed)
        assertTrue(state.showTileNumbersHint)

        state = engine.hideTileNumbersHint(state)
        assertFalse(state.showTileNumbersHint)
    }

    @Test
    fun `restart resets moves, hints and timer`() {
        var state = engine.createInitialState(easyLevel, seed = 12L)
        val validMove = engine.getAdjacentIndices(state.emptyIndex, 3).first()
        state = engine.moveTile(state, validMove)
        assertEquals(1, state.movesCount)

        val restarted = engine.restart(state, seed = 99L)
        assertEquals(0, restarted.movesCount)
        assertEquals(3, restarted.hintsRemaining)
        assertFalse(restarted.isCompleted)
    }

    @Test
    fun `medium level initializes with 4x4 grid`() {
        val state = engine.createInitialState(mediumLevel, seed = 50L)
        assertEquals(16, state.board.size)
        assertEquals(1, state.board.count { it == null })
        assertEquals(15, state.board.filterNotNull().size)
    }
}
