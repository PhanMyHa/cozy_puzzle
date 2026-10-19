package com.example.cozypuzzle.domain.repository

import com.example.cozypuzzle.domain.model.daily.DailyTasksUiState
import kotlinx.coroutines.flow.Flow

interface DailyTasksRepository {

    fun observeUiState(): Flow<DailyTasksUiState>

    fun observeUnclaimedCount(): Flow<Int>

    suspend fun claimMissionReward(missionId: String): Int

    suspend fun claimBonusReward(): Int

    suspend fun recordLevelCompleted(mode: String, levelId: String, stars: Int)

    suspend fun recordPairsMatched(count: Int = 1)

    suspend fun recordModePlayed(mode: String)
}
