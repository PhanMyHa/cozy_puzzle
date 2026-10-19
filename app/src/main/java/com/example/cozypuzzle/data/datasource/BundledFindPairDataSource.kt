package com.example.cozypuzzle.data.datasource

import com.example.cozypuzzle.R
import com.example.cozypuzzle.core.findpair.model.FindPairDifficulty
import com.example.cozypuzzle.core.findpair.model.FindPairLevel
import com.example.cozypuzzle.data.remote.datasource.GameRemoteDataSource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BundledFindPairDataSource @Inject constructor() {

    @Inject
    lateinit var gameRemoteDataSource: GameRemoteDataSource

    private val allImages = listOf(
        R.drawable.img_puzzle_cute_cat,
        R.drawable.img_puzzle_cozy_room,
        R.drawable.img_puzzle_little_garden,
        R.drawable.img_puzzle_animals,
        R.drawable.img_puzzle_picnic,
        R.drawable.img_puzzle_nature,
        R.drawable.img_puzzle_food,
        R.drawable.img_puzzle_frog_pond,
        R.drawable.img_puzzle_space,
        R.drawable.img_puzzle_cat_artist
    )

    private val easyLevels = listOf(
        FindPairLevel(
            id = "pair_easy_1",
            number = 1,
            title = "Level 1",
            difficulty = FindPairDifficulty.EASY,
            columns = 3,
            rows = 4,
            timeLimitSeconds = 120,
            threeStarMaxFlips = 16,
            twoStarMaxFlips = 24,
            images = allImages.take(6),
            thumbnailRes = allImages[0]
        ),
        FindPairLevel(
            id = "pair_easy_2",
            number = 2,
            title = "Level 2",
            difficulty = FindPairDifficulty.EASY,
            columns = 3,
            rows = 4,
            timeLimitSeconds = 120,
            threeStarMaxFlips = 16,
            twoStarMaxFlips = 24,
            images = listOf(allImages[1], allImages[2], allImages[3], allImages[4], allImages[5], allImages[6]),
            thumbnailRes = allImages[1]
        ),
        FindPairLevel(
            id = "pair_easy_3",
            number = 3,
            title = "Level 3",
            difficulty = FindPairDifficulty.EASY,
            columns = 3,
            rows = 4,
            timeLimitSeconds = 120,
            threeStarMaxFlips = 16,
            twoStarMaxFlips = 24,
            images = listOf(allImages[2], allImages[3], allImages[4], allImages[5], allImages[6], allImages[7]),
            thumbnailRes = allImages[2]
        ),
        FindPairLevel(
            id = "pair_easy_4",
            number = 4,
            title = "Level 4",
            difficulty = FindPairDifficulty.EASY,
            columns = 3,
            rows = 4,
            timeLimitSeconds = 120,
            threeStarMaxFlips = 16,
            twoStarMaxFlips = 24,
            images = listOf(allImages[3], allImages[4], allImages[5], allImages[6], allImages[7], allImages[8]),
            thumbnailRes = allImages[3]
        ),
        FindPairLevel(
            id = "pair_easy_5",
            number = 5,
            title = "Level 5",
            difficulty = FindPairDifficulty.EASY,
            columns = 3,
            rows = 4,
            timeLimitSeconds = 120,
            threeStarMaxFlips = 16,
            twoStarMaxFlips = 24,
            images = listOf(allImages[4], allImages[5], allImages[6], allImages[7], allImages[8], allImages[9]),
            thumbnailRes = allImages[4]
        )
    )

    private val mediumLevels = listOf(
        FindPairLevel(
            id = "pair_med_1",
            number = 1,
            title = "Level 1",
            difficulty = FindPairDifficulty.MEDIUM,
            columns = 4,
            rows = 4,
            timeLimitSeconds = 180,
            threeStarMaxFlips = 24,
            twoStarMaxFlips = 36,
            images = allImages.take(8),
            thumbnailRes = allImages[0]
        ),
        FindPairLevel(
            id = "pair_med_2",
            number = 2,
            title = "Level 2",
            difficulty = FindPairDifficulty.MEDIUM,
            columns = 4,
            rows = 4,
            timeLimitSeconds = 180,
            threeStarMaxFlips = 24,
            twoStarMaxFlips = 36,
            images = allImages.drop(1).take(8),
            thumbnailRes = allImages[1]
        ),
        FindPairLevel(
            id = "pair_med_3",
            number = 3,
            title = "Level 3",
            difficulty = FindPairDifficulty.MEDIUM,
            columns = 4,
            rows = 4,
            timeLimitSeconds = 180,
            threeStarMaxFlips = 24,
            twoStarMaxFlips = 36,
            images = allImages.drop(2).take(8),
            thumbnailRes = allImages[2]
        )
    )

    private val hardLevels = listOf(
        FindPairLevel(
            id = "pair_hard_1",
            number = 1,
            title = "Level 1",
            difficulty = FindPairDifficulty.HARD,
            columns = 4,
            rows = 5,
            timeLimitSeconds = 240,
            threeStarMaxFlips = 32,
            twoStarMaxFlips = 48,
            images = allImages.take(10),
            thumbnailRes = allImages[0]
        ),
        FindPairLevel(
            id = "pair_hard_2",
            number = 2,
            title = "Level 2",
            difficulty = FindPairDifficulty.HARD,
            columns = 4,
            rows = 5,
            timeLimitSeconds = 240,
            threeStarMaxFlips = 32,
            twoStarMaxFlips = 48,
            images = allImages.reversed().take(10),
            thumbnailRes = allImages[9]
        ),
        FindPairLevel(
            id = "pair_hard_3",
            number = 3,
            title = "Level 3",
            difficulty = FindPairDifficulty.HARD,
            columns = 4,
            rows = 5,
            timeLimitSeconds = 240,
            threeStarMaxFlips = 32,
            twoStarMaxFlips = 48,
            images = listOf(
                allImages[9], allImages[0], allImages[8], allImages[1],
                allImages[7], allImages[2], allImages[6], allImages[3],
                allImages[5], allImages[4]
            ),
            thumbnailRes = allImages[3]
        )
    )

    private val allActiveLevels: List<FindPairLevel>
        get() {
            val remote = if (::gameRemoteDataSource.isInitialized) {
                gameRemoteDataSource.getFindPairLevels()
            } else {
                null
            }
            return if (!remote.isNullOrEmpty()) remote else (easyLevels + mediumLevels + hardLevels)
        }

    fun getLevels(difficulty: FindPairDifficulty): List<FindPairLevel> =
        allActiveLevels.filter { it.difficulty == difficulty }

    fun getAllLevels(): List<FindPairLevel> = allActiveLevels

    fun getLevel(levelId: String): FindPairLevel? =
        getAllLevels().firstOrNull { it.id == levelId }

    fun getNextLevel(currentLevelId: String): FindPairLevel? {
        val current = getLevel(currentLevelId) ?: return null
        val list = getLevels(current.difficulty)
        val index = list.indexOfFirst { it.id == currentLevelId }
        return if (index != -1 && index + 1 < list.size) list[index + 1] else null
    }

    companion object {
        const val FIRST_LEVEL_ID = "pair_easy_1"
    }
}
