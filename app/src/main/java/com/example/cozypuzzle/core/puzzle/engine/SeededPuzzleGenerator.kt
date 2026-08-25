package com.example.cozypuzzle.core.puzzle.engine

import com.example.cozypuzzle.core.puzzle.model.Difficulty
import com.example.cozypuzzle.core.puzzle.model.Puzzle
import com.example.cozypuzzle.core.puzzle.model.PuzzleConfig
import com.example.cozypuzzle.core.puzzle.model.PuzzlePiece
import java.util.Random

class SeededPuzzleGenerator : PuzzleGenerator {

    override fun generate(config: PuzzleConfig, seed: Long): Puzzle {
        requireValid(config)
        val random = Random(seed)
        var candidate = scrambledPuzzle(config, "$seed", random)
        var attempt = 1
        while (validator.isSolved(candidate) && attempt < MAX_SHUFFLE_ATTEMPTS) {
            candidate = scrambledPuzzle(config, "$seed-$attempt", random)
            attempt++
        }
        check(!validator.isSolved(candidate)) {
            "Could not generate an unsolved puzzle for ${config.rows}x${config.columns} with seed $seed"
        }
        return candidate
    }

    fun generateSolved(config: PuzzleConfig): Puzzle {
        requireValid(config)
        return Puzzle(
            id = puzzleId(config, SOLVED_ID_MARKER),
            config = config,
            pieces = solvedPieces(config)
        )
    }

    private fun scrambledPuzzle(
        config: PuzzleConfig,
        idSuffix: String,
        random: Random
    ): Puzzle {
        val positions = (0 until config.pieceCount).shuffled(random)
        val pieces = solvedPieces(config).mapIndexed { index, piece ->
            piece.copy(
                currentPosition = positions[index],
                rotation = rotationFor(config.difficulty, random)
            )
        }
        return Puzzle(id = puzzleId(config, idSuffix), config = config, pieces = pieces)
    }

    private fun solvedPieces(config: PuzzleConfig): List<PuzzlePiece> =
        (0 until config.pieceCount).map { pieceId ->
            PuzzlePiece(
                id = pieceId,
                targetPosition = pieceId,
                currentPosition = pieceId,
                rotation = 0
            )
        }

    private fun rotationFor(difficulty: Difficulty, random: Random): Int {
        if (random.nextDouble() >= difficulty.rotateProbability) return 0
        val steps = random.nextInt(MAX_ROTATION_STEPS - 1) + 1
        return steps * ROTATION_DEGREES
    }

    private fun requireValid(config: PuzzleConfig) {
        require(config.rows >= MIN_GRID_SIZE && config.columns >= MIN_GRID_SIZE) {
            "Grid ${config.rows}x${config.columns} too small, minimum is ${MIN_GRID_SIZE}x$MIN_GRID_SIZE"
        }
    }

    private fun puzzleId(config: PuzzleConfig, suffix: Any): String =
        "${config.mode.name}-${config.rows}x${config.columns}-$suffix"

    private companion object {
        const val ROTATION_DEGREES = 90
        const val MAX_ROTATION_STEPS = 4
        const val MIN_GRID_SIZE = 2
        const val MAX_SHUFFLE_ATTEMPTS = 100
        const val SOLVED_ID_MARKER = "solved"

        val validator = GridPuzzleValidator()
    }
}
