package com.example.cozypuzzle.core.puzzle.engine

import com.example.cozypuzzle.core.puzzle.model.Puzzle
import com.example.cozypuzzle.core.puzzle.model.PuzzleAction

interface PuzzleEngine {

    fun rotate(puzzle: Puzzle, pieceId: Int): Puzzle

    fun swap(puzzle: Puzzle, firstId: Int, secondId: Int): Puzzle

    fun move(puzzle: Puzzle, pieceId: Int, position: Int): Puzzle

    fun applyAction(puzzle: Puzzle, action: PuzzleAction): Puzzle
}
