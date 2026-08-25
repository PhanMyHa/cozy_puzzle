package com.example.cozypuzzle.core.puzzle.engine

import com.example.cozypuzzle.core.puzzle.model.Puzzle
import com.example.cozypuzzle.core.puzzle.model.PuzzleConfig

interface PuzzleGenerator {

    fun generate(config: PuzzleConfig, seed: Long): Puzzle
}
