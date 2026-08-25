package com.example.cozypuzzle.core.puzzle.engine

import com.example.cozypuzzle.core.puzzle.model.Difficulty
import com.example.cozypuzzle.core.puzzle.model.Puzzle
import com.example.cozypuzzle.core.puzzle.model.PuzzleAction
import com.example.cozypuzzle.core.puzzle.model.PuzzleConfig
import com.example.cozypuzzle.core.puzzle.model.PuzzleMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class GridPuzzleEngineTest {

    private val engine = GridPuzzleEngine()

    @Test
    fun `rotate turns piece by 90 degrees`() {
        val puzzle = solvedPuzzle()

        val rotated = engine.rotate(puzzle, pieceId = 4)

        assertEquals(90, rotated.pieces.first { it.id == 4 }.rotation)
    }

    @Test
    fun `rotating four times returns piece to original state`() {
        val puzzle = solvedPuzzle()

        var result = engine.rotate(puzzle, 2)
        repeat(3) { result = engine.rotate(result, 2) }

        assertEquals(puzzle, result)
    }

    @Test
    fun `rotate does not modify the original puzzle or other pieces`() {
        val puzzle = solvedPuzzle()
        val originalPieces = puzzle.pieces.toList()

        engine.rotate(puzzle, 0)

        assertEquals(originalPieces, puzzle.pieces)
    }

    @Test
    fun `swap exchanges positions of exactly two pieces`() {
        val puzzle = solvedPuzzle()
        val positionOfOne = currentPositionOf(puzzle, 1)
        val positionOfThree = currentPositionOf(puzzle, 3)

        val swapped = engine.swap(puzzle, firstId = 1, secondId = 3)

        assertEquals(positionOfThree, currentPositionOf(swapped, 1))
        assertEquals(positionOfOne, currentPositionOf(swapped, 3))
        assertEquals(9, swapped.pieces.map { it.currentPosition }.toSet().size)
    }

    @Test
    fun `swap preserves rotation values of both pieces`() {
        val puzzle = engine.rotate(solvedPuzzle(), 5)

        val swapped = engine.swap(puzzle, 0, 5)

        assertEquals(90, swapped.pieces.first { it.id == 5 }.rotation)
        assertEquals(0, swapped.pieces.first { it.id == 0 }.rotation)
    }

    @Test
    fun `swapping a piece with itself is rejected`() {
        val puzzle = solvedPuzzle()

        assertThrows(IllegalArgumentException::class.java) {
            engine.swap(puzzle, 1, 1)
        }
    }

    @Test
    fun `move onto occupied position exchanges the two pieces`() {
        val puzzle = solvedPuzzle()
        val targetPosition = currentPositionOf(puzzle, 6)

        val moved = engine.move(puzzle, pieceId = 2, position = targetPosition)

        assertEquals(targetPosition, currentPositionOf(moved, 2))
        assertEquals(currentPositionOf(puzzle, 2), currentPositionOf(moved, 6))
    }

    @Test
    fun `move to own position returns unchanged instance`() {
        val puzzle = solvedPuzzle()

        val moved = engine.move(puzzle, pieceId = 3, position = 3)

        assertEquals(puzzle, moved)
    }

    @Test
    fun `move outside board bounds is rejected`() {
        val puzzle = solvedPuzzle(3, 3)

        assertThrows(IllegalArgumentException::class.java) {
            engine.move(puzzle, pieceId = 0, position = 9)
        }
        assertThrows(IllegalArgumentException::class.java) {
            engine.move(puzzle, pieceId = 0, position = -1)
        }
    }

    @Test
    fun `operations on unknown piece id are rejected`() {
        val puzzle = solvedPuzzle()

        assertThrows(IllegalArgumentException::class.java) { engine.rotate(puzzle, 99) }
        assertThrows(IllegalArgumentException::class.java) { engine.swap(puzzle, 0, 99) }
        assertThrows(IllegalArgumentException::class.java) { engine.move(puzzle, 99, 0) }
    }

    @Test
    fun `applyAction dispatches rotate swap and move`() {
        val puzzle = solvedPuzzle()

        val viaRotateAction = engine.applyAction(puzzle, PuzzleAction.Rotate(pieceId = 1))
        val viaDirectCall = engine.rotate(puzzle, 1)
        assertEquals(viaDirectCall, viaRotateAction)

        val viaSwapAction = engine.applyAction(puzzle, PuzzleAction.Swap(firstId = 1, secondId = 2))
        assertEquals(engine.swap(puzzle, 1, 2), viaSwapAction)

        val viaMoveAction = engine.applyAction(puzzle, PuzzleAction.Move(pieceId = 1, position = 8))
        assertEquals(engine.move(puzzle, 1, 8), viaMoveAction)
    }

    @Test
    fun `sequence of actions can restore solved state`() {
        var puzzle: Puzzle = solvedPuzzle()

        puzzle = engine.rotate(puzzle, 7)
        puzzle = engine.swap(puzzle, 0, 4)
        puzzle = engine.move(puzzle, 0, currentPositionOf(puzzle, 4))
        assertNotEquals(solvedPuzzle(), puzzle)

        puzzle = engine.move(puzzle, 0, currentPositionOf(puzzle, 4))
        puzzle = engine.swap(puzzle, 0, 4)
        repeat(3) { puzzle = engine.rotate(puzzle, 7) }

        assertEquals(solvedPuzzle(), puzzle)
    }

    private fun solvedPuzzle(rows: Int = 3, columns: Int = 3): Puzzle =
        SeededPuzzleGenerator().generateSolved(
            PuzzleConfig(rows, columns, PuzzleMode.BASIC, Difficulty.EASY)
        )

    private fun currentPositionOf(puzzle: Puzzle, pieceId: Int): Int =
        puzzle.pieces.first { it.id == pieceId }.currentPosition
}
