package com.example.cozypuzzle.domain.model

import com.example.cozypuzzle.core.puzzle.model.Difficulty

data class CatLevel(
    val id: String,
    val title: String,
    val rows: Int,
    val columns: Int,
    val difficulty: Difficulty
)
