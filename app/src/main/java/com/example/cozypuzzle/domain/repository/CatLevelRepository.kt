package com.example.cozypuzzle.domain.repository

import com.example.cozypuzzle.domain.model.CatLevel

interface CatLevelRepository {

    suspend fun getCatLevels(): List<CatLevel>

    suspend fun getCatLevel(levelId: String): CatLevel?
}
