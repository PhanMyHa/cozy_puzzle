package com.example.cozypuzzle.core.puzzle.engine

import com.example.cozypuzzle.core.puzzle.model.Puzzle
import com.example.cozypuzzle.core.puzzle.model.PuzzlePiece

interface PuzzleValidator {

    fun isSolved(puzzle: Puzzle): Boolean

    fun isPieceSolved(piece: PuzzlePiece): Boolean

    fun isBoardValid(puzzle: Puzzle): Boolean
}
