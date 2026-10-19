package com.example.cozypuzzle.data.datasource

import com.example.cozypuzzle.R
import com.example.cozypuzzle.core.catpuzzle.model.CatPuzzleDifficulty
import com.example.cozypuzzle.core.catpuzzle.model.CatPuzzleLevel
import com.example.cozypuzzle.data.remote.datasource.GameRemoteDataSource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BundledCatPuzzleDataSource @Inject constructor() {

    @Inject
    lateinit var gameRemoteDataSource: GameRemoteDataSource

    private val defaultBasicLevels = listOf(
        CatPuzzleLevel("cat_1", 1, "Level 1", CatPuzzleDifficulty.BASIC, R.drawable.img_puzzle_cute_cat),
        CatPuzzleLevel("cat_2", 2, "Level 2", CatPuzzleDifficulty.BASIC, R.drawable.img_puzzle_cozy_room),
        CatPuzzleLevel("cat_3", 3, "Level 3", CatPuzzleDifficulty.BASIC, R.drawable.img_puzzle_little_garden),
        CatPuzzleLevel("cat_4", 4, "Level 4", CatPuzzleDifficulty.BASIC, R.drawable.img_puzzle_animals),
        CatPuzzleLevel("cat_5", 5, "Level 5", CatPuzzleDifficulty.BASIC, R.drawable.img_puzzle_picnic),
        CatPuzzleLevel("cat_6", 6, "Level 6", CatPuzzleDifficulty.BASIC, R.drawable.img_puzzle_nature),
        CatPuzzleLevel("cat_7", 7, "Level 7", CatPuzzleDifficulty.BASIC, R.drawable.img_puzzle_food),
        CatPuzzleLevel("cat_8", 8, "Level 8", CatPuzzleDifficulty.BASIC, R.drawable.img_puzzle_frog_pond),
        CatPuzzleLevel("cat_9", 9, "Level 9", CatPuzzleDifficulty.BASIC, R.drawable.img_puzzle_space)
    )

    private val basicLevels: List<CatPuzzleLevel>
        get() {
            val remote = if (::gameRemoteDataSource.isInitialized) {
                gameRemoteDataSource.getCatPuzzleLevels()
            } else {
                null
            }
            return if (!remote.isNullOrEmpty()) remote else defaultBasicLevels
        }

    fun getLevels(difficulty: CatPuzzleDifficulty): List<CatPuzzleLevel> = when (difficulty) {
        CatPuzzleDifficulty.BASIC -> basicLevels
        CatPuzzleDifficulty.INTERMEDIATE -> emptyList()
        CatPuzzleDifficulty.HARD -> emptyList()
    }

    fun getAllLevels(): List<CatPuzzleLevel> = basicLevels

    fun getLevel(levelId: String): CatPuzzleLevel? =
        basicLevels.firstOrNull { it.id == levelId }

    fun getNextLevel(currentLevelId: String): CatPuzzleLevel? {
        val index = basicLevels.indexOfFirst { it.id == currentLevelId }
        return if (index != -1 && index + 1 < basicLevels.size) basicLevels[index + 1] else null
    }

    companion object {
        const val FIRST_LEVEL_ID = "cat_1"
    }
}
