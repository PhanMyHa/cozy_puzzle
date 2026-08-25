package com.example.cozypuzzle.data.datasource

import com.example.cozypuzzle.core.puzzle.model.Difficulty
import com.example.cozypuzzle.domain.model.CatLevel
import javax.inject.Inject

class BundledCatLevelDataSource @Inject constructor() : LocalCatLevelDataSource {

    private val levels = listOf(
        CatLevel("first_steps", "First Steps", 4, 4, Difficulty.EASY),
        CatLevel("garden_detour", "Garden Detour", 5, 5, Difficulty.MEDIUM),
        CatLevel("rocky_path", "Rocky Path", 5, 5, Difficulty.HARD),
        CatLevel("fence_maze", "Fence Maze", 6, 6, Difficulty.MEDIUM),
        CatLevel("long_way_home", "Long Way Home", 6, 6, Difficulty.EXPERT)
    )

    override suspend fun getLevels(): List<CatLevel> = levels

    override suspend fun getLevel(levelId: String): CatLevel? =
        levels.firstOrNull { it.id == levelId }
}
