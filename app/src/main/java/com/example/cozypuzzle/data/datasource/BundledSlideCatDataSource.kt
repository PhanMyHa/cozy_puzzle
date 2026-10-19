package com.example.cozypuzzle.data.datasource

import com.example.cozypuzzle.R
import com.example.cozypuzzle.core.slidethecat.model.SlideCatDifficulty
import com.example.cozypuzzle.core.slidethecat.model.SlideCatLevel
import com.example.cozypuzzle.data.remote.datasource.GameRemoteDataSource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BundledSlideCatDataSource @Inject constructor() {

    @Inject
    lateinit var gameRemoteDataSource: GameRemoteDataSource

    private val easyLevels = listOf(
        SlideCatLevel(
            id = "slide_easy_1",
            number = 1,
            title = "Level 1",
            difficulty = SlideCatDifficulty.EASY,
            gridSize = 3,
            imageRes = R.drawable.img_puzzle_cute_cat,
            shuffleMoves = 15,
            threeStarMoves = 22,
            twoStarMoves = 38
        ),
        SlideCatLevel(
            id = "slide_easy_2",
            number = 2,
            title = "Level 2",
            difficulty = SlideCatDifficulty.EASY,
            gridSize = 3,
            imageRes = R.drawable.img_puzzle_cozy_room,
            shuffleMoves = 15,
            threeStarMoves = 22,
            twoStarMoves = 38
        ),
        SlideCatLevel(
            id = "slide_easy_3",
            number = 3,
            title = "Level 3",
            difficulty = SlideCatDifficulty.EASY,
            gridSize = 3,
            imageRes = R.drawable.img_puzzle_little_garden,
            shuffleMoves = 15,
            threeStarMoves = 22,
            twoStarMoves = 38
        ),
        SlideCatLevel(
            id = "slide_easy_4",
            number = 4,
            title = "Level 4",
            difficulty = SlideCatDifficulty.EASY,
            gridSize = 3,
            imageRes = R.drawable.img_puzzle_animals,
            shuffleMoves = 15,
            threeStarMoves = 22,
            twoStarMoves = 38
        ),
        SlideCatLevel(
            id = "slide_easy_5",
            number = 5,
            title = "Level 5",
            difficulty = SlideCatDifficulty.EASY,
            gridSize = 3,
            imageRes = R.drawable.img_puzzle_picnic,
            shuffleMoves = 15,
            threeStarMoves = 22,
            twoStarMoves = 38
        )
    )

    private val mediumLevels = listOf(
        SlideCatLevel(
            id = "slide_med_1",
            number = 1,
            title = "Level 1",
            difficulty = SlideCatDifficulty.MEDIUM,
            gridSize = 4,
            imageRes = R.drawable.img_puzzle_nature,
            shuffleMoves = 30,
            threeStarMoves = 48,
            twoStarMoves = 80
        ),
        SlideCatLevel(
            id = "slide_med_2",
            number = 2,
            title = "Level 2",
            difficulty = SlideCatDifficulty.MEDIUM,
            gridSize = 4,
            imageRes = R.drawable.img_puzzle_cute_cat,
            shuffleMoves = 30,
            threeStarMoves = 48,
            twoStarMoves = 80
        ),
        SlideCatLevel(
            id = "slide_med_3",
            number = 3,
            title = "Level 3",
            difficulty = SlideCatDifficulty.MEDIUM,
            gridSize = 4,
            imageRes = R.drawable.img_puzzle_space,
            shuffleMoves = 30,
            threeStarMoves = 48,
            twoStarMoves = 80
        )
    )

    private val hardLevels = listOf(
        SlideCatLevel(
            id = "slide_hard_1",
            number = 1,
            title = "Level 1",
            difficulty = SlideCatDifficulty.HARD,
            gridSize = 4,
            imageRes = R.drawable.img_puzzle_food,
            shuffleMoves = 55,
            threeStarMoves = 70,
            twoStarMoves = 110
        ),
        SlideCatLevel(
            id = "slide_hard_2",
            number = 2,
            title = "Level 2",
            difficulty = SlideCatDifficulty.HARD,
            gridSize = 4,
            imageRes = R.drawable.img_puzzle_frog_pond,
            shuffleMoves = 55,
            threeStarMoves = 70,
            twoStarMoves = 110
        ),
        SlideCatLevel(
            id = "slide_hard_3",
            number = 3,
            title = "Level 3",
            difficulty = SlideCatDifficulty.HARD,
            gridSize = 4,
            imageRes = R.drawable.img_puzzle_cozy_room,
            shuffleMoves = 55,
            threeStarMoves = 70,
            twoStarMoves = 110
        )
    )

    private val allActiveLevels: List<SlideCatLevel>
        get() {
            val remote = if (::gameRemoteDataSource.isInitialized) {
                gameRemoteDataSource.getSlideCatLevels()
            } else {
                null
            }
            return if (!remote.isNullOrEmpty()) remote else (easyLevels + mediumLevels + hardLevels)
        }

    fun getLevels(difficulty: SlideCatDifficulty): List<SlideCatLevel> =
        allActiveLevels.filter { it.difficulty == difficulty }

    fun getAllLevels(): List<SlideCatLevel> = allActiveLevels

    fun getLevel(levelId: String): SlideCatLevel? =
        getAllLevels().firstOrNull { it.id == levelId }

    fun getNextLevel(currentLevelId: String): SlideCatLevel? {
        val current = getLevel(currentLevelId) ?: return null
        val list = getLevels(current.difficulty)
        val index = list.indexOfFirst { it.id == currentLevelId }
        return if (index != -1 && index + 1 < list.size) list[index + 1] else null
    }

    companion object {
        const val FIRST_LEVEL_ID = "slide_easy_1"
    }
}
