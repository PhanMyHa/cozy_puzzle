package com.example.cozypuzzle.domain.repository

import kotlinx.coroutines.flow.Flow

interface FindPairProgressRepository {

    fun observeUnlockedLevels(): Flow<Set<String>>

    fun observeLevelStars(): Flow<Map<String, Int>>

    suspend fun isLevelUnlocked(levelId: String): Boolean

    suspend fun getLevelStars(levelId: String): Int

    suspend fun completeLevel(levelId: String, stars: Int, nextLevelId: String?): Int
}
