package com.example.cozypuzzle.core.puzzle.engine

import com.example.cozypuzzle.core.puzzle.model.Difficulty
import com.example.cozypuzzle.core.puzzle.model.Puzzle
import com.example.cozypuzzle.core.puzzle.model.PuzzleConfig
import com.example.cozypuzzle.core.puzzle.model.PuzzleMode
import com.example.cozypuzzle.core.puzzle.model.PuzzlePiece
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GridPuzzleValidatorTest {

    private val validator = GridPuzzleValidator()
    private val generator = SeededPuzzleGenerator()

    @Test
    fun `solved puzzle is detected as solved`() {
        val puzzle = generator.generateSolved(config())

        assertTrue(validator.isSolved(puzzle))
        assertTrue(validator.isBoardValid(puzzle))
    }

    @Test
    fun `scrambled puzzle is not solved`() {
        val puzzle = generator.generate(config(), seed = 42L)

        assertFalse(validator.isSolved(puzzle))
    }

    @Test
    fun `piece in correct position but wrong rotation is not solved`() {
        val rotated = engine().rotate(generator.generateSolved(config()), pieceId = 0)

        assertFalse(validator.isSolved(rotated))
    }

    @Test
    fun `piece with correct rotation but wrong position is not solved`() {
        val swapped = engine().swap(generator.generateSolved(config()), 0, 1)

        assertFalse(validator.isSolved(swapped))
    }

    @Test
    fun `restoring all pieces makes the puzzle solved again`() {
        var puzzle: Puzzle = generator.generateSolved(config())
        puzzle = engine().rotate(puzzle, 3)
        puzzle = engine().swap(puzzle, 4, 5)
        assertFalse(validator.isSolved(puzzle))

        puzzle = engine().swap(puzzle, 4, 5)
        repeat(3) { puzzle = engine().rotate(puzzle, 3) }

        assertTrue(validator.isSolved(puzzle))
    }

    @Test
    fun `board is valid for every supported grid size`() {
        listOf(2 to 2, 3 to 3, 3 to 4, 4 to 4, 6 to 6).forEach { (rows, columns) ->
            val config = PuzzleConfig(rows, columns, PuzzleMode.BASIC, Difficulty.EASY)
            val scrambled = generator.generate(config, seed = 7L)
            val solved = generator.generateSolved(config)

            assertTrue("rows=${rows} columns=$columns", validator.isBoardValid(scrambled))
            assertTrue("rows=${rows} columns=$columns", validator.isBoardValid(solved))
        }
    }

    @Test
    fun `board with duplicate current positions is invalid`() {
        val config = config()
        val corruptPieces = (0 until 9).map { pieceId ->
            PuzzlePiece(
                id = pieceId,
                targetPosition = pieceId,
                currentPosition = 0,
                rotation = 0
            )
        }
        val corrupt = Puzzle(id = "corrupt", config = config, pieces = corruptPieces)

        assertFalse(validator.isBoardValid(corrupt))
    }

    @Test
    fun `board with wrong piece count is invalid`() {
        val config = config()
        val tooFewPieces = (0 until 8).map { pieceId ->
            PuzzlePiece(pieceId, pieceId, pieceId, 0)
        }
        val corrupt = Puzzle(id = "corrupt", config = config, pieces = tooFewPieces)

        assertFalse(validator.isBoardValid(corrupt))
    }

    @Test
    fun `board with non right angle rotation is invalid`() {
        val config = config()
        val tiltedPieces = (0 until 9).map { pieceId ->
            PuzzlePiece(pieceId, pieceId, pieceId, if (pieceId == 8) 45 else 0)
        }
        val corrupt = Puzzle(id = "corrupt", config = config, pieces = tiltedPieces)

        assertFalse(validator.isBoardValid(corrupt))
    }

    @Test
    fun `board with out of range position is invalid`() {
        val config = config()
        val piecesWithOverflow = (0 until 9).map { pieceId ->
            PuzzlePiece(pieceId, pieceId, currentPosition = if (pieceId == 0) 9 else pieceId, rotation = 0)
        }
        val corrupt = Puzzle(id = "corrupt", config = config, pieces = piecesWithOverflow)

        assertFalse(validator.isBoardValid(corrupt))
    }

    private fun engine() = GridPuzzleEngine()

    private fun config(rows: Int = 3, columns: Int = 3) =
        PuzzleConfig(rows, columns, PuzzleMode.BASIC, Difficulty.EASY)
}
