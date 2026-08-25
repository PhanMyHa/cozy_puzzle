package com.example.cozypuzzle.domain.usecase.cat

import com.example.cozypuzzle.core.path.engine.CatBoardGenerator
import com.example.cozypuzzle.core.path.model.CatBoard
import com.example.cozypuzzle.core.puzzle.model.Difficulty
import com.example.cozypuzzle.domain.model.CatLevel
import com.example.cozypuzzle.domain.repository.CatLevelRepository
import javax.inject.Inject

data class CatLevelGame(
    val board: CatBoard,
    val levelTitle: String,
    val difficulty: Difficulty,
    val maxHints: Int
)

class StartCatLevelUseCase @Inject constructor(
    private val catLevelRepository: CatLevelRepository,
    private val generator: CatBoardGenerator
) {

    suspend operator fun invoke(levelId: String, seed: Long): CatLevelGame {
        val level = catLevelRepository.getCatLevel(levelId)
            ?: throw IllegalArgumentException("Unknown cat level '$levelId'")

        return CatLevelGame(
            board = generator.generate(
                levelId = level.id,
                rows = level.rows,
                columns = level.columns,
                difficulty = level.difficulty,
                seed = seed
            ),
            levelTitle = level.title,
            difficulty = level.difficulty,
            maxHints = DEFAULT_MAX_HINTS
        )
    }

    private companion object {
        const val DEFAULT_MAX_HINTS = 3
    }
}
