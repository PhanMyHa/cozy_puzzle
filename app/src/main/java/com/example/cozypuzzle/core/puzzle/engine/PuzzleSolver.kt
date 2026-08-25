package com.example.cozypuzzle.core.puzzle.engine

import com.example.cozypuzzle.core.puzzle.model.Puzzle
import com.example.cozypuzzle.core.puzzle.model.PuzzleAction

interface PuzzleSolver {

    suspend fun solve(puzzle: Puzzle): List<PuzzleAction>
}
