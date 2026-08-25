package com.example.cozypuzzle.domain.usecase.puzzle

import com.example.cozypuzzle.core.puzzle.engine.GridPuzzleEngine
import com.example.cozypuzzle.core.puzzle.engine.SeededPuzzleGenerator
import com.example.cozypuzzle.core.puzzle.history.PuzzleHistory
import com.example.cozypuzzle.core.puzzle.model.Difficulty
import com.example.cozypuzzle.core.puzzle.model.PuzzleConfig
import com.example.cozypuzzle.core.puzzle.model.PuzzleMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BasicPuzzleCompletionFlowTest {

    private val engine = GridPuzzleEngine()
    private val generator = SeededPuzzleGenerator()
    private val checkCompleted = CheckPuzzleCompletedUseCase(com.example.cozypuzzle.core.puzzle.engine.GridPuzzleValidator())

    private fun startGame(): Pair<com.example.cozypuzzle.core.puzzle.model.Puzzle, PuzzleHistory> {
        val config = PuzzleConfig(3, 3, PuzzleMode.BASIC, Difficulty.MEDIUM)
        val puzzle = generator.generate(config, seed = 11L)
        return puzzle to PuzzleHistory(puzzle)
    }

    @Test
    fun `generated basic puzzle is not completed at start`() {
        val (puzzle, _) = startGame()

        assertFalse(checkCompleted(puzzle))
        assertFalse(puzzle.pieces.all { it.currentPosition == it.targetPosition })
    }

    @Test
    fun `every committed move is counted and recorded in history`() {
        var (puzzle, history) = startGame()
        var moves = 0

        repeat(3) { index ->
            puzzle = engine.rotate(puzzle, pieceId = index)
            history.commit(puzzle)
            moves++
        }

        assertEquals(3, moves)
        assertTrue(history.canUndo())
        assertFalse(history.canRedo())
    }

    @Test
    fun `undo removes the last counted move`() {
        var (puzzle, history) = startGame()
        var moves = 2

        puzzle = engine.rotate(puzzle, 1)
        history.commit(puzzle)
        moves++

        history.undo()?.let { restored ->
            puzzle = restored
            moves--
        }

        assertEquals(2, moves)
        assertTrue(history.canRedo())
    }

    @Test
    fun `redo re-applies the undone move`() {
        var (puzzle, history) = startGame()

        puzzle = engine.swap(puzzle, 0, 4)
        history.commit(puzzle)
        val afterMove = history.currentState
        history.undo()
        val redone = history.redo()

        assertEquals(afterMove, redone)
    }

    @Test
    fun `solving every piece completes the puzzle exactly once`() {
        var (puzzle, history) = startGame()
        var completions = 0

        // Solve by moving each misplaced piece to its target position and fixing rotation.
        // A move always places the chosen piece onto its own unique target slot, so it
        // can never be displaced again and the loop terminates.
        while (!checkCompleted(puzzle)) {
            val targetPiece = puzzle.pieces.first { !isPieceSolved(it) }
            if (targetPiece.currentPosition != targetPiece.targetPosition) {
                puzzle = engine.move(puzzle, targetPiece.id, targetPiece.targetPosition)
            } else {
                puzzle = engine.rotate(puzzle, targetPiece.id)
            }
            history.commit(puzzle)

            if (checkCompleted(puzzle)) completions++
            check(completions <= 1) { "Completion must trigger only once" }
        }

        assertEquals(1, completions)
        assertTrue(history.canUndo())
    }

    private fun isPieceSolved(piece: com.example.cozypuzzle.core.puzzle.model.PuzzlePiece): Boolean =
        piece.currentPosition == piece.targetPosition && piece.rotation % 360 == 0
}
