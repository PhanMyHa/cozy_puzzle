package com.example.cozypuzzle.core.puzzle.model

enum class Difficulty(val rotateProbability: Double) {
    EASY(0.0),
    MEDIUM(0.25),
    HARD(0.5),
    EXPERT(0.75)
}
