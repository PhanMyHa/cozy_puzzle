package com.example.cozypuzzle.domain.model.profile

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

data class PlayerStats(
    val totalStars: Int = 0,
    val levelsCompleted: Int = 0,
    val catsCollected: Int = 0,
    val totalCats: Int = 12,
    val dailyStreak: Int = 0
)

data class Achievement(
    val id: String,
    @StringRes val titleRes: Int,
    @StringRes val descRes: Int,
    @DrawableRes val iconRes: Int,
    val target: Int,
    val rewardStars: Int = 15
)

data class AchievementUiModel(
    val id: String,
    @StringRes val titleRes: Int,
    @StringRes val descRes: Int,
    @DrawableRes val iconRes: Int,
    val currentProgress: Int,
    val target: Int,
    val isUnlocked: Boolean,
    val rewardStars: Int = 15,
    val isClaimed: Boolean = false
)
