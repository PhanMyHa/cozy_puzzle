package com.example.cozypuzzle.domain.model.starshop

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

enum class StarRewardStatus {
    NOT_COMPLETED,
    CLAIMABLE,
    CLAIMED
}

data class StarShopAchievementItem(
    val id: String,
    @StringRes val titleRes: Int,
    @StringRes val descRes: Int,
    @DrawableRes val iconRes: Int,
    val currentProgress: Int,
    val target: Int,
    val rewardStars: Int,
    val status: StarRewardStatus
)

data class StarShopState(
    val currentStars: Int = 0,
    val dailyLoginClaimedToday: Boolean = false,
    val dailyLoginReward: Int = 15,
    val levelsCompletedCount: Int = 0,
    val totalLevelsCount: Int = 28,
    val dailyMissionsCompletedCount: Int = 0,
    val dailyMissionsTotalCount: Int = 5,
    val hasUnclaimedDailyMissions: Boolean = false,
    val achievements: List<StarShopAchievementItem> = emptyList(),
    val unlockedAchievementsCount: Int = 0,
    val totalAchievementsCount: Int = 7,
    val totalClaimableCount: Int = 0
)

data class StarRewardTransaction(
    val id: String,
    val title: String,
    val starsDelta: Int,
    val timestamp: Long,
    val source: String
)
