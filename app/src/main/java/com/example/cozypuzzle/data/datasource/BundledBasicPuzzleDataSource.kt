package com.example.cozypuzzle.data.datasource

import com.example.cozypuzzle.core.puzzle.model.Difficulty
import com.example.cozypuzzle.domain.model.BasicPuzzle
import javax.inject.Inject

class BundledBasicPuzzleDataSource @Inject constructor() : LocalBasicPuzzleDataSource {

    private val puzzles = listOf(
        BasicPuzzle(
            id = "cute_cat",
            title = "Cute Cat",
            imageId = "cute_cat",
            difficulty = Difficulty.EASY,
            availableGridSizes = listOf(3, 4, 5)
        ),
        BasicPuzzle(
            id = "cozy_room",
            title = "Cozy Room",
            imageId = "cozy_room",
            difficulty = Difficulty.MEDIUM,
            availableGridSizes = listOf(3, 4, 5)
        ),
        BasicPuzzle(
            id = "little_garden",
            title = "Little Garden",
            imageId = "little_garden",
            difficulty = Difficulty.MEDIUM,
            availableGridSizes = listOf(3, 4, 5, 6)
        ),
        BasicPuzzle(
            id = "frog_pond",
            title = "Frog Pond",
            imageId = "frog_pond",
            difficulty = Difficulty.HARD,
            availableGridSizes = listOf(4, 5, 6)
        ),
        BasicPuzzle(
            id = "picnic",
            title = "Picnic",
            imageId = "picnic",
            difficulty = Difficulty.EXPERT,
            availableGridSizes = listOf(4, 5, 6)
        )
    )

    override suspend fun getPuzzles(): List<BasicPuzzle> = puzzles

    override suspend fun getPuzzle(puzzleId: String): BasicPuzzle? =
        puzzles.firstOrNull { it.id == puzzleId }
}
