package com.example.cozypuzzle.domain.usecase.puzzle

import com.example.cozypuzzle.core.puzzle.engine.SeededPuzzleGenerator
import com.example.cozypuzzle.core.puzzle.model.Difficulty
import com.example.cozypuzzle.core.puzzle.model.PuzzleConfig
import com.example.cozypuzzle.core.puzzle.model.PuzzleMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GetPuzzleHintUseCaseTest {

    private val useCase = GetPuzzleHintUseCase(com.example.cozypuzzle.core.puzzle.engine.GridPuzzleValidator())
    private val generator = SeededPuzzleGenerator()

    @Test
    fun `hint points to the first misplaced piece`() {
        val config = PuzzleConfig(3, 3, PuzzleMode.BASIC, Difficulty.MEDIUM)
        val puzzle = generator.generate(config, seed = 5L)
        val expected = puzzle.pieces.first { !isPieceSolved(it) }

        val hint = useCase(puzzle, hintsRemaining = 2)

        assertEquals(expected.id, hint?.pieceId)
        assertEquals(expected.targetPosition, hint?.correctPosition)
    }

    @Test
    fun `no hint when the puzzle is already solved`() {
        val config = PuzzleConfig(3, 3, PuzzleMode.BASIC, Difficulty.EASY)
        val solved = generator.generateSolved(config)

        assertNull(useCase(solved, hintsRemaining = 3))
    }

    @Test
    fun `no hint when no hints remain`() {
        val config = PuzzleConfig(3, 3, PuzzleMode.BASIC, Difficulty.HARD)
        val scrambled = generator.generate(config, seed = 9L)

        assertNull(useCase(scrambled, hintsRemaining = 0))
        assertNull(useCase(scrambled, hintsRemaining = -1))
    }

    private fun isPieceSolved(piece: com.example.cozypuzzle.core.puzzle.model.PuzzlePiece): Boolean =
        piece.currentPosition == piece.targetPosition && piece.rotation % 360 == 0
}
