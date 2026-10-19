package com.example.cozypuzzle.domain.repository

import com.example.cozypuzzle.domain.model.starshop.StarRewardTransaction
import com.example.cozypuzzle.domain.model.starshop.StarShopState
import kotlinx.coroutines.flow.Flow

interface StarRewardRepository {

    /** Observes the consolidated state for the Star Shop */
    fun observeStarShopState(): Flow<StarShopState>

    /** Observes recent star reward transactions */
    fun observeRecentTransactions(): Flow<List<StarRewardTransaction>>

    /**
     * Claims the daily login reward.
     * Returns the rewarded stars (e.g. 15), or 0 if already claimed today.
     */
    suspend fun claimDailyLoginReward(): Int

    /**
     * Claims the one-time star reward for an unlocked achievement.
     * Returns the rewarded stars, or 0 if already claimed or not yet unlocked.
     */
    suspend fun claimAchievementReward(achievementId: String): Int

    /**
     * Records a reward transaction in history.
     */
    suspend fun recordRewardTransaction(title: String, starsDelta: Int, source: String)
}
