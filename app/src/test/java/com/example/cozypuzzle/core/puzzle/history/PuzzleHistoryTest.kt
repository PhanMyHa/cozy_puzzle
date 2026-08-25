package com.example.cozypuzzle.core.puzzle.history

import com.example.cozypuzzle.core.puzzle.engine.GridPuzzleEngine
import com.example.cozypuzzle.core.puzzle.engine.SeededPuzzleGenerator
import com.example.cozypuzzle.core.puzzle.model.Difficulty
import com.example.cozypuzzle.core.puzzle.model.Puzzle
import com.example.cozypuzzle.core.puzzle.model.PuzzleConfig
import com.example.cozypuzzle.core.puzzle.model.PuzzleMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class PuzzleHistoryTest {

    private lateinit var base: Puzzle
    private lateinit var afterRotate: Puzzle
    private lateinit var afterSwap: Puzzle
    private lateinit var afterMove: Puzzle

    @Before
    fun setUp() {
        val engine = GridPuzzleEngine()
        val generator = SeededPuzzleGenerator()
        base = generator.generateSolved(config())
        afterRotate = engine.rotate(base, pieceId = 0)
        afterSwap = engine.swap(afterRotate, firstId = 1, secondId = 2)
        afterMove = engine.move(afterSwap, pieceId = 3, position = 0)
    }

    @Test
    fun `undo restores previous state`() {
        val history = PuzzleHistory(base)
        history.commit(afterRotate)

        assertEquals(base, history.undo())
    }

    @Test
    fun `redo restores undone state`() {
        val history = PuzzleHistory(base)
        history.commit(afterRotate)
        history.undo()

        assertEquals(afterRotate, history.redo())
    }

    @Test
    fun `undo on empty stack is safe`() {
        val history = PuzzleHistory(base)

        assertNull(history.undo())
        assertFalse(history.canUndo())
        assertEquals(base, history.currentState)
    }

    @Test
    fun `redo without prior undo is safe`() {
        val history = PuzzleHistory(base)

        assertNull(history.redo())
        assertFalse(history.canRedo())
    }

    @Test
    fun `multiple undos walk back through every action`() {
        val history = newFullHistory()

        assertEquals(afterMove, history.currentState)
        assertEquals(afterSwap, history.undo())
        assertEquals(afterRotate, history.undo())
        assertEquals(base, history.undo())

        assertFalse(history.canUndo())
        assertTrue(history.canRedo())
    }

    @Test
    fun `multiple redos replay every undone action in order`() {
        val history = newFullHistory()
        repeat(3) { history.undo() }
        assertEquals(base, history.currentState)

        assertEquals(afterRotate, history.redo())
        assertEquals(afterSwap, history.redo())
        assertEquals(afterMove, history.redo())

        assertFalse(history.canRedo())
    }

    @Test
    fun `committing a new action clears the redo stack`() {
        val history = newFullHistory()
        history.undo()
        history.undo()
        assertTrue(history.canRedo())

        val branchedState = GridPuzzleEngine().rotate(history.currentState, pieceId = 8)
        history.commit(branchedState)

        assertFalse(history.canRedo())
        assertNull(history.redo())
    }

    @Test
    fun `flags track stack availability`() {
        val history = PuzzleHistory(base)
        assertFalse(history.canUndo())
        assertFalse(history.canRedo())

        history.commit(afterRotate)
        assertTrue(history.canUndo())
        assertFalse(history.canRedo())

        history.undo()
        assertFalse(history.canUndo())
        assertTrue(history.canRedo())
    }

    @Test
    fun `clear resets history to a fresh state`() {
        val history = newFullHistory()

        history.clear(base)

        assertEquals(base, history.currentState)
        assertFalse(history.canUndo())
        assertFalse(history.canRedo())
    }

    private fun newFullHistory(): PuzzleHistory {
        val history = PuzzleHistory(base)
        history.commit(afterRotate)
        history.commit(afterSwap)
        history.commit(afterMove)
        return history
    }

    private fun config() =
        PuzzleConfig(3, 3, PuzzleMode.FIND_MY_CAT, Difficulty.EASY)
}
