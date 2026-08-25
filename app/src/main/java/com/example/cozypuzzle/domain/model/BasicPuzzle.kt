package com.example.cozypuzzle.domain.model

import com.example.cozypuzzle.core.puzzle.model.Difficulty

data class BasicPuzzle(
    val id: String,
    val title: String,
    val imageId: String,
    val difficulty: Difficulty,
    val availableGridSizes: List<Int>
)
