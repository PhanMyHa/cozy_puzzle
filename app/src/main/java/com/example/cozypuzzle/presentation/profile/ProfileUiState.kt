package com.example.cozypuzzle.presentation.profile

import com.example.cozypuzzle.domain.model.profile.AchievementUiModel
import com.example.cozypuzzle.domain.model.profile.PlayerStats
import com.example.cozypuzzle.domain.repository.AppSettings

data class ProfileUiState(
    val playerName: String = "",
    val avatarUri: String? = null,
    val level: Int = 1,
    val levelProgress: Int = 0,
    val coins: Int = 0,
    val stats: PlayerStats = PlayerStats(),
    val achievements: List<AchievementUiModel> = emptyList(),
    val unlockedAchievementsCount: Int = 0,
    val totalAchievementsCount: Int = 0,
    val settings: AppSettings = AppSettings(),
    val appVersion: String = "1.0",
    val isLoading: Boolean = false
)
