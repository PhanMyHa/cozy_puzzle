package com.example.cozypuzzle.core.profile

import com.example.cozypuzzle.R
import com.example.cozypuzzle.domain.model.profile.Achievement
import com.example.cozypuzzle.domain.model.profile.AchievementUiModel
import com.example.cozypuzzle.domain.model.profile.PlayerStats

object ProfileLogic {

    const val MAX_NICKNAME_LENGTH = 24

    val DEFAULT_ACHIEVEMENTS = listOf(
        Achievement(
            id = "ach_puzzle_novice",
            titleRes = R.string.ach_novice_title,
            descRes = R.string.ach_novice_desc,
            iconRes = R.drawable.ic_mode_cat,
            target = 5,
            rewardStars = 15
        ),
        Achievement(
            id = "ach_puzzle_master",
            titleRes = R.string.ach_master_title,
            descRes = R.string.ach_master_desc,
            iconRes = R.drawable.ic_paw,
            target = 15,
            rewardStars = 30
        ),
        Achievement(
            id = "ach_star_gazer",
            titleRes = R.string.ach_star_gazer_title,
            descRes = R.string.ach_star_gazer_desc,
            iconRes = R.drawable.ic_star_yellow,
            target = 20,
            rewardStars = 20
        ),
        Achievement(
            id = "ach_super_star",
            titleRes = R.string.ach_super_star_title,
            descRes = R.string.ach_super_star_desc,
            iconRes = R.drawable.ic_star_yellow,
            target = 50,
            rewardStars = 50
        ),
        Achievement(
            id = "ach_cat_friend",
            titleRes = R.string.ach_cat_friend_title,
            descRes = R.string.ach_cat_friend_desc,
            iconRes = R.drawable.ic_tab_my_cats,
            target = 3,
            rewardStars = 25
        ),
        Achievement(
            id = "ach_cat_collector",
            titleRes = R.string.ach_cat_collector_title,
            descRes = R.string.ach_cat_collector_desc,
            iconRes = R.drawable.ic_tab_my_cats,
            target = 6,
            rewardStars = 50
        ),
        Achievement(
            id = "ach_streak_flame",
            titleRes = R.string.ach_streak_title,
            descRes = R.string.ach_streak_desc,
            iconRes = R.drawable.ic_streak_flame,
            target = 3,
            rewardStars = 30
        )
    )

    fun validateNickname(name: String): Boolean {
        val trimmed = name.trim()
        return trimmed.isNotEmpty() && trimmed.length <= MAX_NICKNAME_LENGTH
    }

    fun calculateStats(
        catPuzzleStars: Map<String, Int>,
        colorCatStars: Map<String, Int>,
        slideCatStars: Map<String, Int>,
        findPairStars: Map<String, Int>,
        ownedCatsCount: Int,
        totalCatsCount: Int,
        dailyStreak: Int
    ): PlayerStats {
        val totalStars = catPuzzleStars.values.sum() +
                colorCatStars.values.sum() +
                slideCatStars.values.sum() +
                findPairStars.values.sum()

        val levelsCompleted = catPuzzleStars.count { it.value > 0 } +
                colorCatStars.count { it.value > 0 } +
                slideCatStars.count { it.value > 0 } +
                findPairStars.count { it.value > 0 }

        return PlayerStats(
            totalStars = totalStars,
            levelsCompleted = levelsCompleted,
            catsCollected = ownedCatsCount,
            totalCats = totalCatsCount,
            dailyStreak = dailyStreak
        )
    }

    fun evaluateAchievements(
        stats: PlayerStats,
        definitions: List<Achievement> = DEFAULT_ACHIEVEMENTS,
        claimedIds: Set<String> = emptySet()
    ): List<AchievementUiModel> {
        return definitions.map { ach ->
            val progress = when (ach.id) {
                "ach_puzzle_novice", "ach_puzzle_master" -> stats.levelsCompleted
                "ach_star_gazer", "ach_super_star" -> stats.totalStars
                "ach_cat_friend", "ach_cat_collector" -> stats.catsCollected
                "ach_streak_flame" -> stats.dailyStreak
                else -> 0
            }
            val isUnlocked = progress >= ach.target

            AchievementUiModel(
                id = ach.id,
                titleRes = ach.titleRes,
                descRes = ach.descRes,
                iconRes = ach.iconRes,
                currentProgress = progress.coerceAtMost(ach.target),
                target = ach.target,
                isUnlocked = isUnlocked,
                rewardStars = ach.rewardStars,
                isClaimed = claimedIds.contains(ach.id)
            )
        }
    }
}
